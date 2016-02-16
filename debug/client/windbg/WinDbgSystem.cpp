// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "WinDbgSystem.hpp"
#include <algorithm>
#include <iostream>

#include <tlhelp32.h>

#include <zycon/src/zycon.h>

#include <zywin/src/toolhelp.h>

#include "../errors.hpp"
#include "../logger.hpp"
#include "../DebuggerOptions.hpp"

#include "../windowscommon/WindowsCommon.hpp"
#include <process.h>

#ifdef CPU_AMD64
#define SAVE_SIGN_EXTEND(_x_) (ULONG64)(_x_)
#else
#define SAVE_SIGN_EXTEND(_x_) (ULONG64)(LONG)(_x_ & 0x00000000FFFFFFFF)
#endif

#define BIN_TO_ULONG64(_p_) *(ULONG64*)(_p_)
#define BIN_TO_ULONG(_p_) *(ULONG*)(_p_)

HANDLE ghMutexLogging = NULL;

CRITICAL_SECTION csException;
CRITICAL_SECTION csHalt;

ULONG pWinlogonEprocess = 0;

const NATIVE_STRING windbgEventNames[NUM_INTERNAL_EVENTS] = {
    "WDBG_BREAKPOINT_HIT", "WDBG_EXCEPTION", "WDBG_DEBUGGER_ATTACHED",
    "ZY_COM_INITIALIZED", "ZY_WAIT_FOR_DEBUG_EVENT",
    "ZY_WAIT_FOR_DEBUG_EVENT_RETURNED", "ZY_RESUMING_DEBUGEE" };

#define THREAD_SAFE_LOGGING
/**
 * Thread-safe logging function
 */
#ifdef THREAD_SAFE_LOGGING
void log(unsigned int level, const char* message, ...) {

  if (ghMutexLogging == NULL) {
    ghMutexLogging = CreateMutex(NULL, false, NULL);
  }
  if (WaitForSingleObject(ghMutexLogging, INFINITE) != WAIT_OBJECT_0) {
    return;
  }

  va_list arglist;
  char text[2000];
  va_start(arglist, message);
  vsnprintf(text, 2000, message, arglist);
  va_end(arglist);

  msglog->log(level, text);

  ReleaseMutex(ghMutexLogging);
}
#else
#define log msglog->log
#endif

IDebugClient* WinDbgSystem::client;
IDebugControl4* WinDbgSystem::control;
IDebugSymbols* WinDbgSystem::symbols;
IDebugRegisters* WinDbgSystem::dregister;
IDebugSystemObjects4* WinDbgSystem::systemObjects4;
IDebugDataSpaces4* WinDbgSystem::dataspaces4;
IDebugAdvanced3* WinDbgSystem::advanced3;

/**
 * Logs information concerning the last debug event
 */
