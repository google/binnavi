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

#ifndef WINDBGSYSTEM_HPP
#define WINDBGSYSTEM_HPP

#define _WIN32_WINNT 0x0500
#define NOMINMAX

#include "../defs.hpp"
#include "../BaseSystem.hpp"
#define WIN32_LEAN_AND_MEAN
#define _CRT_SECURE_NO_WARNINGS
#pragma comment(lib, "dbgeng")  //used for linker can be changed
#include <windows.h>
#include <Psapi.h>
#include <string>
#include <map>
#include "dbgeng.h"

#define NUM_INTERNAL_EVENTS 7
#define NUM_INTERNAL_DBG_EVENTS 3

struct DebuggerOptions;

enum WindowsVersion {
  XP,
  VISTA,
  SEVEN,
  WIN2003,
  WIN2008,
  UNKNOWN
};

enum WinDbgEvent {
  WDBG_BREAKPOINT_HIT = 0,
  WDBG_EXCEPTION = 1,
  WDBG_DEBUGGER_ATTACHED = 2,
  ZY_COM_INITIALIZED = 3,
  ZY_WAIT_FOR_DEBUG_EVENT = 4,
  ZY_WAIT_FOR_DEBUG_EVENT_RETURNED = 5,
  ZY_RESUMING_DEBUGEE = 6
};

/**
 * System policy for WinDbg interface.
 */
class WinDbgSystem : public BaseSystem {
 private:

  /**
   * Handle of the target process.
   */
  HANDLE hProcess;

  /**
   * ID of the last thread that reported a debug event.
   */
  DWORD lastThread;

  /**
   * Map that is used to keep track of the memory values
   * that are overwritten by breakpoint bytes.
   */
  std::map<std::string, char> originalBytes;

  /**
   * Keeps track of the threads of the target process.
   */
  std::vector<Thread> tids;

  /**
   * Set of all known threads.
   */
  std::set<unsigned int> knownThreads;

  std::vector<Module> modules;
  std::vector<Module> kernelModules;

  DWORD nextContinue;

  /**
   * Flag indicating, if we are currently processing a remove-breakpoint burst
   */
  bool removeBurst;

  /**
   * Flag indicating, if we are currently processing a set-breakpoint burst
   */
  bool setBurst;

  /**
   * Flag indicating, that no exception was raised so far
   * - used to determine the address of the std interrupt breakpoint
   */
  bool firstBreakpointException;

  /**
   * Address of the std interrupt breakpoint. Used to distinguish between
   * interrupt bps and normal bps.
   */
  CPUADDRESS addrInterrupt;

  /**
   * Handle to unnamed Mutex for thread-safe logging
   */
  HANDLE hMutexLogging;

  /**
   * Vector holding the execution status the debugee had before startTempHalt()
   * was called the last time
   */
  std::vector<ULONG> preHaltExecStatus;

  /**
   * A set of addresses of all breakpoints that were removed since the last halt.
   */
  std::set<CPUADDRESS> removedBreakpoints;

  /**
   * The user supplied named pipe to connect to.
   */
  const NATIVE_STRING pipe;

  std::vector<char> readPointedMemory(CPUADDRESS address);

  //interfaces
  static IDebugClient* client;
  static IDebugControl4* control;
  static IDebugSymbols* symbols;
  static IDebugRegisters* dregister;
  static IDebugSystemObjects4* systemObjects4;
  static IDebugAdvanced3* advanced3;
  static IDebugDataSpaces4* dataspaces4;

  /**
   * DebugBaseEventCallbacks methods
   * Are installed as callbacks for WinDbg events.
   */
  class EventCallbacks : public DebugBaseEventCallbacks {
   public:

    EventCallbacks(WinDbgSystem* windbg);
    ~EventCallbacks();

    STDMETHOD_(ULONG, AddRef)(THIS);STDMETHOD_(ULONG, Release)(THIS);

    // IDebugEventCallbacks.
    STDMETHOD(GetInterestMask)(THIS_ OUT PULONG Mask);

    STDMETHOD(Breakpoint)(THIS_ IN PDEBUG_BREAKPOINT Bp);
    STDMETHOD(Exception)(THIS_ IN PEXCEPTION_RECORD64 Exception,
        IN ULONG FirstChance);