void logLastDbgEventInfo() {
  struct LastEventInformation {
    ULONG Type;
    ULONG ProcessId;
    ULONG ThreadId;
  } ei;

  ULONG threadId;
  ULONG processId;
  ULONG cthreadId;
  ULONG cprocessId;
  ULONG nthreads;
  ULONG nprocesses;
  ULONG tthreads;
  ULONG lprocess;

  WinDbgSystem::getDebugControl()->GetLastEventInformation(&ei.Type,
                                                           &ei.ProcessId,
                                                           &ei.ThreadId, NULL,
                                                           0, NULL, NULL, 0,
                                                           NULL);
  if (WinDbgSystem::getDebugSystemObjects()->GetEventThread(&threadId)
      != S_OK) {
    log(LOG_ALL, "Error, GetEventThread() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetEventProcess(&processId)
      != S_OK) {
    log(LOG_ALL, "Error, GetEventProcess() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetCurrentProcessId(&cprocessId)
      != S_OK) {
    log(LOG_ALL, "Error, GetCurrentProcessId() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetCurrentThreadId(&cthreadId)
      != S_OK) {
    log(LOG_ALL, "Error, GetCurrentThreadId() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetNumberThreads(&nthreads)
      != S_OK) {
    log(LOG_ALL, "Error, GetNumberThreads() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetNumberProcesses(&nprocesses)
      != S_OK) {
    log(LOG_ALL, "Error, GetNumberProcesses() failed.");
  }

  if (WinDbgSystem::getDebugSystemObjects()->GetTotalNumberThreads(&tthreads,
                                                                   &lprocess)
      != S_OK) {
    log(LOG_ALL, "Error, GetTotalNumberThreads() failed.");
  }

  log(LOG_ALL,
      "'-> LastEventInformation:\n\tType: 0x%08x\n\tpID: 0x%08x\n\ttID: "
      "0x%08x\n\ttID2: 0x%08x\n\tpID2: 0x%08x\n\tctID: 0x%08x\n\tcpID: "
      "0x%08x\n\tnumber of threads: %d\n\tnumber of processes: %d\n\ttotal "
      "number of threads: %d\n\tlargest number of threads: %d",
      ei.Type, ei.ProcessId, ei.ThreadId, threadId, processId, cthreadId,
      cprocessId, nthreads, nprocesses, tthreads, lprocess);

}

/**
 * Inits WinDbg COM objects and waits for debug events
 */
void waitForEventThread(WinDbgSystem* winDbg) {
  WinDbgSystem::initCOM();

  IDebugControl4* control = WinDbgSystem::getDebugControl();

  HANDLE hEventCOMInit = OpenEvent(EVENT_MODIFY_STATE, false,
                                   windbgEventNames[ZY_COM_INITIALIZED]);
  if (hEventCOMInit == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open ZY_COM_INITIALIZED event.");
  }

  HANDLE hEventResumeDebugee = OpenEvent(
      EVENT_MODIFY_STATE | SYNCHRONIZE, false,
      windbgEventNames[ZY_WAIT_FOR_DEBUG_EVENT]);
  if (hEventCOMInit == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open ZY_WAIT_FOR_DEBUG_EVENT event.");
  }

  HANDLE hEventResumingDebugee = OpenEvent(
      EVENT_MODIFY_STATE, false, windbgEventNames[ZY_RESUMING_DEBUGEE]);
  if (hEventResumingDebugee == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open ZY_RESUMING_DEBUGEE event.");
  }

  HANDLE hEventReturned = OpenEvent(
      EVENT_MODIFY_STATE, false,
      windbgEventNames[ZY_WAIT_FOR_DEBUG_EVENT_RETURNED]);
  if (hEventCOMInit == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open ZY_WAIT_FOR_EVENT_RETURNED event.");
  }

  SetEvent(hEventCOMInit);

  for (;;) {
    log(LOG_ALL, "Info: waitForEventThread - halting debugee");

    if (WaitForSingleObject(hEventResumeDebugee, INFINITE) != WAIT_OBJECT_0) {
      log(LOG_ALWAYS, "Error: waitForEventThread failed to wait "
          "ZY_WAIT_FOR_DEBUG_EVENT, error: %08x",
          GetLastError());
    }

    if (ResetEvent(hEventReturned) == false) {
      log(LOG_ALWAYS, "Error: waitForEventThread failed to reset "
          "ZY_WAIT_FOR_EVENT_RETURNED, error: %08x",
          GetLastError());
    }

    log(LOG_ALL, "Info: waitForEventThread - resuming debugee");
    if (HRESULT result = control->WaitForEvent(0, INFINITE) != S_OK) {
      log(LOG_ALWAYS, "Error: control->WaitForEvent failed with code: 0x%08x",
          result);
    }

    winDbg->updateActiveThread();

    EnterCriticalSection(&csHalt);

    log(LOG_ALL, "Info: waitForEventThread - got debug event...");
    if (SetEvent(hEventReturned) == false) {
      log(LOG_ALWAYS, "Error: waitForEventThread failed to set "
          "ZY_WAIT_FOR_EVENT_RETURNED, error: %08x",
          GetLastError());
    }

    LeaveCriticalSection(&csHalt);
  }
}

STDMETHODIMP_(ULONG) WinDbgSystem::OutputCallbacks::AddRef(THIS) {
  // This class is designed to be static so
  // there's no true refcount.
  return 1;
}

STDMETHODIMP_(ULONG) WinDbgSystem::OutputCallbacks::Release(THIS) {
  // This class is designed to be static so
  // there's no true refcount.
  return 1;
}

//TODO: remove?
STDMETHODIMP WinDbgSystem::OutputCallbacks::QueryInterface(
    THIS_ IN REFIID interfaceId, OUT PVOID* ppInterface) {
  *ppInterface = 0;
  HRESULT res = E_NOINTERFACE;
  if (TRUE == IsEqualIID(interfaceId, __uuidof(IUnknown)) ||
      TRUE == IsEqualIID(interfaceId, __uuidof(IDebugOutputCallbacks))) {
    *ppInterface = (IDebugOutputCallbacks*)this;
    AddRef();
    res = S_OK;
  }
  return res;
}

STDMETHODIMP WinDbgSystem::OutputCallbacks::Output(THIS_ IN ULONG mask,
    IN PCSTR msg) {
  log(LOG_VERBOSE, msg);

  return S_OK;

}

void printLastError() {
  unsigned int err = GetLastError();

  LPVOID message;
  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
                NULL, err, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                (LPTSTR) & message, 0, NULL);

  log(LOG_ALWAYS,
      "Error: ContinueDebugEvent() failed with error: %s (Error Code: %d)",
      message, err);

  LocalFree(message);
}

void dbg_echo_MEMORY_BASIC_INFORMATION(MEMORY_BASIC_INFORMATION* m) {
  log(LOG_VERBOSE, "Base: 				%lx", m->BaseAddress);
  log(LOG_VERBOSE, "AllocationBase: 		%lx", m->AllocationBase);
  log(LOG_VERBOSE, "AllocationProtect: 	%lx", m->AllocationProtect);
  log(LOG_VERBOSE, "RegionSize: 			%lx", m->RegionSize);
  log(LOG_VERBOSE, "state:				%lx", m->State);
  log(LOG_VERBOSE, "Protect:				%lx", m->Protect);
  log(LOG_VERBOSE, "Type:					%lx", m->Type);
}

/**
 * Makes a page of the target process memory writable.
 *
 * @param offset The offset whose page should be made writable.
 * @param oldProtection The protection level of the memory before it is made
 * writable.
 *
 * @return True, if the operation succeeded. False, otherwise.
 */
bool makePageWritable(CPUADDRESS offset, DWORD& oldProtection) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  return true;
}

/**
 * Restores the old protection level of a page of the target process memory.
 *
 * @param offset The offset whose page should be made restored.
 * @param oldProtection The original protection level of the page.
 */
NaviError restoreMemoryProtection(CPUADDRESS offset, DWORD oldProtection) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  return true;
}

WinDbgSystem::EventCallbacks::EventCallbacks(WinDbgSystem* windbg) {
  hEventBreakpoint = NULL;
  hEventException = NULL;
  unhandledException = false;
  this->windbg = windbg;

  InitializeCriticalSection (&csException);

  lastBreakpoint = (DEBUG_BREAKPOINT_PARAMETERS*) malloc(
      sizeof(DEBUG_BREAKPOINT_PARAMETERS*));
  lastException = (EXCEPTION_RECORD64*) malloc(sizeof(EXCEPTION_RECORD64));
}

WinDbgSystem::EventCallbacks::~EventCallbacks() {
  if (hEventBreakpoint != NULL) {
    CloseHandle (hEventBreakpoint);
  }
  if (hEventException != NULL) {
    CloseHandle (hEventException);
  }
  if (lastBreakpoint != NULL) {
    free (lastBreakpoint);
  }
  if (lastException != NULL) {
    free (lastException);
  }
}

void WinDbgSystem::EventCallbacks::openEvents() {
  hEventBreakpoint = OpenEvent(EVENT_MODIFY_STATE, false,
                               windbgEventNames[WDBG_BREAKPOINT_HIT]);
  if (hEventBreakpoint == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open breakpoint event.");
  }

  hEventException = OpenEvent(EVENT_MODIFY_STATE, false,
                              windbgEventNames[WDBG_EXCEPTION]);
  if (hEventBreakpoint == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open exception event.");
  }

  hEventDebuggerAttached = OpenEvent(EVENT_MODIFY_STATE, false,
                                     windbgEventNames[WDBG_DEBUGGER_ATTACHED]);
  if (hEventDebuggerAttached == NULL) {
    log(LOG_ALWAYS, "Error: Failed to open debugger attached event.");
  }
}

void WinDbgSystem::EventCallbacks::setLastBreakpoint(PDEBUG_BREAKPOINT Bp) {
  if (lastBreakpoint != NULL) {
    if (Bp->GetParameters(lastBreakpoint) != S_OK) {
      log(LOG_ALWAYS, "Error: Failed to retrieve breakpoint parameters.");
    }
  }
}

DEBUG_BREAKPOINT_PARAMETERS* WinDbgSystem::EventCallbacks::getLastBreakpoint() {
  return lastBreakpoint;
}

void WinDbgSystem::EventCallbacks::setLastException(
    PEXCEPTION_RECORD64 Exception) {
  if (lastException != NULL) {
    unhandledException = true;
    memcpy(lastException, Exception, sizeof(EXCEPTION_RECORD64));
  }
}

EXCEPTION_RECORD64* WinDbgSystem::EventCallbacks::getLastException() {
  unhandledException = false;
  return lastException;
}

unsigned int WinDbgSystem::EventCallbacks::getLastExceptionThreadId() {
  return lastExceptionThreadId;
}

void WinDbgSystem::EventCallbacks::setLastExceptionThreadId(unsigned int tid) {
  lastExceptionThreadId = tid;
}

EXCEPTION_RECORD64* WinDbgSystem::EventCallbacks::getLastExceptionUnhandled() {
  bool unhandled = unhandledException;
  unhandledException = false;
  return unhandled ? lastException : NULL;
}

bool WinDbgSystem::EventCallbacks::hasUnhandledException() {
  return unhandledException;
}

void WinDbgSystem::EventCallbacks::setLastExceptionUnhandled() {
  unhandledException = true;
}

STDMETHODIMP_ (ULONG)
WinDbgSystem::EventCallbacks::AddRef(THIS) {
  // This class is designed to be static so
  // there's no true refcount.
  return 1;
}

STDMETHODIMP_ (ULONG)
WinDbgSystem::EventCallbacks::Release(THIS) {
  // This class is designed to be static so
  // there's no true refcount.
  return 0;
}
STDMETHODIMP WinDbgSystem::EventCallbacks::GetInterestMask(
    THIS_ OUT PULONG Mask) {
  *Mask = DEBUG_EVENT_BREAKPOINT | DEBUG_EVENT_EXCEPTION |
  DEBUG_EVENT_CREATE_THREAD | DEBUG_EVENT_SESSION_STATUS;

  openEvents();
  return DEBUG_STATUS_NO_CHANGE;
}

STDMETHODIMP WinDbgSystem::EventCallbacks::CreateThread(ULONG64 Handle,
                                                        ULONG64 DataOffset,
                                                        ULONG64 StartOffset) {
  log(LOG_ALL, "Info: new thread created:\n\tHandle:\t "
      "0x%08x%08x\n\tDataOffset:\t 0x%08x\n\tStartOffset:\t 0x%08x",
      (ULONG32)(Handle >> 32), (ULONG32)(Handle & 0xFFFFFFFF),
      (CPUADDRESS) DataOffset, (CPUADDRESS) StartOffset);

  return DEBUG_STATUS_NO_CHANGE;
}

STDMETHODIMP WinDbgSystem::EventCallbacks::Breakpoint(
    THIS_ IN PDEBUG_BREAKPOINT Bp) {
  ULONG64 addr;
  Bp->GetOffset(&addr);
  log(LOG_ALL, "Info: Got windbg breakpoint at 0x%08x", (CPUADDRESS) addr);
  setLastBreakpoint(Bp);
  if (hEventBreakpoint != NULL) {
    SetEvent(hEventBreakpoint);
  }

  return DEBUG_STATUS_NO_CHANGE;
}

STDMETHODIMP WinDbgSystem::EventCallbacks::SessionStatus(
    THIS_ IN ULONG SessionStatus) {

  if (SessionStatus == DEBUG_SESSION_ACTIVE) {
    SetEvent(hEventDebuggerAttached);
  }

  return DEBUG_STATUS_NO_CHANGE;
}

STDMETHODIMP WinDbgSystem::EventCallbacks::Exception(
    PEXCEPTION_RECORD64 Exception, ULONG FirstChance) {
  log(LOG_ALL, "Info: Got exception at 0x%08x", Exception->ExceptionAddress);
  logLastDbgEventInfo();

  unsigned int tid;
  if (windbg->readThreadId(tid) == NaviErrors::SUCCESS) {
    setLastExceptionThreadId(tid);
  } else {
    log(LOG_ALWAYS, "Error: Failed to get thread id in exception callback.");
    setLastExceptionThreadId(-1);
  }

  setLastException(Exception);
  if (hEventException != NULL) {
    SetEvent (hEventException);
  }

  return DEBUG_STATUS_NO_CHANGE;
}

WinDbgSystem::WinDbgSystem(const NATIVE_STRING pipe)
    : BaseSystem(),
      nextContinue(DBG_CONTINUE),
      pipe(pipe),
      hProcess(0),
      setBurst(false),
      removeBurst(false),
      firstBreakpointException(true),
      addrInterrupt(0) {
  eventCallbacks = new EventCallbacks(this);
  outputCallbacks = new OutputCallbacks();

  InitializeCriticalSection (&csHalt);

  attachToPipe();
}

/**
 * reads a single byte from the virtual kernel address space of the target
 * machine.
 *
 * @param offset Memory offset to read from.
 * @param b The read byte is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readByte(CPUADDRESS offset, char& b) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  DWORD startTime = GetTickCount();

  ULONG64 addr = SAVE_SIGN_EXTEND(offset);
  ULONG64 validBase;
  ULONG validSize;
  if (dataspaces4->GetValidRegionVirtual(addr, 1, &validBase, &validSize)
      != S_OK) {
#ifdef CPU_AMD64
    log(LOG_ALL,
        "Error: Couldn't validate read access of byte at address %016x",
        offset);
#else
    log(LOG_ALL,
        "Error: Couldn't validate read access of byte byte at address %08x",
        offset);
    return NaviErrors::COULDNT_READ_MEMORY;
#endif
  }

  if ((validSize != 1) || (addr != validBase)) {
#ifdef CPU_AMD64
    log(LOG_ALWAYS, "No access possible to byte at address %016x", offset);
#else
    log(LOG_ALWAYS, "No access possible to byte at address %08x", offset);
#endif
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  if (dataspaces4->ReadVirtual(addr, &b, sizeof(char), NULL) != S_OK) {
#ifdef CPU_AMD64
    log(LOG_ALWAYS, "Error: Couldn't read byte at address %016x", offset);
#else
    log(LOG_ALWAYS,
        "Error: Couldn't read byte at address %08x, GetLastError(): 0x%08x",
        offset, GetLastError());
    return NaviErrors::COULDNT_READ_MEMORY;
#endif
  }

  log(LOG_ALL, "Read 1 byte in %dms", GetTickCount() - startTime);
  return NaviErrors::SUCCESS;
}

/**
 * writes a single byte to the virtual kernel address space of the target
 * machine.
 *
 * @param offset Memory offset to write to.
 * @param b Byte to write to the target process memory.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::writeByte(CPUADDRESS offset, char b) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (dataspaces4->WriteVirtual(SAVE_SIGN_EXTEND(offset), &b, sizeof(char),
                                NULL) != S_OK) {
#ifdef CPU_AMD64
    log(LOG_ALWAYS, "Error: Couldn't read byte at address %016x", offset);
#else
    log(LOG_ALWAYS, "Error: Couldn't read byte at address %08x", offset);
    return NaviErrors::COULDNT_READ_MEMORY;
#endif
  }
  return NaviErrors::SUCCESS;
}

/**
 * Changes the step mode of the target process.
 *
 * @param threadID The thread ID of the thread whose step mode is changed.
 * @param enterSingleStep If true, the trap flag is set. Else, it is cleared.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::changeStepMode(unsigned int threadId,
                                       bool enterSingleStep) {
  ULONG indexEflags;
  DEBUG_VALUE valueeflags;

  if (dregister->GetIndexByName("efl", &indexEflags) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to get index of register EFLAGS.");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  if (dregister->GetValue(indexEflags, &valueeflags) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read value of register EFLAGS");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  const unsigned int TRAP_FLAG = 0x100;

  valueeflags.Type = DEBUG_VALUE_INT32;
  if (enterSingleStep) {
    valueeflags.I32 |= TRAP_FLAG;
  } else {
    valueeflags.I32 &= ~TRAP_FLAG;
  }

  if (dregister->SetValue(indexEflags, &valueeflags) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set value of register EFLAGS");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Attaches to the WinDBG pipe.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::attachToPipe() {
  hMutexLogging = CreateMutex(NULL, true, NULL);

  //BaseSystem(connection, commands);
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  //create internal events
  for (int i = 0; i < NUM_INTERNAL_EVENTS; i++) {
    eventHandles[i] = CreateEvent(NULL, false, false, windbgEventNames[i]);
  }

  //copy dbg event handles to seperate list
  for (int i = 0; i < NUM_INTERNAL_DBG_EVENTS; i++) {
    dbgEventHandles[i] = eventHandles[i];
  }

  CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE) waitForEventThread, this, 0,
               NULL);

  if (WaitForSingleObject(eventHandles[ZY_COM_INITIALIZED], INFINITE)
      != WAIT_OBJECT_0) {
    log(LOG_VERBOSE, "Error, failed to wait ZY_COM_INITIALIZED event.");
  }

  client->SetOutputCallbacks(outputCallbacks);

  //all necesarry com objects were created- connect to the target machine now!
  connectToPipe();
  return NaviErrors::SUCCESS;

}

WinDbgSystem::~WinDbgSystem() {
  for (int i = 0; i < NUM_INTERNAL_EVENTS; i++) {

    if (CloseHandle(eventHandles[i]) == false) {
      log(LOG_ALL, "Error, failed to close event handle with index %d", i);
    }
  }
  cleanUpCOM();
  delete eventCallbacks;
  delete outputCallbacks;
}

void WinDbgSystem::initCOM() {
  HRESULT hr = E_FAIL;

  // Initialize COM
  hr = CoInitialize(NULL);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to initialize COM.");
    cleanUpCOM();
  }

  // Create the base IDebugClient object
  hr = DebugCreate(__uuidof(IDebugClient), (LPVOID*) &client);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugClient.");
    cleanUpCOM();
  }

  // from the base, create the Control and Symbols objects
  hr = client->QueryInterface(__uuidof(IDebugControl4), (LPVOID*) &control);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugControl.");
    cleanUpCOM();
  }

  // from the base, create the Control and Symbols objects
  hr = client->QueryInterface(__uuidof(IDebugRegisters), (LPVOID*) &dregister);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugRegisters.");
    cleanUpCOM();
  }

  //create register object
  hr = client->QueryInterface(__uuidof(IDebugSymbols), (LPVOID*) &symbols);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugSymbols.");
    cleanUpCOM();
  }

  //create IDebugDataSpaces
  hr = client->QueryInterface(__uuidof(IDebugDataSpaces4),
                              (LPVOID*) &dataspaces4);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugDataSpaces4.");
    cleanUpCOM();
  }

  //create IDebugSystemObjects
  hr = client->QueryInterface(__uuidof(IDebugSystemObjects4),
                              (LPVOID*) &systemObjects4);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugSystemObjects.");
    cleanUpCOM();
  }

  //create IDebugSystemObjects
  hr = client->QueryInterface(__uuidof(IDebugAdvanced), (LPVOID*) &advanced3);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to create IDebugAdvanced.");
    cleanUpCOM();
  }
}

void WinDbgSystem::cleanUpCOM() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (dataspaces4 != NULL) {
    dataspaces4->Release();
    dataspaces4 = NULL;
  }

  if (symbols != NULL) {
    symbols->Release();
    symbols = NULL;
  }

  if (dregister != NULL) {
    dregister->Release();
    dregister = NULL;
  }

  if (control != NULL) {
    control->Release();
    control = NULL;
  }

  if (client != NULL) {
    client->Release();
    client = NULL;
  }

  if (systemObjects4 != NULL) {
    systemObjects4->Release();
    systemObjects4 = NULL;
  }

  if (advanced3 != NULL) {
    advanced3->Release();
    advanced3 = NULL;
  }

  // cleanup COM
  CoUninitialize();
}
/**
 * @return A NaviError code that describes whether the operation was successful or
 * not.
 */
NaviError WinDbgSystem::connectToPipe() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT hr;

  log(LOG_ALWAYS, "please wait until debuggee is attached");

  //install callbacks
  HRESULT Status;
  if ((Status = client->SetEventCallbacks(eventCallbacks)) != S_OK) {
    log(LOG_ALWAYS, "Error: SetEventCallbacks failed, 0x%X\n", Status);
    cleanUpCOM();
  }

  // set initial breakpoint
  hr = control->SetEngineOptions(DEBUG_ENGOPT_INITIAL_BREAK);
  if (FAILED(hr)) {
    log(LOG_ALL, "Error: SetEngineOptions failed.");
    return NaviErrors::COULDNT_ENTER_DEBUG_MODE;
  }

  // attach to kernel
  hr = client->AttachKernel(DEBUG_ATTACH_KERNEL_CONNECTION, pipe);

  if (FAILED(hr)) {
    log(LOG_ALWAYS, "Error: Failed to attach to kernel");
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }

  resumeWaitThread();

  if (WaitForSingleObject(eventHandles[WDBG_DEBUGGER_ATTACHED], INFINITE)
      != WAIT_OBJECT_0) {
    log(LOG_ALWAYS, "Error: Failed to wait for debugger to attach, this should "
        "never happen.");
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }

  //skip over initial debug events

  while (readDebugEvents() == NaviErrors::SUCCESS) {
    //resumeThread(0);
  }

  log(LOG_ALWAYS,
      "Successfully attached to debugee, you may now connect with BinNavi");

  return NaviErrors::SUCCESS;
}
/**
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::attachToProcess() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  Module m = kernelModules.at(getPID());
  //create one fake thread
  //TODO: one thread per core

  setActiveThread(0);
  Thread ts(0, RUNNING);

  // Generate processStart message and send it to BinNavi.

  processStart(m, ts);

  return NaviErrors::SUCCESS;
}
/**
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::startProcess(
    const NATIVE_STRING path,
    const std::vector<const NATIVE_STRING>& commands) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  return NaviErrors::SUCCESS;
}

/**
 * Detaches from the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::detach() {

  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT hr;

  // set interrupt exit
  hr = control->SetEngineOptions(DEBUG_INTERRUPT_EXIT);
  if (FAILED(hr)) {
    log(LOG_ALWAYS, "SetEngineOptions failed.");
    return NaviErrors::COULDNT_DETACH;
  }

  //detach form kernel
  client->EndSession(DEBUG_END_PASSIVE);
  client->DetachProcesses();

  //kill process because we don't want to reconnect
  cleanUpCOM();
  ExitProcess(0);

  return NaviErrors::SUCCESS;
}

/**
 * Terminates the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::terminateProcess() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  return NaviErrors::SUCCESS;
}

/**
 * Does nothing, original data is stored in setBreakpoint
 *
 * @param bp The breakpoint in question.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::storeOriginalData(const BREAKPOINT& bp) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  return NaviErrors::SUCCESS;
}

/**
 * Wrapper for setBreakpointRaw
 *
 * @param bp The breakpoint to be set.
 *
 * @param indicates whether the reuqest is part of a series of requests
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::setBreakpoint(const BREAKPOINT& bp, bool moreToCome) {
  NaviError retVal;

  //check if this request is the first of a whole series of requests
  if ((setBurst == false) && (moreToCome == true)) {
    setBurst = true;
    startTempHalt();
  }

  if (setBurst == true) {
    retVal = setBreakpointRaw(bp);

    //check if this was the last request of a series
    if (moreToCome == false) {
      //reset echo burst flag and resume target
      setBurst = false;
      endTempHalt();
    }
  } else {
    //just an average request - halt and resume
    startTempHalt();
    retVal = setBreakpointRaw(bp);
    endTempHalt();
  }
  return retVal;
}

/**
 * Sets a breakpoint in the target process.
 *
 * @param breakpoint The breakpoint to be set.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::setBreakpointRaw(const BREAKPOINT& breakpoint) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  //save original data
  std::string addressString = cpuAddressToString(breakpoint.addr);

  //check if data was already stored
  if (originalBytes.find(addressString) == originalBytes.end()) {
    char b;

    NaviError result = readByte(breakpoint.addr, b);

    if (result) {
      log(LOG_ALWAYS, "Error: Couldn't read byte from address %s",
          addressString.c_str());
      return result;
    }

    originalBytes[addressString] = b;
  }
  const unsigned char BREAKPOINT_OPCODE = 0xCC;

  NaviError retVal = writeByte(breakpoint.addr, BREAKPOINT_OPCODE);

  return retVal;
}

/**
 * Wrapper for removeBreakpointRaw
 *
 * @param bp The breakpoint to be removed.
 *
 * @param indicates whether the reuqest is part of a series of requests
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::removeBreakpoint(const BREAKPOINT& bp,
                                         bool moreToCome) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  NaviError retVal;

  //check if this request is the first of a whole series of requests
  if ((removeBurst == false) && (moreToCome == true)) {
    removeBurst = true;
    startTempHalt();
  }

  if (removeBurst == true) {
    retVal = removeBreakpointRaw(bp);

    //check if this was the last request of a series
    if (moreToCome == false) {
      //reset echo burst flag and resume target
      removeBurst = false;
      endTempHalt();
    }
  } else {
    //just an average request - halt and resume
    startTempHalt();
    retVal = removeBreakpointRaw(bp);
    endTempHalt();
  }
  return retVal;
}

/**
 * Removes a breakpoint from the target process.
 *
 * @param bp The breakpoint to be removed.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::removeBreakpointRaw(const BREAKPOINT& bp) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  std::string addressString = cpuAddressToString(bp.addr);

  if (originalBytes.find(addressString) == originalBytes.end()) {
    //if the breakpoint to be removed is at the current ip address, it already
    //had been removed -> return success
    CPUADDRESS ip;
    getInstructionPointer(0, ip);
    if (bp.addr == ip) {
      return NaviErrors::SUCCESS;
    } else {
      log(LOG_ALWAYS,
          "Error: Trying to restore a breakpoint with unknown original byte");
      return NaviErrors::ORIGINAL_DATA_NOT_AVAILABLE;
    }
  }

  char b = originalBytes[addressString];

  log(LOG_VERBOSE, "Writing byte %1x to address %s", b, addressString.c_str());

  NaviError retVal = writeByte(bp.addr, b);

  //remove bp from originalBytes
  originalBytes.erase(addressString);

  //add to list of removed breakpoints
  removedBreakpoints.insert(bp.addr);

  return retVal;
}

/**
 * prints current instruction to logger
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::log_current_instruction() {
  char b;
  CPUADDRESS myaddress;
  this->getInstructionPointer(0, myaddress);
  readByte(myaddress, b);
  log(LOG_ALL, "char@%x is %x", myaddress, b);
  return NaviErrors::SUCCESS;
}

/**
 * The alreadyStepped set keeps track of all threads that were already single-
 * stepped "recently".
 *
 * This is necessary because of the following scenario:
 *
 * - Thread A hits breakpoint
 * - Thread A is stepped over the breakpoint
 * - Before Thread A stepping is complete, Thread B hits breakpoint
 * - Thread B is stepped over the breakpoint
 * - Before Thread B stepping is complete, Thread A stepping is complete
 *
 * What has to happen now is that the step-loop of Thread A has to
 * be stopped but Thread A does not know this because it was stepped in
 * the step-loop of Thread B and the two loops can not communicate with
 * each other directly. Therefore alreadyStepped is used.
 *
 * You can make this scenario arbitrarily difficult by adding more threads.
 */
std::set<unsigned int> alreadyStepped;

/**
 * Executes a single instruction.
 *
 * @param tid The thread ID of the thread that executes the instruction.
 * @param address The address of the instruction pointer after the single step.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::doSingleStep(unsigned int& tid, CPUADDRESS& address) {
  tid = getActiveThread();  //TODO: does this make sense?

  log(LOG_ALL, "Entering %s, tid: %d", __FUNCTION__, tid);

  //remove thread id from alreadyStepped set
  if (alreadyStepped.find(tid) != alreadyStepped.end()) {
    alreadyStepped.erase(tid);
  }

  //change the execution status to single step
  if (changeExecutionStatus(DEBUG_STATUS_STEP_INTO) != NaviErrors::SUCCESS) {
    log(LOG_ALWAYS, "Error: Failed to execute single-step.");
    return NaviErrors::COULDNT_ENTER_SINGLE_STEP_MODE;
  }

  //now wait for the corresponding event

  while (alreadyStepped.find(tid) == alreadyStepped.end()) {
    if (waitForDebugEventConsume() != NaviErrors::SUCCESS) {
      log(LOG_ALWAYS,
          "Error: In doSingleStep() failed to wait for next event.");
      return NaviErrors::COULDNT_ENTER_SINGLE_STEP_MODE;
    }

    //get the id of thread the execution stopped in
    unsigned int newTid = getActiveThread();

    //get the last unhandled exception
    EXCEPTION_RECORD64* ex = eventCallbacks->getLastExceptionUnhandled();

    //get the ip the execution stopped at
    CPUADDRESS ip;
    this->getInstructionPointer(0, ip);
    address = ip;
    if (ex == NULL) {
      if (ip != this->addrInterrupt) {
        //this is the normal case

        //if singelstep succeeds on first try, we should end-up here
        log(LOG_VERBOSE, "Info: Single-stepped to address %08x, TID: %d", ip,
            newTid);

        //TODO: simplify if-else construct!
        //heuristic: if we're in a different thread and got no exception, then
        //we must have hit an int3 or gotten a single-step exception!
        if (newTid != tid) {
          std::string addressString = cpuAddressToString(ip);
          if (hasCustomBreakpoint(addressString) == true) {
            //breakpoint hit in different thread
            log(LOG_VERBOSE, "Info: Hit a breakpoint at 0x%08x in thread %d "
                "while executing a single-step for thread %d",
                ip, newTid, tid);
            breakpointHit(ip, tid, true /* continue on echo bp */);
          } else {
            //single-step event in different thread
            log(LOG_VERBOSE, "Info: Single-stepped to 0x%08x in thread %d "
                "while executing a single-step for thread %d",
                ip, newTid, tid);
            alreadyStepped.insert(newTid);
            changeExecutionStatus (DEBUG_STATUS_GO);
          }
        } else {
          //single-step was executed as it should be
          alreadyStepped.insert(newTid);
        }
      } else {
        log(LOG_VERBOSE, "Info: Hit interrupt in doSingleStep()");
        changeExecutionStatus (DEBUG_STATUS_GO);
      }
    } else {
      processDebugEvent (WDBG_EXCEPTION);
    }

  }
  log(LOG_VERBOSE, "Info: Leaving doSingleStep() for TID: %d", tid);
  return NaviErrors::SUCCESS;
}

/**
 * Resumes the WinDBG waiting thread.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::resumeWaitThread() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (SetEvent(eventHandles[ZY_WAIT_FOR_DEBUG_EVENT]) != TRUE) {
    log(LOG_VERBOSE, "Error, failed to set event ZY_WAIT_FOR_DEBUG_EVENT");
    return NaviErrors::GENERIC_ERROR;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Attempts to changes the execution status of the primary thread.
 *
 * @param execStatus Desired execution stauts
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::changeExecutionStatus(ULONG execStatus) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT status;
  if (getExecutionStatus() == execStatus) {
    return NaviErrors::SUCCESS;
  }
  status = control->SetExecutionStatus(execStatus);
  if (status != S_OK) {

    log(LOG_ALWAYS,
        "Error: Failed to change execution status to 0x%08x, error code: "
        "0x%08x, GetLastError(): 0x%08x, current status 0x%08x",
        execStatus, status, GetLastError(), getExecutionStatus());
    return NaviErrors::GENERIC_ERROR;
  }
  resumeWaitThread();

  return NaviErrors::SUCCESS;
}

/**
 * Checks whether a custom breakpoint was set on the given address.
 *
 * @param addressString The address to be checked.
 *
 * @return Boolean value indicating whether a custom breakpoint was set on the
 * given address or not.
 */