    STDMETHOD(CreateThread)(ULONG64 Handle, ULONG64 DataOffset,
                             ULONG64 StartOffset);
    STDMETHOD(SessionStatus)(THIS_ IN ULONG Status);

    DEBUG_BREAKPOINT_PARAMETERS* getLastBreakpoint();
    EXCEPTION_RECORD64* getLastException();
    EXCEPTION_RECORD64* getLastExceptionUnhandled();  //might return NULL
    bool hasUnhandledException();
    void setLastExceptionUnhandled();
    unsigned int getLastExceptionThreadId();

   private:

    HANDLE hEventBreakpoint;
    HANDLE hEventException;
    HANDLE hEventDebuggerAttached;

    WinDbgSystem* windbg;

    bool unhandledException;

    DEBUG_BREAKPOINT_PARAMETERS* lastBreakpoint;
    EXCEPTION_RECORD64* lastException;
    unsigned int lastExceptionThreadId;
    EXCEPTION_RECORD64* lastExceptionUnhandled;

    void setLastBreakpoint(PDEBUG_BREAKPOINT Bp);
    void setLastException(PEXCEPTION_RECORD64 Exception);
    void setLastExceptionThreadId(unsigned int tid);
    void openEvents();
  };

  /**
   * IDebugOutputCallbacks methods
   * Are installed as callbacks for WinDbg output messages.
   */

  class OutputCallbacks : public IDebugOutputCallbacks {

    // IUnknown.
    STDMETHOD_(ULONG, AddRef)(THIS);
    STDMETHOD_(ULONG, Release)(THIS);
    STDMETHOD(QueryInterface)(THIS_ IN REFIID interfaceId,
        OUT PVOID* ppInterface);
    STDMETHOD(Output)(THIS_ IN ULONG mask, IN PCSTR msg);
  };

  EventCallbacks* eventCallbacks;
  OutputCallbacks* outputCallbacks;

  // list of internal event handles
  HANDLE eventHandles[NUM_INTERNAL_EVENTS];

  // list of internal debug event handles - a subset of eventHandles
  HANDLE dbgEventHandles[NUM_INTERNAL_DBG_EVENTS];

  // reads one byte from virtual address space
  NaviError readByte(CPUADDRESS offset, char& b);
  // writes one byte to virtual address space
  NaviError writeByte(CPUADDRESS offset, char b);

  // Cleans up COM objects
  static void cleanUpCOM();

  // starts a temporary halt-phase
  NaviError startTempHalt();

  // ends a temporary halt-phase
  NaviError endTempHalt();

  // starts a time measurement
  void startTimeMeasurement();

  // ends a time measurement
  DWORD endTimeMeasurement();

  // holds the various starting times of various time measurements
  std::vector<DWORD> startingTimes;

  // Sets a breakpoint in the target process
  NaviError setBreakpointRaw(const BREAKPOINT& breakpoint);

  // Removes a breakpoint from the target process
  NaviError removeBreakpointRaw(const BREAKPOINT& breakpoint);

  // Attempts to change the execution status of the primary thread
  NaviError changeExecutionStatus(ULONG execStatus);

  // Processes a debug event (event type is indicated by eventIndex)
  NaviError processDebugEvent(WinDbgEvent event);

  // Returns true if a breakpoint event is pending
  bool hasBreakpointEvent();

  // Sets the TrapFlag to the desired value
  NaviError setTrapFlag(BOOL value);

  // Gets the current state of the TrapFlag
  NaviError getTrapFlag(char& value);

  // Gets the address of the KTHREAD structure
  NaviError getKThreadAddress(ULONG processor, CPUADDRESS& addr);

  // Changes the step-mode of the target.
  NaviError changeStepMode(unsigned int threadId, bool enterSingleStep);

  void testThread();

 protected:

  // Connects to remote machine via a named pipe
  NaviError connectToPipe();

  // Attaches to a running process
  NaviError attachToProcess();

  // Starts a new process for debugging
  NaviError startProcess(const NATIVE_STRING path,
                         const std::vector<const NATIVE_STRING>& commands);

  // Detaches from the target process
  NaviError detach();

  // Terminates the target process
  NaviError terminateProcess();