bool WinDbgSystem::hasCustomBreakpoint(std::string addressString) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  return (originalBytes.find(addressString) != originalBytes.end());
}

/**
 * Returns true of a breakpoint event is pending.
 * @return Boolean value indicating the presence of a breakpoint event.
 */
bool WinDbgSystem::hasBreakpointEvent() {
  bool retval;
  EXCEPTION_RECORD64* er = eventCallbacks->getLastExceptionUnhandled();
  if (er == NULL) {
    retval = false;
  } else if (er->ExceptionCode == EXCEPTION_BREAKPOINT) {
    retval = true;
  } else {
    retval = false;
  }

  eventCallbacks->setLastExceptionUnhandled();
  return retval;
}

/**
 * Resumes the thread with the given thread ID.
 *
 * @param tid The thread ID of the thread.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::resumeThread(unsigned int tid) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (getExecutionStatus() == DEBUG_STATUS_GO) {
    return NaviErrors::SUCCESS;
  }

  if (eventCallbacks->hasUnhandledException() == true) {
    return processDebugEvent(WDBG_EXCEPTION);
  }

  return changeExecutionStatus(DEBUG_STATUS_GO);
}
/**
 * This function is a workaround to ensure a breakpoints gets hit right after a
 * single step took place
 *
 * @param tid The thread ID of the thread.
 * @param address the cpu address after the single step
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */

NaviError WinDbgSystem::resumeAfterStepping(unsigned int threadId,
                                            CPUADDRESS address) {

  if (hasBreakpoint(address, BPX_simple)) {
    removeBreakpoint(getBreakpoint(address, BPX_simple));
    breakpointHit(address, threadId, true /* resume on echo bp */);
  } else if (hasBreakpoint(address, BPX_stepping)) {
    removeBreakpoint(getBreakpoint(address, BPX_stepping));
    breakpointHit(address, threadId, true /* resume on echo bp */);
  } else if (hasBreakpoint(address, BPX_echo)) {
    removeBreakpoint(getBreakpoint(address, BPX_echo));
    breakpointHit(address, threadId, true /* resume on echo bp */);
  } else {
    unsigned resumeResult = resumeProcess();

    if (resumeResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't resume target thread",
                  resumeResult);
      return resumeResult;
    }
  }

  return NaviErrors::SUCCESS;
}

/**
 * This function is responsible for resuming the system.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::resumeProcess() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  //ensure we waited long enough

  if (getExecutionStatus() == DEBUG_STATUS_GO) {
    return NaviErrors::SUCCESS;
  }

  resumeThread (getActiveThread());return
NaviErrors  ::SUCCESS;
}

/**
 * Retrieves the value of the instruction pointer in a given thread.
 *
 * @param tid The thread ID of the thread.
 * @param addr The variable where the value of the instruction pointer is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::getInstructionPointer(unsigned int threadId,
                                              CPUADDRESS& addr) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT Status;
  __int64 tempAddr;
  if (Status = dregister->GetInstructionOffset((PULONG64) & tempAddr) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to get instruction pointer.");
    return NaviErrors::COULDNT_DETERMINE_INSTRUCTION_POINTER;
  }
  // Select the instruction pointer
  addr = (CPUADDRESS) tempAddr;

  return NaviErrors::SUCCESS;
}

/**
 * Retrieves the value stored in the FS register
 *
 * @param fs The variable where the value of the FS register is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::getFSRegisterValue(CPUADDRESS& fs) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  unsigned int offsetFS;

  /**
   * ensure we are really breaked
   */
  if (getExecutionStatus() != DEBUG_STATUS_BREAK) {
    log(LOG_ALWAYS,
        "Error: Failed to read FS register, because debuggee is not halted.");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  CONTEXT context;

  if (advanced3->GetThreadContext(&context, sizeof(CONTEXT)) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to get thread context.");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  offsetFS = context.SegFs;

  ULONG indexGdtr;
  ULONG64 valueFS;
  DEBUG_VALUE valueGdtr;

  if (dregister->GetIndexByName("gdtr", &indexGdtr) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to get index of register GDTR.");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  if (dregister->GetValue(indexGdtr, &valueGdtr) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read value of register GDTR");
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

#ifdef CPU_386
  HRESULT hr;
  ULONG bytesRead;
  if (hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(valueGdtr.I32 + offsetFS),
          &valueFS, sizeof(ULONG64), &bytesRead) !=
      S_OK) {
    log(LOG_ALWAYS,
        "Error: Failed to read FS, GDTR: 0x%08x, GetLastError(): 0x%08x",
        valueGdtr.I32, GetLastError());
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  ULONG valueFSLow, valueFSHigh;
  valueFSLow = valueFS & 0xFFFFFFFF;
  valueFSHigh = valueFS >> 32;

  fs = 0;
  fs += (valueFSLow & 0xFFFF0000) >> 16;   //lowest bit
  fs += (valueFSHigh & 0xFF000000);//higest bit
  fs += (valueFSHigh & 0x000000FF) << 16;//mid bit

#else
#error Unsupported architecture
#endif

  return NaviErrors::SUCCESS;
}

/**
 * Retrieves the Windows Version of the Debuggee
 *
 * @param version The variable where the value of the Windows Version is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::getWindowsVersion(WindowsVersion& version) {

  ULONG bytesRead;
  char systemVersion[256];
  HRESULT hr;
  ULONG KdMinor, KdMajor, Win32Minor, Win32Major, PlatformId;

  /**
   * TODO(timkornau): needs to be improved
   *
   * GetSystemVersionValues is buggy,
   * Windows Vista and Windows 7 both return version 6.0
   * The used workaround parses DEBUG_SYSVERSTR_BUILD string to differentiate
   * between both versions
   */
  if (control->GetSystemVersionValues(&PlatformId, &Win32Major, &Win32Minor,
                                      &KdMajor, &KdMinor) != S_OK) {
    log(LOG_ALL, "Error: Could not get debuggee's windows version.");
    return NaviErrors::GENERIC_ERROR;
  }

  version = UNKNOWN;
  //TODO: verify win2008

  if (Win32Major == 5) {
    if (Win32Minor == 1) {
      version = XP;
    }
    if (Win32Minor == 2) {
      version = WIN2003;
    }
  } else if (Win32Major == 6) {
    hr = control->GetSystemVersionString(DEBUG_SYSVERSTR_BUILD, systemVersion,
                                         sizeof(systemVersion), &bytesRead);

    if (hr != S_OK) {
      log(LOG_ALL, "Error: Could not get debuggee's windows version.");
      return NaviErrors::GENERIC_ERROR;
    }
    if (strstr(systemVersion, "win7") != 0) {
      version = SEVEN;
    } else if (strstr(systemVersion, "longhorn") != 0) {
      version = VISTA;
    } else if (strstr(systemVersion, "vistasp1") != 0) {
      version = WIN2008;
    }
  }
  return NaviErrors::SUCCESS;
}
/**
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::updateActiveThread() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  //get thread id the event occured in
  unsigned int newTid;
  NaviError error = readThreadId(newTid);

  if (error == NaviErrors::SUCCESS) {
    //set thread id as active
    setActiveThread(newTid);
  }

  return error;
}
/**
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::setActiveThread(unsigned int tid) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (knownThreads.find(tid) == knownThreads.end()) {
    knownThreads.insert(tid);
    //notify the base-system
    threadCreated(tid, RUNNING);
  }
  BaseSystem::setActiveThread(tid);
  return NaviErrors::SUCCESS;
}

/**
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readThreadId(unsigned int& threadId) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  const unsigned int offsetKTHREAD = 0x124;

  // ensure we are really breaked

  if (getExecutionStatus() != DEBUG_STATUS_BREAK) {
    log(LOG_ALWAYS, "Error: Failed to get getThreadId DEBUG_STATUS_BREAK");
    //maybe we could ignore this
    //return NaviErrors::GENERIC_ERROR;
  }

  //get fs register
  CPUADDRESS fs;
  getFSRegisterValue(fs);

  //get pointer to KTHREAD structure
  CPUADDRESS pKTHREAD;
  readMemoryData((char*) &pKTHREAD, fs + offsetKTHREAD, sizeof(CPUADDRESS));

  //now get the thread id
  unsigned int tid;
  unsigned int offsetTid;

  //KTHREAD structure layout depends on windows version
  WindowsVersion version;
  getWindowsVersion(version);
  switch (version) {
    case XP:
      offsetTid = 0x1f0;
      break;

    case VISTA:
      offsetTid = 0x210;
      break;

    case SEVEN:
      offsetTid = 0x230;
      break;

    case WIN2003:
      offsetTid = 0x1e8;
      break;

    case WIN2008:
      offsetTid = 0x210;
      break;

    default:
      log(LOG_ALWAYS, "Error: Couldn't get thread id.");
      return NaviErrors::GENERIC_ERROR;
  }
  readMemoryData((char*) &tid, pKTHREAD + offsetTid, sizeof(unsigned int));

  threadId = tid;
  return NaviErrors::SUCCESS;
}

/**
 * Sets the instruction pointer in the target process to a new value.
 *
 * @param tid Thread ID of the target thread.
 * @param address The new value of the instruction pointer.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::setInstructionPointer(unsigned int tid,
                                              CPUADDRESS address) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  HRESULT status;
  ULONG eipIndex;
  DEBUG_VALUE eipValue;

  status = dregister->GetIndexByName("eip", &eipIndex);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set InstructionPointer", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  eipValue.Type = DEBUG_VALUE_INT32;
  eipValue.I32 = address;
  status = dregister->SetValue(eipIndex, &eipValue);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set InstructionPointer", status);
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Fills a given register container structure with information about the
 * current values of the CPU registers.
 *
 * @param registers The register information structure.
 * @param tid The thread ID from which the data is read.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readRegisters(RegisterContainer& registers) {
  HRESULT status;
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  startTimeMeasurement();

  unsigned int tid = getActiveThread();
  Thread thread(tid, SUSPENDED);

  if (getExecutionStatus() != DEBUG_STATUS_BREAK) {
    log(LOG_VERBOSE,
        "Warning: trying to read registers, while debuggee is not halted");
    registers.addThread(thread);

    return NaviErrors::SUCCESS;
  }

#ifdef CPU_386
  ULONG eaxIndex, ebxIndex, ecxIndex, edxIndex, esiIndex, ediIndex, espIndex,
  ebpIndex, eipIndex, eflagsIndex;
  DEBUG_VALUE eaxValue, ebxValue, ecxValue, edxValue, esiValue, ediValue,
  espValue, ebpValue, eipValue, eflagsValue;

  status = dregister->GetIndexByName("eax", &eaxIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("ebx", &ebxIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("ecx", &ecxIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("edx", &edxIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("esi", &esiIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("edi", &ediIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("esp", &espIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("ebp", &ebpIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("eip", &eipIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetIndexByName("efl", &eflagsIndex);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  status = dregister->GetValue(eaxIndex, &eaxValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(ebxIndex, &ebxValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(ecxIndex, &ecxValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(edxIndex, &edxValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(esiIndex, &esiValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(ediIndex, &ediValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(espIndex, &espValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(ebpIndex, &ebpValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(eipIndex, &eipValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(eflagsIndex, &eflagsValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  thread.registers.push_back(
      makeRegisterValue("EAX", zylib::zycon::toHexString(eaxValue.I32),
          readPointedMemory(eaxValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("EBX", zylib::zycon::toHexString(ebxValue.I32),
          readPointedMemory(ebxValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("ECX", zylib::zycon::toHexString(ecxValue.I32),
          readPointedMemory(ecxValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("EDX", zylib::zycon::toHexString(edxValue.I32),
          readPointedMemory(edxValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("ESI", zylib::zycon::toHexString(esiValue.I32),
          readPointedMemory(esiValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("EDI", zylib::zycon::toHexString(ediValue.I32),
          readPointedMemory(ediValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("ESP", zylib::zycon::toHexString(espValue.I32),
          readPointedMemory(espValue.I32), false, true));
  thread.registers.push_back(
      makeRegisterValue("EBP", zylib::zycon::toHexString(ebpValue.I32),
          readPointedMemory(ebpValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("EIP", zylib::zycon::toHexString(eipValue.I32),
          readPointedMemory(eipValue.I32), true, false));
  thread.registers.push_back(
      makeRegisterValue("EFLAGS", zylib::zycon::toHexString(eflagsValue.I32)));
  thread.registers.push_back(
      makeRegisterValue("CF", zylib::zycon::toHexString(eflagsValue.I32 & 1)));
  thread.registers.push_back(makeRegisterValue(
          "PF", zylib::zycon::toHexString((eflagsValue.I32 >> 2) & 1)));
  thread.registers.push_back(makeRegisterValue(
          "AF", zylib::zycon::toHexString((eflagsValue.I32 >> 4) & 1)));
  thread.registers.push_back(makeRegisterValue(
          "ZF", zylib::zycon::toHexString((eflagsValue.I32 >> 6) & 1)));
  thread.registers.push_back(makeRegisterValue(
          "SF", zylib::zycon::toHexString((eflagsValue.I32 >> 7) & 1)));
  thread.registers.push_back(makeRegisterValue(
          "OF", zylib::zycon::toHexString((eflagsValue.I32 >> 11) & 1)));

#else
#error Unsupported architecture
#endif

  registers.addThread(thread);

  log(LOG_ALL, "Read all registers in %dms", endTimeMeasurement());

  return NaviErrors::SUCCESS;
}

std::vector<char> WinDbgSystem::readPointedMemory(CPUADDRESS address) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  unsigned int currentSize = 128;

  //only call this function if we are not running
  if (getExecutionStatus() != DEBUG_STATUS_BREAK) {
    return std::vector<char>();
  }
  while (currentSize != 0) {
    std::vector<char> memory(currentSize, 0);

    if (readMemoryData(&memory[0], address, memory.size())
        == NaviErrors::SUCCESS) {
      return memory;
    }

    currentSize /= 2;
  }

  return std::vector<char>();
}

/**
 * Updates the value of a given register in a given thread.
 *
 * @param tid The thread ID of the thread.
 * @param index The index of the register.
 * @param value The new value of the register.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::setRegister(unsigned int tid, unsigned int index,
                                    CPUADDRESS address) {
  ULONG registerIndex;
  DEBUG_VALUE registerValue;
  HRESULT status;
  log(LOG_ALL, "Entering %s", __FUNCTION__);
#ifdef CPU_386
  switch (index) {
    case 0:
    status = dregister->GetIndexByName("eax", &registerIndex);
    break;
    case 1:
    status = dregister->GetIndexByName("ebx", &registerIndex);
    break;
    case 2:
    status = dregister->GetIndexByName("ecx", &registerIndex);
    break;
    case 3:
    status = dregister->GetIndexByName("edx", &registerIndex);
    break;
    case 4:
    status = dregister->GetIndexByName("esi", &registerIndex);
    break;
    case 5:
    status = dregister->GetIndexByName("edi", &registerIndex);
    break;
    case 6:
    status = dregister->GetIndexByName("esp", &registerIndex);
    break;
    case 7:
    status = dregister->GetIndexByName("ebp", &registerIndex);
    break;
    case 8:
    status = dregister->GetIndexByName("eip", &registerIndex);
    break;
    case 10:
    status = dregister->GetIndexByName("cf", &registerIndex);
    break;
    case 11:
    status = dregister->GetIndexByName("pf", &registerIndex);
    break;
    case 12:
    status = dregister->GetIndexByName("af", &registerIndex);
    break;
    case 13:
    status = dregister->GetIndexByName("zf", &registerIndex);
    break;
    case 14:
    status = dregister->GetIndexByName("sf", &registerIndex);
    break;
    case 15:
    status = dregister->GetIndexByName("of", &registerIndex);
    break;

    default:
    return NaviErrors::INVALID_REGISTER_INDEX;
  }
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to set Register", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  registerValue.Type = DEBUG_VALUE_INT32;
  registerValue.I32 = address;
  status = dregister->SetValue(registerIndex, &registerValue);
  if (status != S_OK) {
    log(LOG_VERBOSE, "Error: Failed to set Register", status);
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }
#elif CPU_AMD64
#error not implemented
#else
#error Unknown architecture
#endif

  return NaviErrors::SUCCESS;
}

NaviError WinDbgSystem::getTrapFlag(char& value) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT status;

  ULONG tfRegister;
  if ((status = dregister->GetIndexByName("tf", &tfRegister)) != S_OK) {
    log(LOG_ALWAYS, "Error: Could not get index of register TF. Code: 0x%08x",
        status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  DEBUG_VALUE dv;
  if ((status = dregister->GetValue(tfRegister, &dv)) != S_OK) {
    log(LOG_ALWAYS, "Error: Could not get TF register. Code: 0x%08x", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  value = (char) dv.I8;

  return NaviErrors::SUCCESS;
}

NaviError WinDbgSystem::getKThreadAddress(ULONG processor, CPUADDRESS& addr) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT status;
  ULONG64 buffer;

  if ((status = dataspaces4->ReadProcessorSystemData(processor,
                                                     DEBUG_DATA_KTHREAD_OFFSET,
                                                     &buffer, sizeof(buffer),
                                                     NULL)) != S_OK) {
    log(LOG_ALWAYS,
        "Error: Could not get address of KTHREAD structure. Code: 0x%08x",
        status);
    return NaviErrors::COULDNT_FIND_DATA;
  }
#ifdef CPU_386
  addr = (CPUADDRESS)(buffer & 0xFFFFFFFF);
#elif CPU_AMD64
  addr = buffer;
#else
#error not implemented
#endif
  return NaviErrors::SUCCESS;
}

NaviError WinDbgSystem::setTrapFlag(BOOL value) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT status;
  CONTEXT context;

  if ((status = advanced3->GetThreadContext(&context, sizeof(context)))
      != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set thread context.");
  }

  context.EFlags |= 0x100;

  if ((status = advanced3->SetThreadContext(&context, sizeof(context)))
      != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set thread context.");
  }

  if ((status = advanced3->GetThreadContext(&context, sizeof(context)))
      != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to set thread context.");
  }
  log(LOG_VERBOSE, "Info: EFlags: 0x%08x", context.EFlags);

  return NaviErrors::SUCCESS;
}

/**
 * Given a start address, this function returns the first and last offset of the
 * memory region the start address belongs to.
 *
 * @param start The start address.
 * @param from The first offset of the memory region.
 * @param to The last offset of the memory region.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                       CPUADDRESS& to) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  ULONG64 addr;
  ULONG size;

  dataspaces4->GetValidRegionVirtual(start, 0, &addr, &size);

  from = (CPUADDRESS) addr;
  to = (CPUADDRESS) addr + size;

  return size != 0 ? NaviErrors::SUCCESS : NaviErrors::NO_VALID_MEMORY;
}
/**
 * Changes the process context of the debugger to the context of "winlogon.exe"
 * This enables the paging of all modules, e.g. win32k.sys
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::updateAddressSpace() {
  HRESULT hr;
  /**
   * even though SetImplicitProcessDataOffset needs KPROCESS,
   * the pointer to EPROCESS is ok, as the pointer to EPROCESS points to the
   * beginning of KPROCESS
   */

  if ((hr = systemObjects4->SetImplicitProcessDataOffset(pWinlogonEprocess))
      != S_OK) {
    log(LOG_ALWAYS, "Error: SetImplicitProcessDataOffset failed: %x", hr);
    return NaviErrors::GENERIC_ERROR;
  }
  return NaviErrors::SUCCESS;

}
/**
 * Initialize the address of the winlogon process
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::initWinlogonAddress() {
  ULONG PsActiveProcessHead;
  ULONG pValue;
  ULONG pNext;
  ULONG pEPROCESS;
  char Buffer64[8];
  char Buffer32[4];
  char imageName[16];
  ULONG bytesRead;
  HRESULT hr;
  unsigned int offsetImageName;
  unsigned int offsetActiveProcessLinks;
  WindowsVersion version;
  getWindowsVersion(version);
  switch (version) {
    case XP:
      offsetActiveProcessLinks = 0x88;
      offsetImageName = 0x174;
      break;

    case VISTA:
      offsetActiveProcessLinks = 0xa0;
      offsetImageName = 0x14C;
      break;

    case SEVEN:
      offsetActiveProcessLinks = 0xb8;
      offsetImageName = 0x16c;
      break;

    case WIN2003:
      offsetActiveProcessLinks = 0x98;
      offsetImageName = 0x164;
      break;

    case WIN2008:
      offsetActiveProcessLinks = 0xa0;
      offsetImageName = 0x14c;
      break;

    default:
      log(LOG_ALWAYS,
          "Error: Couldn't get Windows Version in initWinlogonAddress");
      return NaviErrors::GENERIC_ERROR;
  }

  //get address of PsActiveProcessHead
  if ((hr = dataspaces4->ReadDebuggerData(DEBUG_DATA_PsActiveProcessHeadAddr,
                                          Buffer64, sizeof(Buffer64),
                                          &bytesRead)) != S_OK) {
    log(LOG_ALWAYS, "Error: ReadDebuggerData failed: %x", hr);
    return NaviErrors::GENERIC_ERROR;
  }

  PsActiveProcessHead = BIN_TO_ULONG(Buffer64);

  if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(PsActiveProcessHead),
                                     Buffer32, sizeof(Buffer32), &bytesRead))
      != S_OK) {
    log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }

  pValue = BIN_TO_ULONG(Buffer32);

  pNext = pValue;

  /**
   * iterate over the link list which is located at PsActiveProcessHead
   * every element is a EPROCESS
   */
  while (pNext != PsActiveProcessHead) {
    //pNext points to +offsetActiveProcessLinks ActiveProcessLinks

    pEPROCESS = pNext - offsetActiveProcessLinks;

    if ((hr = dataspaces4->ReadVirtual(
        SAVE_SIGN_EXTEND(pEPROCESS + offsetImageName), imageName,
        sizeof(imageName), &bytesRead)) != S_OK) {
      log_executionstatus();
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }

    if (strcmp("winlogon.exe", imageName) == 0) {
      //save current EPROCESS as global variable
      pWinlogonEprocess = pEPROCESS;
      return NaviErrors::SUCCESS;
    }

    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pNext), Buffer32,
                                       sizeof(Buffer32), &bytesRead)) != S_OK) {
      log_executionstatus();
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }
    pNext = BIN_TO_ULONG(Buffer32);

  }
  //no winlogon EPROCESS was found, return with error

  return NaviErrors::GENERIC_ERROR;
}