  // Stores the original data that is replaced by a breakpoint
  NaviError storeOriginalData(const BREAKPOINT& bp);

  // Sets a breakpoint in the target process
  NaviError setBreakpoint(const BREAKPOINT& breakpoint,
                          bool moreToCome = false);

  // Removes a breakpoint from the target process
  NaviError removeBreakpoint(const BREAKPOINT& bp, bool moreToCome = false);

  // Executes the next instruction in thread of the target process
  NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address);

  // Resumes a thread in the target process
  NaviError resumeThread(unsigned int tid);

  NaviError resumeProcess();

  NaviError resumeAfterStepping(unsigned int threadId, CPUADDRESS address);

  NaviError suspendThread(unsigned int tid);

  // Resumes the WinDBG wait thread
  NaviError resumeWaitThread();

  // Returns the current instruction pointer of a thread of the target process
  NaviError getInstructionPointer(unsigned int tid, CPUADDRESS& addr);

  // Returns the windows version of the debuggee
  NaviError getWindowsVersion(WindowsVersion& version);

  // Reads the debuggee's current thread id
  NaviError readThreadId(unsigned int& tid);

  // Gets the current value of the debuggee's FS register
  NaviError getFSRegisterValue(CPUADDRESS& fs);

  // Sets the instruction pointer of a thread in the target process
  NaviError setInstructionPointer(unsigned int tid, CPUADDRESS address);

  // Reads the register values of all threads
  NaviError readRegisters(RegisterContainer& registers);

  // Sets the value of a register in the target process
  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS address);

  // Reads a part of the target process memory
  NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to);

  NaviError writeMemory(CPUADDRESS address, const std::vector<char>& data);

  NaviError writeBytes(HANDLE hProcess, CPUADDRESS offset,
                       std::vector<char> data);

  // Finds the memory region an offset belongs to
  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from, CPUADDRESS& to);

  // Upades the address space
  NaviError updateAddressSpace();

  // initialize the address of the winlogon process
  NaviError WinDbgSystem::initWinlogonAddress();

  // Find all allocated memory sections of the target process
  NaviError getMemmap(std::vector<CPUADDRESS>& addresses);

  // Halts the target process
  NaviError halt();

  // Checks for any breakpoint at the given address
  bool hasCustomBreakpoint(std::string addressString);

  ULONG getExecutionStatus();

  NaviError readProcessList(ProcessListContainer& processList);

  NaviError readFiles(FileListContainer& fileList) {
    return NaviErrors::SUCCESS;
  }

  NaviError readFiles(FileListContainer& fileList, const std::string& path) {
    return NaviErrors::SUCCESS;
  }

  NaviError log_current_instruction();

  NaviError log_executionstatus();

  NaviError attachToPipe();

  NaviError parsePAE(std::vector<CPUADDRESS>& addresses, int PSE);

  NaviError parseNonPAE(std::vector<CPUADDRESS>& addresses, int PSE);

  NaviError waitForDebugEvent();

  NaviError waitForDebugEventConsume();

  NaviError unsetDebugEvents();

  NaviError setActiveThread(unsigned int tid);

  NaviError getFileSystems(std::vector<boost::filesystem::path>& roots) const;

  NaviError getSystemRoot(boost::filesystem::path& root) const;

  DebugExceptionContainer getPlatformExceptions() const;

 public:
  /**
   * Creates a new WinDbgSystem object
   *
   * @param pipeName The name of the pipe to connect windbg to
   */
  WinDbgSystem(const NATIVE_STRING pipe);

  ~WinDbgSystem();

  // Reads new debug events from the target process
  NaviError readDebugEvents();

  // Returns the register names that are available on the target architecture
  std::vector<RegisterDescription> getRegisterNames() const;

  // Returns the address size of the target architecture
  unsigned int getAddressSize() const;

  // Returns the debugger options that are supported by the debug client
  DebuggerOptions getDebuggerOptions() const;

  // Updates the active thread id
  NaviError updateActiveThread();

  // called by waiter-thread
  static void initCOM();

  //* Returns a pointer to the debug controller com object
  static IDebugControl4* getDebugControl() {
    return control;
  }
  static IDebugSystemObjects4* getDebugSystemObjects() {
    return systemObjects4;
  }

};
#endif