/**
 * Returns a list of all memory regions that are available in the target process.
 *
 * @param addresses The memory map is written into this list.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::getMemmap(std::vector<CPUADDRESS>& addresses) {
  ULONG cr4Index;
  DEBUG_VALUE cr4Value;
  HRESULT status;

  //0 for non Page Size Extension
  int PSE = 0;
  //0 for non Physical Address Extension
  int PAE = 0;

  unsigned int cr4;

  NaviError naviEr;

  log(LOG_ALL, "Entering %s", __FUNCTION__);
  log(LOG_ALWAYS, "please be patient while the memory map is loaded");

  /**
   * initialize the pointer to the winlogon process, to always include
   * win23k.sys
   */

  naviEr = initWinlogonAddress();

  if (naviEr != NaviErrors::SUCCESS) {
    log(LOG_ALWAYS, "Error: initialize winlogon address");
    return NaviErrors::GENERIC_ERROR;
  }

  /* get Control Register 4 for paging settings */
  status = dregister->GetIndexByName("cr4", &cr4Index);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  status = dregister->GetValue(cr4Index, &cr4Value);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  cr4 = cr4Value.I32;
  /* pse is bit 4 */
  PSE = (cr4 & 16) >> 4;
  /* pae is bit 5 */
  PAE = (cr4 & 32) >> 5;

  //use physical address extension

  if (PAE == 1) {
    naviEr = parsePAE(addresses, PSE);
    if (naviEr != NaviErrors::SUCCESS) {
      return naviEr;
    }

  }
  //use non pae
  else {
    naviEr = parseNonPAE(addresses, PSE);
    if (naviEr != NaviErrors::SUCCESS) {
      return naviEr;
    }

  }
  return NaviErrors::SUCCESS;
}

/**
 * This function is responsible for parsing the pae structure
 *
 * @param addresses The memory map is written into this list.
 *
 * @param PSE The flag if PSE is active or not
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::parsePAE(std::vector<CPUADDRESS>& addresses, int PSE) {
  ULONG cr3Index;
  CPUADDRESS addr;
  DEBUG_VALUE cr3Value;
  HRESULT status;
  ULONG bytesRead;
  unsigned int cr3;

  /* this flag is set per block */
  int PSflag;
  int validPage;
  char pageDirectoryPointerTable[32 + 1];
  char pageDirectoryTable[512 * 8 + 1];
  char pageTable[512 * 8 + 1];

  unsigned int PageDirectroyPointerIndex;
  unsigned int PageDirectroyIndex;
  unsigned int pageTableIndex;

  unsigned int consecutiveRegions = 0;

  ULONG64 pageDirectoryPointerEntry; /*64 bit value*/
  unsigned int pageDirectoryPointerEntryAddr;
  //	unsigned int pageDirectoryPointerEntryFlags;

  ULONG64 pageDirectoryEntry; /*64 bit value*/
  unsigned int pageDirectoryEntryAddr;
  unsigned int pageDirectoryEntryFlags;

  ULONG64 pageTableEntry; /*64 bit value*/
  unsigned int pageTableEntryAddr;
  //	unsigned int pageTableEntryFlags;

  unsigned int smallPageSize = 0x1000;    //4kb
  unsigned int largePageSize = 0x200000;  //2mb

  /**
   * get CR3, The value of Control Register 3 is
   * important for the Page Directory Table
   */

  status = dregister->GetIndexByName("cr3", &cr3Index);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers, status: %08x", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(cr3Index, &cr3Value);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers, status: %08x", status);
    this->log_executionstatus();
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  log(LOG_VERBOSE, "Info: page directory table at %x", cr3Value);
  cr3 = cr3Value.I32;

  /* get page-directory-pointer table offset */

  if ((status = dataspaces4->ReadPhysical(SAVE_SIGN_EXTEND(cr3),
                                          pageDirectoryPointerTable, 32,
                                          &bytesRead)) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read  page-directory-pointer table "
        "offset, status: %08x",
        status);
    this->log_executionstatus();
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  //there are 4 page directories
  for (PageDirectroyPointerIndex = 0; PageDirectroyPointerIndex <= 3;
      PageDirectroyPointerIndex++) {
    /* get Page Direcectory Pointer Entry*/
    pageDirectoryPointerEntry = BIN_TO_ULONG64(pageDirectoryPointerTable +
        PageDirectroyPointerIndex * sizeof(ULONG64));

    /**
     * Page Directory Pointer Entry:
     * bit 0-11 ignore
     * bit (m-1):12 physical address of the page directory table
     */

    pageDirectoryPointerEntryAddr = pageDirectoryPointerEntry
        & 0xFFFFFFFFFFFFF800;  //get physical address

    /*get page directory table*/
    if ((status = dataspaces4->ReadPhysical(
        SAVE_SIGN_EXTEND(pageDirectoryPointerEntryAddr +
            PageDirectroyPointerIndex * 8),
        pageDirectoryTable, 512 * 8, &bytesRead)) != S_OK) {
      log(LOG_ALWAYS,
          "Error: Failed to read page directory table, status: %08x", status);
      this->log_executionstatus();
      return NaviErrors::COULDNT_READ_MEMORY;
    }

    /**
     * iterate over all Page Directories in Page Directory table
     * there exists 1024 Pag Directories
     */
    for (PageDirectroyIndex = 0; PageDirectroyIndex <= 0x1ff;
        PageDirectroyIndex++) {
      /*get Page Directory Entry*/
      pageDirectoryEntry = BIN_TO_ULONG64(pageDirectoryTable +
          PageDirectroyIndex * sizeof(ULONG));

      pageDirectoryEntryFlags = pageDirectoryEntry & 0x3FF;
      pageDirectoryEntryAddr = (unsigned int) (pageDirectoryEntry
          & 0xFFFFFFFFFFFFF800);

      /*first check if empty Page Directory*/
      if (pageDirectoryEntryAddr != 0) {
        /**
         * Page Directory Entry
         * bit 0-6 flags
         * bit 7 page size flag
         * bit 8-11 ignore
         * bit (M�1):12 physical address of page table
         */

        if (PSE == 1) {
          PSflag = (pageDirectoryEntryFlags & 128) >> 7;
        } else {
          PSflag = 0;
        }
        /* large page 2mb */
        if (PSflag == 1) {
          /**
           * Page Directory Entry that represents 2mb page bit (M�1):21
           * Physical address of the 2-MByte page referenced by this entry
           * bit 63 if 1 execute-disable
           */

          addr = PageDirectroyPointerIndex << 30;
          addr += PageDirectroyIndex << 21;
          addresses.push_back(addr);
          addresses.push_back(addr + largePageSize - 1);
          /**
           * log(LOG_VERBOSE, "found large:%x",addr);
           * log(LOG_VERBOSE, "contains:%x",pageDirectoryEntryAddr);
           */

        } /* small page 4kb */
        else {
          /* get Page Table */
          if ((status = dataspaces4->ReadPhysical(
              SAVE_SIGN_EXTEND(pageDirectoryEntryAddr), pageTable,
              512 * sizeof(ULONG64), &bytesRead)) != S_OK) {
            log(LOG_ALWAYS, "Error: Failed to read Page Table, status: %08x",
                status);
            return NaviErrors::COULDNT_READ_MEMORY;
          }
          /* iterate over all pages in Page table there are 1024 pages */

          for (pageTableIndex = 0; pageTableIndex <= 0x1ff; pageTableIndex++) {
            /* get Page Table Entry */
            pageTableEntry =
                BIN_TO_ULONG64(pageTable + pageTableIndex * sizeof(ULONG64));
            /**
             * Page Table Entry:
             * bit 0 must be 1 to be a valid page
             * bit 1-11 ignore flags
             * bit (M�1):12 physical address of 4-KByte aligned page table entry
             * bit 63 if 1 execute-disable
             */

            /* first check if page entry is empty */
            if (pageTableEntry != 0) {
              validPage = pageTableEntry & 1;
              pageTableEntryAddr = (unsigned int) (pageTableEntry
                  & 0x7FFFFFFFFFFFF000);

              if ((pageTableEntryAddr != 0xCCCCC000)) {
                ++consecutiveRegions;
                addr = PageDirectroyPointerIndex << 30;
                addr += PageDirectroyIndex << 21;
                addr += pageTableIndex << 12;
                if (consecutiveRegions == 1) {
                  /**
                   * log(LOG_VERBOSE, "Found memory section between %X and %X",
                   * addr,addr + smallPageSize-1);
                   * log(LOG_VERBOSE, "contains:%x",pageTableEntryAddr);
                   */
                  addresses.push_back(addr);
                  addresses.push_back(addr + smallPageSize - 1);  //4kb
                } else {
                  /** log(LOG_VERBOSE, "Extending memory section to %X",
                   * addresses[addresses.size() - 1] + smallPageSize);
                   * log(LOG_VERBOSE, "contains:%x",pageTableEntryAddr);
                   */
                  addresses[addresses.size() - 1] += smallPageSize;
                }
              } else {
                consecutiveRegions = 0;
              }
            } else {
              consecutiveRegions = 0;
            }
          }
        }
      }
    }
  }
  return NaviErrors::SUCCESS;
}
/**
 * This function is responsible for parsing the non pae structure
 *
 * @param addresses The memory map is written into this list.
 *
 * @param PSE The flag if PSE is active or not
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::parseNonPAE(std::vector<CPUADDRESS>& addresses,
                                    int PSE) {

  ULONG cr3Index;
  CPUADDRESS addr;
  DEBUG_VALUE cr3Value;
  HRESULT status;
  ULONG bytesRead;

  int PSflag = 0;
  unsigned int cr3;
  unsigned int nonpaesize = 4092;
  char pageDirectory[4092 + 1];

  char pageTable[4092 + 1];

  unsigned int pageDirectoryCounter;
  unsigned int pageTableCounter;  //page table counter

  unsigned int consecutiveRegions = 0;
  unsigned int directoryEntry, tableEntry, tempEntry;
  unsigned int directoryFlag;

  unsigned int smallPageSize = 0x1000;    //4kb
  unsigned int largePageSize = 0x400000;  //4mb

  /* get CR3 */
  /* The value of Control Register 3 is important for the Page Directory Table
   */

  status = dregister->GetIndexByName("cr3", &cr3Index);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  status = dregister->GetValue(cr3Index, &cr3Value);
  if (status != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read registers", status);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  log(LOG_VERBOSE, "Debug: page directory table at %x", cr3Value);
  cr3 = cr3Value.I32;

  //get the page directory entries
  //get only half of the table (userspace can be ignored)
  if ((status = dataspaces4->ReadPhysical(
      SAVE_SIGN_EXTEND(cr3 + (nonpaesize / 2)), pageDirectory, (nonpaesize / 2),
      &bytesRead)) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to read memory", status);
    return NaviErrors::COULDNT_READ_MEMORY;
  }
  //copy kernelspace to right location
  memcpy(pageDirectory + (nonpaesize / 2), pageDirectory, nonpaesize / 2);

  //loop over directory entries
  for (pageDirectoryCounter = 511; pageDirectoryCounter <= 0x3ff;
      pageDirectoryCounter++) {
    directoryEntry =
        BIN_TO_ULONG(&pageDirectory[pageDirectoryCounter * sizeof(ULONG)]);
    if (directoryEntry != 0) {
      //found page table
      directoryFlag = directoryEntry & 0x00000fff;  //get flags
      //check if page size extention
      if (PSE == 1) {
        PSflag = (directoryFlag & 128) >> 7;  //page size flag is bit 7
      }

      //large page size
      if (PSflag == 1) {
        addr = pageDirectoryCounter << 22;  //first part are the upper 10bits

        addresses.push_back(addr);
        addresses.push_back(addr + largePageSize - 1);
      }

      //normal page size
      else {
        directoryEntry &= 0xfffff000;  //skip bytes that represents flags
        dataspaces4->ReadPhysical(SAVE_SIGN_EXTEND(directoryEntry), pageTable,
                                  nonpaesize, &bytesRead);
        //todo:error checking ;)
        consecutiveRegions = 0;
        for (pageTableCounter = 0; pageTableCounter <= 0x3ff;
            pageTableCounter++) {

          tableEntry =
              BIN_TO_ULONG(&pageTable[pageTableCounter * sizeof(ULONG)]);

          tempEntry = tableEntry & 0xfffff000;

          addr = pageDirectoryCounter << 22;  //first part are the upper 10bits
          addr += pageTableCounter << 12;  //second part are the second 10bits
          //this can be found in the intel manuel

          //ignore addresses under 0x80000000
          if ((tableEntry != 0xCCCCC000) & (tempEntry != 0)
              & (addr > 0x80000000)) {
            ++consecutiveRegions;

            if (consecutiveRegions == 1) {
              //log(LOG_VERBOSE, "Found memory section between %X and %X",
              //addr,addr + 0xFFF);
              addresses.push_back(addr);
              addresses.push_back(addr + smallPageSize - 1);  //4kb
            } else {
              //log(LOG_VERBOSE, "Extending memory section to %X",
              //addresses[addresses.size() - 1] + 0x1000);
              addresses[addresses.size() - 1] += smallPageSize;
            }
          } else {
            consecutiveRegions = 0;
          }
        }
      }
    }
  }
  return NaviErrors::SUCCESS;
}
/**
 * Fills a buffer with memory data from the current process.
 *
 * @param buffer The buffer to fill.
 * @param address The address from where the memory is read.
 * @param size Number of bytes to read.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readMemoryData(char* buffer, CPUADDRESS address,
                                       CPUADDRESS size) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  //only call this function if we are not running
  if (getExecutionStatus() != DEBUG_STATUS_BREAK) {
    //return NaviErrors::COULDNT_READ_MEMORY;
    return NaviErrors::SUCCESS;
  }

  DWORD startTime = GetTickCount();

  ULONG64 addr = SAVE_SIGN_EXTEND(address);
  ULONG64 validBase;
  ULONG validSize;
  if (dataspaces4->GetValidRegionVirtual(addr, size, &validBase, &validSize)
      != S_OK) {
#ifdef CPU_AMD64
    log(LOG_ALWAYS,
        "Error: Couldn't validate read access of byte at address %016x",
        address);
#else
    log(LOG_ALWAYS,
        "Error: Couldn't validate read access of byte byte at address %08x",
        address);
    return NaviErrors::COULDNT_READ_MEMORY;
#endif
  }

  if (!((addr >= validBase) && ((addr + size) <= (validBase + validSize)))) {
#ifdef CPU_AMD64
    log(LOG_ALL, "No access possible to byte at address %016x", address);
#else
    log(LOG_ALL, "No access possible to mem at address %08x, took %d ms",
        address, GetTickCount() - startTime);
#endif
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  ULONG bytesRead;
  HRESULT status;
  if ((status = dataspaces4->ReadVirtual(addr, buffer, size, &bytesRead))
      != S_OK) {
#ifdef CPU_AMD64
    log(LOG_ALWAYS,
        "Error: Failed to read %d bytes from 0x%016x with error code 0x%08x",
        size, address);
#else
    log(LOG_ALWAYS, "Error: Failed to read %d bytes from 0x%08x with error "
        "code 0x%08x, GetLastError(): 0x%08x",
        size, address, status, GetLastError());
    log_executionstatus();
#endif
  }

  log(LOG_ALL, "Read %d bytes in %dms", size, GetTickCount() - startTime);

  return
      size == bytesRead ? NaviErrors::SUCCESS : NaviErrors::COULDNT_READ_MEMORY;
}
/**
 * Logs the current execution status for debugging purpose
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::log_executionstatus() {
  ULONG execStatus = getExecutionStatus();
  switch (execStatus) {
    case DEBUG_STATUS_BREAK:
      log(LOG_VERBOSE, "Execution Status: DEBUG_STATUS_BREAK");
      break;
    case DEBUG_STATUS_GO:
      log(LOG_VERBOSE, "Execution Status: DEBUG_STATUS_GO");
      break;

    default:
      log(LOG_VERBOSE, "Execution Status: %x ", execStatus);
  }

  return NaviErrors::SUCCESS;;
}
/**
 * Fills a buffer with a list of all current running modules
 *
 * @param processList ProcessListContainer
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readProcessList(ProcessListContainer& processList) {
  //todo: this function is not 64 bit save
  HRESULT hr;
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  ULONG bytesRead;
  char rBuffer[8];
  char unicodeNameBuffer[129];
  ULONG PsLoadedModuleList;
  ULONG pNext;
  ULONG pBaseAddr;
  ULONG iSize;
  ULONG64 pDriverName;
  ULONG cbAnsi, cCharacters;
  char ansiNameBuffer[1024];
  ULONG pValue;

  unsigned int pid;

  //ensure target is halted
  halt();

  if ((hr = dataspaces4->ReadDebuggerData(DEBUG_DATA_PsLoadedModuleListAddr,
                                          rBuffer, 8, &bytesRead)) != S_OK) {
    log_executionstatus();
    log(LOG_ALWAYS, "Error: ReadDebuggerData failed: %x", hr);
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }

  PsLoadedModuleList = BIN_TO_ULONG(rBuffer);

  //point to first entry
  if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(PsLoadedModuleList),
                                     rBuffer, 4, &bytesRead)) != S_OK) {
    log_executionstatus();
    log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }
  pValue = BIN_TO_ULONG(rBuffer);

  if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pValue), rBuffer, 4,
                                     &bytesRead)) != S_OK) {
    log_executionstatus();
    log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }
  pNext = BIN_TO_ULONG(rBuffer);

  pid = 0;

  while (pNext != PsLoadedModuleList) {
    //+0x18 BaseAddress
    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pValue + 0x18), rBuffer,
                                       4, &bytesRead)) != S_OK) {
      log_executionstatus();
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }
    pBaseAddr = BIN_TO_ULONG(rBuffer);

    //+0x20 SizeOfImage
    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pValue + 0x20), rBuffer,
                                       4, &bytesRead)) != S_OK) {
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }

    iSize = BIN_TO_ULONG(rBuffer);

    //+0x30 DriverName
    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pValue + 0x30), rBuffer,
                                       4, &bytesRead)) != S_OK) {
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }

    pDriverName = BIN_TO_ULONG(rBuffer);

    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pDriverName),
                                       unicodeNameBuffer, 128, &bytesRead))
        != S_OK) {
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }
    // Convert to ANSI.

    cCharacters = wcslen((wchar_t*) unicodeNameBuffer) + 1;
    cbAnsi = cCharacters * 2;
    WideCharToMultiByte(CP_ACP, 0, (wchar_t*) unicodeNameBuffer, cCharacters,
                        (LPSTR) & ansiNameBuffer, cbAnsi, NULL, NULL);

    //set fake pid
    ProcessDescription process(pid++, ansiNameBuffer);

    //save module information
    //TODO: get path  +0x024 FullDllName      : _UNICODE_STRING
    //					0x28
    char path[10] = "c:\\";

    Module m(ansiNameBuffer, path, pBaseAddr, iSize);
    this->kernelModules.push_back(m);
    processList.push_back(process);

    pValue = pNext;
    //read pointer to next strcuture
    if ((hr = dataspaces4->ReadVirtual(SAVE_SIGN_EXTEND(pValue), rBuffer, 4,
                                       &bytesRead)) != S_OK) {
      log(LOG_ALWAYS, "Error: ReadVirtual failed: %x", hr);
      return NaviErrors::COULDNT_GET_PROCESSLIST;
    }

    pNext = BIN_TO_ULONG(rBuffer);
  }
  return NaviErrors::SUCCESS;
}

/**
 * Finds out whether debug events occured in the target process.
 * @param eventIndex internal index of the event.
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::processDebugEvent(WinDbgEvent eventIndex) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  nextContinue = DBG_CONTINUE;

  switch (eventIndex) {
    case WDBG_EXCEPTION: {
      // get last exception
      EXCEPTION_RECORD64* ex = eventCallbacks->getLastException();

      // get corresponding thread id
      // eventCallbacks->getLastExceptionThreadId();
      unsigned int tid = getActiveThread();

      log(LOG_VERBOSE, "Info: Got exception in thread: %d", tid);

      switch (ex->ExceptionCode) {
        case EXCEPTION_BREAKPOINT: {
          // EnterCriticalSection(&csException);

          // check if this is the first breakpoint exception event in the current
          // session...
          if (firstBreakpointException == true) {
            // ...if so, we are sitting on the std interrupt addr -> save it!
            firstBreakpointException = false;
            addrInterrupt = (CPUADDRESS) ex->ExceptionAddress;
            resumeThread(tid);
          } else {
            std::string addressString = cpuAddressToString(
                (CPUADDRESS) ex->ExceptionAddress);
            // check if legit breakpoint was hit
            if (hasCustomBreakpoint(addressString)) {
              readThreadId(tid);
              breakpointHit((CPUADDRESS) ex->ExceptionAddress, tid,
                            true /* resume on echo bp */);
            }
            // ...if not, check if a bp was hit that had been just removed
            // (race condition...)
            else if ((CPUADDRESS) ex->ExceptionAddress == addrInterrupt) {
              resumeThread(tid);
            }
            //check if breakpoint just has been removed
            else if (removedBreakpoints.find((CPUADDRESS) ex->ExceptionAddress)
                != removedBreakpoints.end()) {
              //check if we're sitting on that breakpoint
              if (getExecutionStatus() == DEBUG_STATUS_BREAK) {
                //if so, resume
                resumeThread(tid);
              }
            } else {
              log(LOG_ALWAYS, "Error: Got unhandled breakpoint exception in "
                  "processDebugEvent(), address: 0x%08x",
                  (CPUADDRESS) ex->ExceptionAddress);
              nextContinue = DBG_EXCEPTION_NOT_HANDLED;
              exceptionRaised(tid, (CPUADDRESS) ex->ExceptionAddress,
                              ex->ExceptionCode);
            }
          }

          //LeaveCriticalSection(&csException);
          return NaviErrors::SUCCESS;
        }
        case EXCEPTION_SINGLE_STEP: {
          log(LOG_ALL, "Info: Got single-step exception at address 0x%08x.",
              CPUADDRESS(ex->ExceptionAddress));
          alreadyStepped.insert(tid);
          return NaviErrors::SUCCESS;
        }

        default: {
          log(LOG_ALL,
              "Info: Got unhandled exception 0x%08x at address 0x%08x.",
              ex->ExceptionCode, ex->ExceptionAddress);
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(tid, (CPUADDRESS) ex->ExceptionAddress,
                          ex->ExceptionCode);
          return NaviErrors::SUCCESS;
        }
      }
    }
    default: {
      log(LOG_VERBOSE, "Info: Unhandled debug event with index: %d",
          eventIndex);
    }
  }

  return NaviErrors::UNKNOWN_DEBUG_EVENT;
}

/**
 * Finds out whether debug events occured in the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::readDebugEvents() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  unsigned int eventIndex;
  if ((eventIndex = WaitForMultipleObjects(NUM_INTERNAL_DBG_EVENTS,
                                           dbgEventHandles, false, 500))
      != WAIT_TIMEOUT) {
    ResetEvent (eventHandles[ZY_WAIT_FOR_DEBUG_EVENT_RETURNED]);

    return processDebugEvent((WinDbgEvent)(eventIndex - WAIT_OBJECT_0));
  }
  return NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
}

/**
 * Wait for the occurances of any debug event
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::waitForDebugEvent() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  if (WaitForSingleObject(eventHandles[ZY_WAIT_FOR_DEBUG_EVENT_RETURNED],
                          INFINITE) != WAIT_OBJECT_0) {
    return NaviErrors::GENERIC_ERROR;
  }

  ULONG execStatus = getExecutionStatus();
  while (execStatus != DEBUG_STATUS_BREAK) {
    log(LOG_ALL, "Info: Busy waiting in waitForDebugEvent(). Exec status of "
        "debugee is: 0x%08x",
        execStatus);
    Sleep(1);
    execStatus = getExecutionStatus();
  }

  return NaviErrors::SUCCESS;
}

/**
 * Wait for the occurances of any debug event, consumes any event
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::waitForDebugEventConsume() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  NaviError error = waitForDebugEvent();
  if (error == NaviErrors::SUCCESS) {
    unsetDebugEvents();
    return NaviErrors::SUCCESS;
  }
  return error;
}

/**
 * Unsets all debug events
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::unsetDebugEvents() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  for (int i = 0; i < NUM_INTERNAL_DBG_EVENTS; i++) {
    if (ResetEvent(eventHandles[i]) == false) {
      log(LOG_ALL, "Error, failed to reset event %d", i);
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Gets the execution status of the target machine.
 * @return The execution status of the target system.
 */
ULONG WinDbgSystem::getExecutionStatus() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT Status;
  ULONG execStatus;

  if (Status = control->GetExecutionStatus(&execStatus) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to get execution status, error code %08x",
        Status);
  }

  return execStatus;
}

void WinDbgSystem::startTimeMeasurement() {
  startingTimes.push_back(GetTickCount());
  return;
}

DWORD WinDbgSystem::endTimeMeasurement() {
  DWORD time = GetTickCount() - startingTimes.back();
  startingTimes.pop_back();
  return time;
}

/**
 * Starts a temporary halt-phase, saves the current execution status
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::startTempHalt() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  this->log_executionstatus();

  UINT execStatus = getExecutionStatus();
  preHaltExecStatus.push_back(execStatus);

  if (execStatus != DEBUG_STATUS_BREAK) {
    return halt();
  } else {
    return NaviErrors::SUCCESS;
  }
}

/**
 * Ends a temporary halt-phase, restores the execution status, the target had
 * before the corresponding call to startTempHalt()
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::endTempHalt() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);
  this->log_executionstatus();
  NaviError retVal;

  ULONG formerExecStatus = preHaltExecStatus.back();
  if (formerExecStatus == DEBUG_STATUS_GO) {
    retVal = resumeProcess();
  } else {
    retVal = NaviErrors::SUCCESS;
  }

  preHaltExecStatus.pop_back();
  return retVal;

}

/**
 * Halts the debugee
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::halt() {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  HRESULT Status;
  NaviError naviEr;
  ULONG execStatus = getExecutionStatus();
  if (execStatus == DEBUG_STATUS_BREAK) {
    return NaviErrors::SUCCESS;
  }

  EnterCriticalSection (&csHalt);
  DWORD signalStatus = WaitForSingleObject(
      eventHandles[ZY_WAIT_FOR_DEBUG_EVENT_RETURNED], 0);
  LeaveCriticalSection(&csHalt);

  if (signalStatus == WAIT_OBJECT_0) {
    execStatus = getExecutionStatus();
    while (execStatus != DEBUG_STATUS_BREAK) {
      log(LOG_ALL,
          "Info: Busy waiting in halt(). Exec status of debugee is: 0x%08x",
          execStatus);
      Sleep(1);
      execStatus = getExecutionStatus();
    }

    log(LOG_VERBOSE, "Info: Leaving halt() without issueing an interrupt, "
        "because target is already halted");
    return NaviErrors::SUCCESS;
  }
  log(LOG_ALL, "Info: Execution status is 0x%08x, setting interrupt in halt()",
      execStatus);

  //request interrupt
  if ((Status = control->SetInterrupt(DEBUG_INTERRUPT_ACTIVE)) != S_OK) {
    log(LOG_ALWAYS, "Error: Failed to change execution status to "
        "DEBUG_STATUS_BREAK, error code %08x",
        Status);
  }

  waitForDebugEvent();

  /**
   * ensure waitForEventThread is finished
   * this should fix a bug, where to debug must be manually resumed
   */

  unsigned int tid = getActiveThread();

  log(LOG_VERBOSE, "Info: Halted in thread: %d", tid);
  //check if we're standing on the interrupt address
  CPUADDRESS ip;
  getInstructionPointer(0, ip);

  if (ip == this->addrInterrupt) {
    //got the expected interrupt -> consume the event
    unsetDebugEvents();
  } else {
    //we hit something different than the expected interrupt...
    log(LOG_ALL, "Info: Got exception at 0x%08x instead of interrupt in halt()",
        ip);
  }

  //reset removed bps list
  removedBreakpoints.clear();

  //update address space every time we halt
  //check if the variable is already initialized
  if (pWinlogonEprocess != 0) {
    naviEr = updateAddressSpace();

    if (naviEr != NaviErrors::SUCCESS) {
      log(LOG_ALWAYS, "Error: updating address space failed");
      return NaviErrors::GENERIC_ERROR;
    }

  }

  return NaviErrors::SUCCESS;
}
/**
 * Returns a list of the names of the registers of the underlying platform.
 *
 * @return A list of register names.
 */
std::vector<RegisterDescription> WinDbgSystem::getRegisterNames() const {
  std::vector < RegisterDescription > regNames;

#ifdef CPU_386
  RegisterDescription eax("EAX", 4, false);
  RegisterDescription ebx("EBX", 4, false);
  RegisterDescription ecx("ECX", 4, false);
  RegisterDescription edx("EDX", 4, false);
  RegisterDescription esi("ESI", 4, false);
  RegisterDescription edi("EDI", 4, false);
  RegisterDescription ebp("EBP", 4, false);
  RegisterDescription esp("ESP", 4, false);
  RegisterDescription eip("EIP", 4, false);
  RegisterDescription eflags("EFLAGS", 4, false);
  RegisterDescription cf("CF", 0, false);
  RegisterDescription pf("PF", 0, false);
  RegisterDescription af("AF", 0, false);
  RegisterDescription zf("ZF", 0, false);
  RegisterDescription sf("SF", 0, false);
  RegisterDescription of("OF", 0, false);

  regNames.push_back(eax);
  regNames.push_back(ebx);
  regNames.push_back(ecx);
  regNames.push_back(edx);
  regNames.push_back(esi);
  regNames.push_back(edi);
  regNames.push_back(esp);
  regNames.push_back(ebp);
  regNames.push_back(eip);
  regNames.push_back(eflags);
  regNames.push_back(cf);
  regNames.push_back(pf);
  regNames.push_back(af);
  regNames.push_back(zf);
  regNames.push_back(sf);
  regNames.push_back(of);
#elif CPU_AMD64
  RegisterDescription rax("RAX", 8, false);
  RegisterDescription rbx("RBX", 8, false);
  RegisterDescription rcx("RCX", 8, false);
  RegisterDescription rdx("RDX", 8, false);
  RegisterDescription rsi("RSI", 8, false);
  RegisterDescription rdi("RDI", 8, false);
  RegisterDescription rbp("RBP", 8, false);
  RegisterDescription rsp("RSP", 8, false);
  RegisterDescription rip("RIP", 8, false);
  RegisterDescription eflags("EFLAGS", 4, false);

  regNames.push_back(rax);
  regNames.push_back(rbx);
  regNames.push_back(rcx);
  regNames.push_back(rdx);
  regNames.push_back(rsi);
  regNames.push_back(rdi);
  regNames.push_back(rsp);
  regNames.push_back(rbp);
  regNames.push_back(rip);
  regNames.push_back(eflags);
#else
#error Unknown architecture
#endif

  return regNames;
}

/**
 * Returns the maximum size of a memory address of the target machine.
 *
 * @return The maximum size of a memory address of the target machine.
 */
unsigned int WinDbgSystem::getAddressSize() const {
#ifdef CPU_386
  return 32;
#elif CPU_AMD64 || CPU_IA64
  return 64;
#else
#error Unknown architecture
#endif
}

/**
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::suspendThread(unsigned int tid) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  return NaviErrors::SUCCESS;
}
/**
 * Writes Memory to address
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::writeMemory(CPUADDRESS address,
                                    const std::vector<char>& data) {
  return writeBytes(hProcess, address, data);
}

/**
 * Writes Memory to address
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinDbgSystem::writeBytes(HANDLE hProcess, CPUADDRESS offset,
                                   std::vector<char> data) {
  log(LOG_ALL, "Entering %s", __FUNCTION__);

  /* we assume all pages are writeable, maybe check this out later */

  /* Write the byte to the memory */
  if (dataspaces4->WriteVirtual(SAVE_SIGN_EXTEND(offset), &data, sizeof(char),
                                NULL) != S_OK) {
    log(LOG_VERBOSE, "WriteVirtual failed in %s ...", __FUNCTION__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Returns the debugger options that are supported by the debug client.
 *
 * @return The debugger options that are supported by the debug client.
 */
DebuggerOptions WinDbgSystem::getDebuggerOptions() const {
  DebuggerOptions windbgoptions;

  windbgoptions.canDetach = true;
  windbgoptions.canHalt = true;
  windbgoptions.hasStack = false;
  windbgoptions.canMemmap = true;
  windbgoptions.canMultithread = false;
  windbgoptions.canSoftwareBreakpoint = true;
  windbgoptions.canTerminate = false;
  windbgoptions.canValidMemory = true;
  windbgoptions.haltBeforeCommunicating = false;
  //not yet implemented
  windbgoptions.canTraceCount = false;
  windbgoptions.canBreakOnModuleUnload = false;
  windbgoptions.canBreakOnModuleLoad = false;
  control->GetPageSize((PULONG) & windbgoptions.pageSize);

  return windbgoptions;
}
NaviError WinDbgSystem::getFileSystems(
    std::vector<boost::filesystem::path>& roots) const {
  // not yet implemented
  return NaviErrors::SUCCESS;
}

NaviError WinDbgSystem::getSystemRoot(boost::filesystem::path& root) const {
  // not yet implemented
  return NaviErrors::SUCCESS;
}

namespace {
const int NumberOfExceptions = 11;

const DebugException ExceptionsArray[NumberOfExceptions] = { DebugException(
    "Access violation", EXCEPTION_ACCESS_VIOLATION, HALT), DebugException(
    "Illegal instruction", EXCEPTION_ILLEGAL_INSTRUCTION, HALT), DebugException(
    "Privileged instruction", EXCEPTION_PRIV_INSTRUCTION, HALT), DebugException(
    "Integer division by zero", EXCEPTION_INT_DIVIDE_BY_ZERO, HALT),
    DebugException("Integer overflow", EXCEPTION_INT_OVERFLOW, HALT),
    DebugException("Stack overflow", EXCEPTION_STACK_OVERFLOW, HALT),
    DebugException("Guard page", EXCEPTION_GUARD_PAGE, HALT), DebugException(
        "Non-continuable exception", EXCEPTION_NONCONTINUABLE_EXCEPTION, HALT),
    DebugException("Floating point division by zero",
                   EXCEPTION_FLT_DIVIDE_BY_ZERO, HALT), DebugException(
        "Floating point invalid operation", EXCEPTION_FLT_INVALID_OPERATION,
        HALT), DebugException("Invalid handle closed", EXCEPTION_INVALID_HANDLE,
                              HALT) };

DebugExceptionContainer exceptions(ExceptionsArray,
                                   ExceptionsArray + NumberOfExceptions);
}

DebugExceptionContainer WinDbgSystem::getPlatformExceptions() const {
  return exceptions;
}
