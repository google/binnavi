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

#include "WinCESystem.hpp"

#include <algorithm>
#include <iostream>
#include <winnt.h>
#include <winbase.h>
#include <psapi.h>

#include <tlhelp32.h>

//#include <pkfuncs.h>
#include <zycon/src/zycon.h>

#include "../errors.hpp"
#include "../DebuggerOptions.hpp"
#include "../logger.hpp"
#include "gdb_arm.h"

/*
 * SetProcPermissions is a kernel api available on WM 5.0 and below, giving the
 * calling process extended rights.
 * Since its not defined in the standard sdk's header files the following
 * construction is necesarry.
 */
extern "C" {
DWORD GetCurrentPermissions(void);
DWORD SetProcPermissions(DWORD newperms);
BOOL WINAPI SetKMode(BOOL fMode);
}

/* Flags for CacheSync/CacheRangeFlush */
#define CACHE_SYNC_DISCARD 0x001      /* write back & discard all cached data */
#define CACHE_SYNC_INSTRUCTIONS 0x002 /* discard all cached instructions */
#define CACHE_SYNC_WRITEBACK 0x004    /* write back but don't discard data \
cache*/
#define CACHE_SYNC_FLUSH_I_TLB 0x008 /* flush I-TLB */
#define CACHE_SYNC_FLUSH_D_TLB 0x010 /* flush D-TLB */
#define CACHE_SYNC_FLUSH_TLB \
  (CACHE_SYNC_FLUSH_I_TLB | CACHE_SYNC_FLUSH_D_TLB) /* flush all TLB */
#define CACHE_SYNC_L2_WRITEBACK 0x020               /* write-back L2 Cache */
#define CACHE_SYNC_L2_DISCARD 0x040                 /* discard L2 Cache */

#define CACHE_SYNC_ALL 0x07F /* sync and discard everything in Cache/TLB */

/*
 * The CacheSync and CacheRangeFlush functions are locatet in recent versions of
 * the coredll.dll
 */
extern "C" {
void CacheSync(int flags);
void CacheRangeFlush(void* pAddr, unsigned long dwLength,
                     unsigned long dwFlags);
}

/* ARM Type Breakpoint */
#define BPX_ARM 0xE6000010

#ifndef min
#define min(a, b) ((a) < (b) ? (a) : (b))
#endif

/**
 * Conversion function that takes a TCHAR and converts it to a std::string
 */
std::string TCharToString(LPCTSTR t) {
  // Handy for converting TCHAR to std::string (char)
  // If the conversion fails, an empty string is returned.
  std::string str;
#ifdef UNICODE
  // calculate the size of the char string required
  // Note: If wcstombs encounters a wide character it cannot convert
  //       to a multibyte character, it returns 1.
  int len = 1 + wcstombs(0, t, 0);
  if (0 == len) return str;

  char* c = new char[len];
  c[0] = '\0';

  wcstombs(c, t, len);
  str = c;
  delete[] c;
#else
  str = t;
#endif
  return str;
}

/*
 * Conversion function that takes a LPWSTR and converts it to a std::string
 */
bool LPWSTRToString(std::string& s, const LPWSTR pw, UINT codepage = CP_ACP) {
  bool res = false;
  char* p = 0;
  int bsz;
  bsz = WideCharToMultiByte(codepage, 0, pw, -1, 0, 0, 0, 0);
  if (bsz > 0) {
    p = new char[bsz];
    int rc = WideCharToMultiByte(codepage, 0, pw, -1, p, bsz, 0, 0);
    if (rc != 0) {
      p[bsz - 1] = 0;
      s = p;
      res = true;
    }
  }
  delete[] p;
  return res;
}

void dbg_echo_MEMORY_BASIC_INFORMATION(MEMORY_BASIC_INFORMATION* m) {
  msglog->log(LOG_VERBOSE, "Base: %lx", m->BaseAddress);
  msglog->log(LOG_VERBOSE, "AllocationBase: %lx", m->AllocationBase);
  msglog->log(LOG_VERBOSE, "AllocationProtect: %lx", m->AllocationProtect);
  msglog->log(LOG_VERBOSE, "RegionSize: %lx", m->RegionSize);
  msglog->log(LOG_VERBOSE, "state: %lx", m->State);
  msglog->log(LOG_VERBOSE, "Protect: %lx", m->Protect);
  msglog->log(LOG_VERBOSE, "Type: %lx", m->Type);
}

/**
 * DLL load / unload handler
 *
 * @param LPVOID moduleBaseAddress The Base Address of the module in the debug
 * event.
 * @param DWORD processID The process id of the process that created the debug
 * event.
 * @param CPUADDRESS imageNamePtr An address ptr pointing to the name of the
 * module.
 * @param int isUnicode indication from the debug event if the name Ptr is
 * unicode.
 * @param bool load indication if the debug event is a load event or an unload
 * event.
 */
NaviError WinCESystem::dllDebugEventHandler(LPVOID moduleBaseAddress,
                                            DWORD processID,
                                            CPUADDRESS imageNamePtr,
                                            int isUnicode, bool load) {


  // String to hold the complete path infromation about the newly loaded DLL
  TCHAR moduleName[MAX_PATH] = L"\0";

  // String that has the information about the module in std::string format
  std::string moduleNameString;

  // Module handle
  HMODULE hModule = NULL;

  // Get the filename of the loaded DLL by the handle from the debug event
  PtrToString(processID, imageNamePtr, MAX_PATH - 1, isUnicode, moduleName);

  DWORD moduleBaseSize = 23;

  // Convert the recieved name to a std::string
  if (!LPWSTRToString(moduleNameString, moduleName, 0)) {
    msglog->log(LOG_VERBOSE,
                "Error: conversion from LPWSTR to std::string failed at %s:%d",
                __FUNCTION__, __LINE__);

    return NaviErrors::COULDNT_FIND_DATA;
  }

  Module currentModule(moduleNameString, moduleNameString,
                       (CPUADDRESS) moduleBaseAddress, moduleBaseSize);

  if (load) {
    NaviError moduleLoadingError = moduleLoaded(currentModule);
    if (moduleLoadingError) {
      msglog->log(LOG_ALWAYS,
                  "Error: Could not handle module (un)loading at %s:%d",
                  __FUNCTION__, __LINE__);

      return NaviErrors::COULDNT_FIND_DATA;
    }
    this->modules.push_back(currentModule);
  } else {  // unload case.
    for (std::vector<Module>::iterator Iter = this->modules.begin();
        Iter != this->modules.end(); ++Iter) {
      if (currentModule == *Iter) {
        this->modules.erase(Iter);
        NaviError moduleLoadingError = moduleUnloaded(currentModule);
        if (moduleLoadingError) {
          msglog->log(LOG_ALWAYS,
                      "Error: Could not handle module (un)loading at %s:%d",
                      __FUNCTION__, __LINE__);

          return NaviErrors::COULDNT_FIND_DATA;
        }
      }
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Makes a page of the target process memory writable.
 *
 * @param hProcess The handle of the target process.
 * @param offset The offset whose page should be made restored.
 * @param oldProtection The original protection level of the page.
 */
bool makePageWritable(HANDLE hProcess, CPUADDRESS offset,
                      DWORD& oldProtection) {


  MEMORY_BASIC_INFORMATION mem;

  // Check if the location is read-only
  int returned = VirtualQuery(reinterpret_cast<void*>(offset),
                              &mem, sizeof(mem));

  if (returned != sizeof(mem)) {
    msglog->log(LOG_VERBOSE,
                "Error: Invalid return value of VirtualQuery at %s:%d",
                __FUNCTION__, __LINE__);
    return false;
  }

  if (!(mem.AllocationProtect & PAGE_READWRITE)) {
    if (!VirtualProtect(reinterpret_cast<void*>(offset), 4,
                        PAGE_EXECUTE_READWRITE, &oldProtection)) {
      msglog->log(LOG_VERBOSE,
                  "Error: VirtualProtect failed at %s:%d with (%d)",
                  __FUNCTION__, __LINE__, GetLastError());
      return false;
    }
  }
  return true;
}

/**
 * Restores the old protection level of a page of the target process memory.
 *
 * @param hProcess The handle of the target process.
 * @param offset The offset whose page should be made restored.
 * @param oldProtection The original protection level of the page.
 */
NaviError restoreMemoryProtection(HANDLE hProcess, CPUADDRESS offset,
                                  DWORD oldProtection) {
  msglog->log(LOG_ALL, "Info: old protection level: %08X", oldProtection);

  DWORD oldProtection2 = 0;

  if (!VirtualProtect(reinterpret_cast<void*>(offset), 4, oldProtection,
                      &oldProtection2)) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Resetting VirtualProtect state failed at %s:%d with (%d)",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::PAGE_NOT_WRITABLE;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Helper function that writes a vector of bytes to the process memory of the
 * target process.
 *
 */
NaviError writeBytes(HANDLE hProcess, CPUADDRESS offset,
                     std::vector<char> data) {
  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  // Try to make the page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  SIZE_T writtenbytes = 0;

  // Write the byte to the target process memory
  if (!WriteProcessMemory(hProcess, reinterpret_cast<void*>(offset), &data[0],
                          data.size(), &writtenbytes)) {
    msglog->log(LOG_VERBOSE, "Error: WriteProcessMemory failed at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }

  // CacheSync invalidates the cache and makes the written data usable
  // See http://msdn.microsoft.com/en-us/library/aa911523.aspx
  //CacheSync(CACHE_SYNC_ALL);
  if (!FlushInstructionCache(hProcess, 0, 0)) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Couldn't flush instruction cache at %s:%d with (Code: %d) ",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::UNSUPPORTED;
  }

  if (restoreMemoryProtection(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't restore old memory protection at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  // restore the access permissions
  SetProcPermissions(oldPermission);
  return NaviErrors::SUCCESS;
}

/**
 * Helper function that writes a single byte to the process memory of the target
 * process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to write to.
 * @param b Byte to write to the target process memory.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError writeByte(HANDLE hProcess, CPUADDRESS offset, char b) {
  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  // Try to make the page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  SIZE_T writtenbytes = 0;

  // Write the byte to the target process memory
  if (!WriteProcessMemory(hProcess, reinterpret_cast<void*>(offset), &b, 1,
                          &writtenbytes)) {
    msglog->log(LOG_VERBOSE, "Error: WriteProcessMemory failed at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }

  // CacheSync invalidates the cache and makes the written data usable
  // See http://msdn.microsoft.com/en-us/library/aa911523.aspx
  //CacheSync(CACHE_SYNC_ALL);
  if (!FlushInstructionCache(hProcess, 0, 0)) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Couldn't flush instruction cache at %s:%d with (Code: %d) ",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::UNSUPPORTED;
  }

  if (restoreMemoryProtection(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't restore old memory protection at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  // restore the access permissions
  SetProcPermissions(oldPermission);
  return NaviErrors::SUCCESS;
}

/**
 * Helper function that writes a single DWORD to the process memory of the target
 * process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to write to.
 * @param d DWORD to write to the target process memory.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError writeDWORD(HANDLE hProcess, CPUADDRESS offset, unsigned int d) {
  // std::cout << "Entering: " << __FUNCTIONW__ << std::endl;



  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  // Try to make page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  SIZE_T writtenbytes = 0;

  // Write the DWORD to the target process memory
  if (!WriteProcessMemory(hProcess, reinterpret_cast<void*>(offset), &d, 4,
                          &writtenbytes)) {
    msglog->log(LOG_VERBOSE, "Error: WriteProcessMemory failed at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }

  // CacheSync invalidates the cache and makes the written data usable
  // See http://msdn.microsoft.com/en-us/library/aa911523.aspx
  //CacheSync(CACHE_SYNC_ALL);
  if (!FlushInstructionCache(hProcess, 0, 0)) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Couldn't flush instruction cache at %s:%d with (Code: %d) ",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::UNSUPPORTED;
  }

  if (restoreMemoryProtection(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't restore old memory protection at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_WRITABLE;
  }

  // Restore the thread access permissions
  SetProcPermissions(oldPermission);
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::readMemoryData(char* buffer, CPUADDRESS offset,
                                      CPUADDRESS size) {
  return readMemoryDataInternal(buffer, offset, size, false);
}
NaviError WinCESystem::readMemoryDataInternal(char* buffer, CPUADDRESS offset,
                                              CPUADDRESS size, bool silent) {
  //

  SIZE_T outlen;

  msglog->log(LOG_VERBOSE, "Trying to read %d bytes from memory address %X",
              size, offset);

  // Make sure the entire region from addr to addr2 is paged
  // VirtualQuery(ntohl(addr->low32bits), & meminf, ntohl(addr2->low32bits) -
  // nothl(addr->low32bits));

  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_READABLE;
  }

  if (ReadProcessMemory(hProcess, reinterpret_cast<void*>(offset), buffer,
                        size, &outlen) == 0) {
    if (!silent) {
      msglog->log(LOG_ALWAYS,
                  "Error: ReadProcessMemory failed (Error Code: %d)",
                  GetLastError());
    }

    return NaviErrors::COULDNT_READ_MEMORY;
  }

  restoreMemoryProtection(hProcess, offset, oldProtection);

  // Restore the thread access permissions
  SetProcPermissions(oldPermission);

  return NaviErrors::SUCCESS;
}

/**
 * Helper function that reads a WString from the process memory of the target
 * process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to read from.
 * @param len in TCHAR to be read from memory max 1023.
 * @param d The read WString is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError readWString(HANDLE hProcess, CPUADDRESS offset, int len, TCHAR* d) {
  //

  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  SIZE_T bytesRead = 0;

  // Try to read the WString from memory
  if (!ReadProcessMemory(hProcess, reinterpret_cast<void*>(offset), d, len,
                         &bytesRead)) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read WString from memory at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  // restore the thread access permissions
  SetProcPermissions(oldPermission);

  return NaviErrors::SUCCESS;
}

/**
 * Helper function that reads a WString from the process memory of the target
 * process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to read from.
 * @param len in char to be read from memory max 1023.
 * @param d The read WString is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError readString(HANDLE hProcess, CPUADDRESS offset, int len, char& d) {
  //

  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  // Try to make page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_READABLE;
  }

  SIZE_T bytesRead = 0;

  // Try to read the WString from memory
  if (!ReadProcessMemory(hProcess, reinterpret_cast<void*>(offset), &d,
                         min(len, sizeof(d) - 1), &bytesRead)) {
    restoreMemoryProtection(hProcess, offset, oldProtection);
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read WString from memory at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  // End the string
  //d[bytesRead] = 0;

  restoreMemoryProtection(hProcess, offset, oldProtection);

  // restore the thread access permissions
  SetProcPermissions(oldPermission);

  return NaviErrors::SUCCESS;
}

/**
 * Helper function that reads a single DWORD from the process memory of the
 * target process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to read from.
 * @param d The read DWORD is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError readDWORD(HANDLE hProcess, CPUADDRESS offset, unsigned int& d) {
  //

  DWORD oldPermission = 0;
  DWORD oldProtection = 0;

  // Save the old permissions to later restore them
  oldPermission = GetCurrentPermissions();

  // Set the permissions for full access on all threads
  SetProcPermissions(0xFFFFFFFF);

  // Try to make page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_READABLE;
  }

  SIZE_T bytesRead = 0;

  // Try to read the DWORD from
  if (!ReadProcessMemory(hProcess, reinterpret_cast<void*>(offset), &d, 4,
                         &bytesRead)) {
    restoreMemoryProtection(hProcess, offset, oldProtection);
    msglog->log(LOG_VERBOSE, "Error: Couldn't read DWORD from memory at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  restoreMemoryProtection(hProcess, offset, oldProtection);

  // restore the thread access permissions
  SetProcPermissions(oldPermission);
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return NaviErrors::SUCCESS;

}

NaviError WinCESystem::writeMemory(CPUADDRESS address,
                                   const std::vector<char>& data) {
  return writeBytes(hProcess, address, data);
}

/**
 * Reads a string from memory address ptr
 *
 * @param DWORD processID Process ID from whoms memory should be read
 * @param CPUADDRESS *address AddressPTR from where the string in memory is to
 * be read
 * @param int len Length of string to be read from memory
 * @param int isUnicode determines if the string to be read is a unicode string
 * or not
 * @param TCHAR string String which holds the result
 */
NaviError WinCESystem::PtrToString(DWORD dwProcessId, CPUADDRESS address,
                                   int len, int uni, TCHAR* tbuf) {
  //

  if (address == NULL) {
    return NaviErrors::COULDNT_READ_MEMORY;
  } else if (uni) {
    readWString((HANDLE) dwProcessId, address, len, tbuf);

    if (tbuf == NULL) {
      msglog->log(LOG_ALL, "Error: string is NULL %s", __FUNCTION__);

      return NaviErrors::COULDNT_READ_MEMORY;
    }
    return NaviErrors::SUCCESS;
  } else {
    static char buf[1024];
    //        readString((HANDLE) dwProcessId, address, len, *buf);

    if (buf == NULL) {
      msglog->log(LOG_ALL, "Error: string is NULL %s", __FUNCTION__);

      return NaviErrors::COULDNT_READ_MEMORY;
    }
    // convert the char[] to TCHAR[]
    //       _sntprintf(&tbuf, 256, L"%hs", buf);

    return NaviErrors::SUCCESS;
  }

}

/**
 * Starts a new process for debugging.
 *
 * @param path The path to the executable of the process.
 * @param arguments The vector of arguments passed to the process.
 * @param tids The thread IDs of the threads that belong to the target process.
 * @param modules The vector of modules loaded by the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinCESystem::startProcess(
    const wchar_t* path, const std::vector<const wchar_t*>& arguments,
    std::vector<Thread>& tids, std::vector<Module>& modules) {
  //

  // Enable full kernel mode
  BOOL bMode = SetKMode(TRUE);

  // Save the current permissions so they can be restored
  DWORD orgPermissions = GetCurrentPermissions();

  // Set the permissions with full access to all threads
  SetProcPermissions(0xFFFFFFFF);

  PROCESS_INFORMATION pi;

  if (!CreateProcessW(path, NULL, NULL, NULL, FALSE, DEBUG_PROCESS, NULL, NULL,
                      NULL, &pi)) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't start target process at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }

  // Keep track of the process hanlde of the target process
  hProcess = OpenProcess(1, false, pi.dwProcessId);
  setPID(pi.dwProcessId);

  // Restore permissions to the stored value.
  SetProcPermissions(orgPermissions);

  // Keep track of the last thread that caused a debug event
  setActiveThread(pi.dwThreadId);

  DEBUG_EVENT dbg_evt;

  // Wait & skip over all initial messages until the first "real" exception is
  // hit
  for (;;) {
    if (!WinCE_arm_wait_for_debug_event(&dbg_evt, INFINITE)) {
      // Waiting for a debug event failed => We will no be able to debug
      msglog->log(LOG_VERBOSE,
                  "Error: WaitForDebugEvent() failed - aborting at %s:%d",
                  __FUNCTION__, __LINE__);

      CloseHandle(hProcess);

      return NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
    }

    // Store the id of the last thread that caused the last debug event
    setActiveThread(dbg_evt.dwThreadId);

    msglog->log(LOG_VERBOSE, "Received debug event %d",
                dbg_evt.dwDebugEventCode);

    if (dbg_evt.dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
      msglog->log(LOG_VERBOSE,
                  "Initial debug event received - starting debug loop.");

      if (dllDebugEventHandler(dbg_evt.u.LoadDll.lpBaseOfDll,
                               dbg_evt.dwProcessId,
                               (CPUADDRESS) dbg_evt.u.LoadDll.lpImageName,
                               dbg_evt.u.LoadDll.fUnicode, true)) {
        return false;
      }

      CloseHandle(hProcess);

      return NaviErrors::SUCCESS;
    } else if (dbg_evt.dwDebugEventCode == CREATE_PROCESS_DEBUG_EVENT) {
      CREATE_PROCESS_DEBUG_INFO* info = &dbg_evt.u.CreateProcessInfo;
      hThread = info->hThread;
      setBaseOfImage((unsigned int) info->lpBaseOfImage);

      Thread ts(dbg_evt.dwThreadId, SUSPENDED);
      tids.push_back(ts);
      this->tids.push_back(ts);

      return NaviErrors::SUCCESS;
    } else {
      msglog->log(LOG_VERBOSE, "Error: Unexpected debug event %d at %s:%d",
                  dbg_evt.dwDebugEventCode, __FUNCTION__, __LINE__);
      WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);
    }
  }
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::attachToProcess(std::vector<Thread>& tids,
                                       std::vector<Module>& modules) {
  //

  DWORD oldPermission = 0;

  // Store the current thread access permissions
  oldPermission = GetCurrentPermissions();

  // Set thread access permissions
  SetProcPermissions(0xFFFFFFFF);

  // TODO: hProcess must be closed somewhere later

  msglog->log(LOG_VERBOSE, "Info: Opening process (PID: %d)", getPID());

  // Try to open the target process
  HANDLE hProcess = OpenProcess(0, false, getPID());

  if (hProcess == NULL) {
    msglog->log(
        LOG_VERBOSE,
        "Error: failed to OpenProcess() - aborting at %s:%d with (Code: %d)",
        __FUNCTION__, __LINE__, GetLastError());

    // Restore old thread access permissions
    SetProcPermissions(oldPermission);

    CloseHandle(hProcess);

    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }

  msglog->log(LOG_VERBOSE, "Debugging process (PID: %d)", getPID());

  // Try to get debugging rights for the target process
  if (!DebugActiveProcess(getPID())) {
    msglog->log(LOG_VERBOSE, "Error: DebugActiceProcess() failed - aborting at "
                "%s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());

    // Restore old thread access permissions
    SetProcPermissions(oldPermission);

    CloseHandle(hProcess);

    return NaviErrors::COULDNT_DEBUG_TARGET_PROCESS;
  }

  DEBUG_EVENT dbg_evt;

  // Wait & skip over all initial messages until the first "real" exception is
  // hit
  for (;;) {
    if (!WinCE_arm_wait_for_debug_event(&dbg_evt, INFINITE)) {
      msglog->log(LOG_VERBOSE,
                  "Error: WaitForDebugEvent() failed - aborting at %s:%d",
                  __FUNCTION__, __LINE__);

      // Restore old thread access permissions
      SetProcPermissions(oldPermission);

      CloseHandle(hProcess);
      return NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
    }

    // Store the id of the last thread that caused the last debug event
    setActiveThread(dbg_evt.dwThreadId);

    if (dbg_evt.dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
      msglog->log(
          LOG_VERBOSE,
          "Info: initial debug event received - starting debug loop at %s:%d",
          __FUNCTION__, __LINE__);

      // The initial debug event is caused by a thread that does not really
      // belong to the target process
      tids.erase(std::remove_if(tids.begin(),
                                tids.end(),
                                Thread::MakeThreadIdComparator(dbg_evt.dwThreadId)),
                 tids.end());
      this->tids.erase(
          std::remove_if(this->tids.begin(),
                         this->tids.end(),
                         Thread::MakeThreadIdComparator(dbg_evt.dwThreadId)),
          this->tids.end());

      // Resume the target thread
      WinCE_arm_resume(dbg_evt.dwThreadId, DBG_CONTINUE);

      // Fill the list of loaded modules
      dllDebugEventHandler(dbg_evt.u.LoadDll.lpBaseOfDll, dbg_evt.dwProcessId,
                           (CPUADDRESS) dbg_evt.u.LoadDll.lpImageName,
                           dbg_evt.u.LoadDll.fUnicode, true);

      // Restore old thread access permissions
      SetProcPermissions(oldPermission);

      return NaviErrors::SUCCESS;
    } else {
      // Store the thread id of the thread that caused the event, but make sure
      // that no duplicates are stored
      if (std::find_if(tids.begin(),
                       tids.end(),
                       Thread::MakeThreadIdComparator(dbg_evt.dwThreadId))
                     == tids.end()) {
        Thread ts(dbg_evt.dwThreadId, RUNNING);

        tids.push_back(ts);
        this->tids.push_back(ts);
      }

      // Restore old thread access permissions
      SetProcPermissions(oldPermission);

      WinCE_arm_resume(dbg_evt.dwThreadId, DBG_CONTINUE);
    }
  }
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return NaviErrors::SUCCESS;
}

bool WinCESystem::WinCE_arm_wait_for_debug_event(LPDEBUG_EVENT lpDebugEvent,
                                                 DWORD dwMilliseconds) {
  //

  DWORD counter = 0;
  bool exit = false;
  bool ret = false;
  while ((counter < dwMilliseconds) && (exit == false) && (ret == false)) {
    ret = (WaitForDebugEvent(lpDebugEvent, 0) != 0);
    counter += 100;

    exit = (WaitForSingleObject(hEventExit, 0) == WAIT_OBJECT_0);
  }

  if (exit == true) {
    clearBreakpoints(bpxlist, BPX_simple);
    clearBreakpoints(ebpxlist, BPX_echo);
    clearBreakpoints(sbpxlist, BPX_stepping);
    ResumeThread(hThread);
    ExitThread(0);
  }
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return ret;
}

/**
 * @return True, if the function succeeds. False, otherwise.
 */
bool WinCESystem::WinCE_arm_resume(unsigned int tid, DWORD mode) {
  //

  msglog->log(LOG_VERBOSE, "Resuming thread %X in process %X", tid, getPID());

  if (!ContinueDebugEvent(getPID(), tid, mode)) {
    LPVOID message;
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
                  NULL, GetLastError(),
                  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR) & message,
                  0, NULL);

    msglog->log(
        LOG_VERBOSE,
        "Error: ContinueDebugEvent() failed with error: (Message: %s) at %s:%d",
        message, __FUNCTION__, __LINE__);

    LocalFree(message);
    return false;
  }
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return true;
}

NaviError WinCESystem::setBreakpoint(const BREAKPOINT& breakpoint, bool) {
  return writeDWORD(hProcess, breakpoint.addr + baseOfImage, BPX_ARM);
}

bool WinCESystem::WinCE_arm_is_dbg_event_available() {
  DEBUG_EVENT dbg_evt;

  // Now process all debug events we can get
  while (WinCE_arm_wait_for_debug_event(&dbg_evt, 1000)) {
    msglog->log(LOG_VERBOSE, "Info: received debug event (Code: %d)",
                dbg_evt.dwDebugEventCode);

    // Store the last thread that caused a debug event
    setActiveThread(dbg_evt.dwThreadId);

    DBGEVT dbgevt;

    nextContinue = DBG_CONTINUE;

    // Get the debug event code we are dealing with
    switch (dbg_evt.dwDebugEventCode) {
      case EXCEPTION_DEBUG_EVENT: {
        // Handle the case of exceptions

        msglog->log(LOG_VERBOSE, "Info: received exception code %08X at %08X.",
                    dbg_evt.u.Exception.ExceptionRecord.ExceptionCode,
                    dbg_evt.u.Exception.ExceptionRecord.ExceptionAddress);

        DWORD code = dbg_evt.u.Exception.ExceptionRecord.ExceptionCode;
        CPUADDRESS address = (CPUADDRESS) dbg_evt.u.Exception.ExceptionRecord
            .ExceptionAddress;

        if (code == EXCEPTION_ACCESS_VIOLATION) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::ACCESS_VIOLATION);
          return true;
        } else if (code == EXCEPTION_ILLEGAL_INSTRUCTION) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::ILLEGAL_INSTRUCTION);

          return true;
        } else if (code == EXCEPTION_IN_PAGE_ERROR) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::PAGE_ERROR);

          return true;
        } else if (code == EXCEPTION_INT_DIVIDE_BY_ZERO) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::DIVIDE_BY_ZERO);

          return true;
        } else if (code == EXCEPTION_INT_OVERFLOW) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::INTEGER_OVERFLOW);

          return true;
        } else if (code == EXCEPTION_NONCONTINUABLE_EXCEPTION) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::NONCONTINUABLE_EXCEPTION);

          return true;
        } else if (code == EXCEPTION_PRIV_INSTRUCTION) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::PRIVILEDGED_INSTRUCTION);

          return true;
        } else if (code == EXCEPTION_STACK_OVERFLOW) {
          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::STACK_OVERFLOW);

          return true;
        } else if (code == EXCEPTION_BREAKPOINT) {
          // Handle breakpoint events

          std::string tmp =
              cpuAddressToString(
                  (CPUADDRESS) dbg_evt.u.Exception.ExceptionRecord
                      .ExceptionAddress);

          // Tell the base system that a breakpoint was hit
          NaviError hitResult = breakpointHit(tmp, dbg_evt.dwThreadId,
                                              true /* resume on echo bp */);

          if (hitResult) {
            // What could have happened here? Maybe a breakpoint was hit that
            // wasn't
            // set by the debugger. TODO: Care more about error codes.

            msglog->log(LOG_ALWAYS, "Error: Breakpoint handler failed at %s:%d",
                        __FUNCTION__, __LINE__);

            // Tell the program that we couldn't handle the breakpoint exception
            ContinueDebugEvent(dbg_evt.dwProcessId, dbg_evt.dwThreadId,
                               DBG_EXCEPTION_NOT_HANDLED);

            continue;
          }

          return true;
        } else if (code == 0xE06D7363) {
          msglog->log(LOG_ALL, "Info: C++ exception was hit", code);

          // Ignore C++ exceptions for now
          ContinueDebugEvent(dbg_evt.dwProcessId, dbg_evt.dwThreadId,
                             DBG_EXCEPTION_NOT_HANDLED);
          return false;
        } else {
          msglog->log(LOG_VERBOSE, "Info: Unknown exception with code %d was "
                      "thrown by the target process",
                      code);

          // Handle unknown exceptions

          nextContinue = DBG_EXCEPTION_NOT_HANDLED;
          exceptionRaised(dbg_evt.dwThreadId, address,
                          ProcessExceptions::UNKNOWN_EXCEPTION);

          return true;
        }
      }

      case LOAD_DLL_DEBUG_EVENT: {
        WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);

        if (dllDebugEventHandler(dbg_evt.u.LoadDll.lpBaseOfDll,
                                 dbg_evt.dwProcessId,
                                 (CPUADDRESS) dbg_evt.u.LoadDll.lpImageName,
                                 dbg_evt.u.LoadDll.fUnicode, true)) {
          return false;
        }

        return true;

      }
      case UNLOAD_DLL_DEBUG_EVENT: {
        WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);

        if (dllDebugEventHandler(dbg_evt.u.LoadDll.lpBaseOfDll,
                                 dbg_evt.dwProcessId,
                                 (CPUADDRESS) dbg_evt.u.LoadDll.lpImageName,
                                 dbg_evt.u.LoadDll.fUnicode, false)) {
          return false;
        }

        return true;
      }

      case CREATE_THREAD_DEBUG_EVENT: {
        // Handle thread creation events

        msglog->log(LOG_VERBOSE,
                    "Info: created new thread with TID %d at %s:%d",
                    dbg_evt.dwThreadId, __FUNCTION__, __LINE__);

        // Save the thread in the internal list
        Thread ts(dbg_evt.dwThreadId, SUSPENDED);
        tids.push_back(ts);

        // Tell the system about the new thread
        NaviError ctResult = threadCreated(dbg_evt.dwThreadId, SUSPENDED);

        if (ctResult) {
          msglog->log(LOG_VERBOSE,
                      "Error: Couldn't handle thread creation at %s:%d",
                      __FUNCTION__, __LINE__);
        }

        WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);

        return true;
      }

      case EXIT_THREAD_DEBUG_EVENT: {
        // Handle the threads exit events

        msglog->log(LOG_VERBOSE, "Info: thread with TID %d exited at %s:%d",
                    dbg_evt.dwThreadId, __FUNCTION__, __LINE__);

        // Remove the thread from the internal list
        tids.erase(
            std::remove_if(tids.begin(), tids.end(),
                           Thread::MakeThreadIdComparator(dbg_evt.dwThreadId)),
            tids.end());

        // tell the system about the exited thread
        NaviError ctResult = threadExit(dbg_evt.dwThreadId);

        if (ctResult) {
          msglog->log(LOG_VERBOSE,
                      "Error: Couldn't handle thread creation at %s:%d",
                      __FUNCTION__, __LINE__);
        }

        WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);

        return true;
      }
      case EXIT_PROCESS_DEBUG_EVENT: {
        // Handle the event that the target process exited
        msglog->log(LOG_VERBOSE, "Info: target process exited at %s:%d",
                    __FUNCTION__, __LINE__);

        // Tell the base system that the process exited
        processExit();

        return true;
      }

      default: {
        // Unknown debug event => tell the target process that we couldn't
        // handle it.
        WinCE_arm_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);

      }
    }
  }

  return false;
}

NaviError WinCESystem::readDebugEvents() {

  return
      WinCE_arm_is_dbg_event_available() ?
          NaviErrors::SUCCESS : NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
}

NaviError WinCESystem::removeBreakpoint(const BREAKPOINT& bp_in, bool) {


  unsigned int writtenbytes = 0;

  char tmp[50];
  sprintf(tmp, ADDRESS_FORMAT_MASK, bp_in.addr);

  if (originalDWORDs.find(tmp) == originalDWORDs.end()) {
    msglog->log(LOG_VERBOSE, "Error: Trying to restore a breakpoint with "
                "unknown original byte at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::ORIGINAL_DATA_NOT_AVAILABLE;
  }

  unsigned int d = originalDWORDs[tmp];

  msglog->log(LOG_VERBOSE, "Writing byte %lx to address %lx\n", d, bp_in.addr);
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return writeDWORD(hProcess, bp_in.addr, d);
}

std::vector<char> WinCESystem::readPointedMemory(CPUADDRESS address) {


  unsigned int currentSize = 128;

  while (currentSize != 0) {
    std::vector<char> memory(currentSize, 0);

    if (readMemoryDataInternal(&memory[0], address, memory.size(), true)
        == NaviErrors::SUCCESS) {
      return memory;
    }

    currentSize /= 2;
  }

  return std::vector<char>();
}

NaviError WinCESystem::setInstructionPointer(unsigned int tid,
                                             CPUADDRESS address) {
  // std::cout << "Entering: " << __FUNCTIONW__ << std::endl;



  DWORD oldPermission = 0;

  CONTEXT threadContext;

  threadContext.ContextFlags = CONTEXT_FULL;

  // save the current thread access permissions
  oldPermission = GetCurrentPermissions();

  // get full access to all threads
  SetProcPermissions(0xFFFFFFFF);

  if (!GetThreadContext(hThread, &threadContext)) {
    msglog->log(LOG_VERBOSE,
                "Error: GetThreadContext failed at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  threadContext.Pc = address;

  if (!SetThreadContext(hThread, &threadContext)) {
    msglog->log(LOG_VERBOSE,
                "Error: SetThreadContext failed at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    NaviErrors::COULDNT_WRITE_REGISTERS;
  }

  // restore old thread access permissions
  SetProcPermissions(oldPermission);
  return NaviErrors::SUCCESS;
}

/**
 * Helper function that reads a single byte from the process memory of the target
 * process.
 *
 * @param hProcess Process handle of the target process.
 * @param offset Memory offset to read from.
 * @param b The read byte is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError readByte(HANDLE hProcess, CPUADDRESS offset, char& b) {


  DWORD oldProtection = 0;

  // Try to make the page writable for us
  if (!makePageWritable(hProcess, offset, oldProtection)) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't make page read/writable at",
                __FUNCTION__, __LINE__);
    return NaviErrors::PAGE_NOT_READABLE;
  }

  SIZE_T writtenbytes = 0;

  // Try to read the byte from process memory
  if (!ReadProcessMemory(hProcess, reinterpret_cast<void*>(offset), &b, 1,
                         &writtenbytes)) {
    restoreMemoryProtection(hProcess, offset, oldProtection);
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read process memory at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  restoreMemoryProtection(hProcess, offset, oldProtection);
  // std::cout << "Exiting: " << __FUNCTIONW__ << std::endl;
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::storeOriginalData(const BREAKPOINT& bp_in) {


  char tmp[50];

  sprintf(tmp, ADDRESS_FORMAT_MASK, bp_in.addr);

  if (originalDWORDs.find(tmp) != originalDWORDs.end()) {
    // Original data already stored
    return NaviErrors::SUCCESS;
  }

  unsigned int d;

  NaviError result = readDWORD(hProcess, bp_in.addr, d);

  if (result) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read byte from address %X at %s:%d",
                bp_in.addr, __FUNCTION__, __LINE__);
    return result;
  }

  originalDWORDs[tmp] = d;
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::readRegisters(RegisterContainer& registers) {

  msglog->log(LOG_VERBOSE, "Info: reading the register values of %d threads",
              tids.size());

  for (std::vector<Thread>::iterator Iter = tids.begin(); Iter != tids.end();
      ++Iter) {
    unsigned int tid = Iter->tid;

    Thread thread(tid, SUSPENDED);

    // Open the selected thread
    HANDLE hThread = (HANDLE) tid;

    if (!hThread) {
      msglog->log(LOG_VERBOSE, "Opening thread %d failed at %s:%d", tid,
                  __FUNCTION__, __LINE__);
    }

    CONTEXT ctx;

    ctx.ContextFlags = CONTEXT_FULL;

    DWORD oldPermission = 0;

    // Save the old permissions for later retore
    oldPermission = GetCurrentPermissions();

    // Get full thread access
    SetProcPermissions(0xFFFFFFFF);

    // Read the values of the registers of the thread
    if (GetThreadContext(hThread, &ctx) == FALSE) {
      msglog->log(LOG_ALWAYS,
                  "Error: GetThreadContext failed at %s:%d with (Code: %d).",
                  __FUNCTION__, __LINE__, GetLastError());
      CloseHandle(hThread);
      SetProcPermissions(oldPermission);
      return NaviErrors::COULDNT_READ_REGISTERS;
    }

    // Restore old thread access permissions
    SetProcPermissions(oldPermission);

    msglog->log(LOG_VERBOSE, "Info: assigning register values of thread %d",
                tid);

    thread.registers.push_back(
        makeRegisterValue("R0", zylib::zycon::toHexString(ctx.R0),
                          readPointedMemory(ctx.R0)));
    thread.registers.push_back(
        makeRegisterValue("R1", zylib::zycon::toHexString(ctx.R1),
                          readPointedMemory(ctx.R1)));
    thread.registers.push_back(
        makeRegisterValue("R2", zylib::zycon::toHexString(ctx.R2),
                          readPointedMemory(ctx.R2)));
    thread.registers.push_back(
        makeRegisterValue("R3", zylib::zycon::toHexString(ctx.R3),
                          readPointedMemory(ctx.R3)));
    thread.registers.push_back(
        makeRegisterValue("R4", zylib::zycon::toHexString(ctx.R4),
                          readPointedMemory(ctx.R4)));
    thread.registers.push_back(
        makeRegisterValue("R5", zylib::zycon::toHexString(ctx.R5),
                          readPointedMemory(ctx.R5)));
    thread.registers.push_back(
        makeRegisterValue("R6", zylib::zycon::toHexString(ctx.R6),
                          readPointedMemory(ctx.R6)));
    thread.registers.push_back(
        makeRegisterValue("R7", zylib::zycon::toHexString(ctx.R7),
                          readPointedMemory(ctx.R7)));
    thread.registers.push_back(
        makeRegisterValue("R8", zylib::zycon::toHexString(ctx.R8),
                          readPointedMemory(ctx.R8)));
    thread.registers.push_back(
        makeRegisterValue("R9", zylib::zycon::toHexString(ctx.R9),
                          readPointedMemory(ctx.R9)));
    thread.registers.push_back(
        makeRegisterValue("R10", zylib::zycon::toHexString(ctx.R10),
                          readPointedMemory(ctx.R10)));
    thread.registers.push_back(
        makeRegisterValue("R11", zylib::zycon::toHexString(ctx.R11),
                          readPointedMemory(ctx.R11)));
    thread.registers.push_back(
        makeRegisterValue("R12", zylib::zycon::toHexString(ctx.R12),
                          readPointedMemory(ctx.R12)));

    // mark SP as stack pointer
    thread.registers.push_back(
        makeRegisterValue("R13(SP)", zylib::zycon::toHexString(ctx.Sp),
                          readPointedMemory(ctx.Sp), false, true));
    thread.registers.push_back(
        makeRegisterValue("R14(LR)", zylib::zycon::toHexString(ctx.Lr),
                          readPointedMemory(ctx.Lr)));
    // mark PC as instruction pointer
    thread.registers.push_back(
        makeRegisterValue("R15(PC)", zylib::zycon::toHexString(ctx.Pc),
                          readPointedMemory(ctx.Pc), true));
    thread.registers.push_back(
        makeRegisterValue("R16(PSR)", zylib::zycon::toHexString(ctx.Psr)));

    thread.registers.push_back(
        makeRegisterValue("N", zylib::zycon::toHexString((ctx.Psr >> 31) & 1)));
    thread.registers.push_back(
        makeRegisterValue("Z", zylib::zycon::toHexString((ctx.Psr >> 30) & 1)));
    thread.registers.push_back(
        makeRegisterValue("C", zylib::zycon::toHexString((ctx.Psr >> 29) & 1)));
    thread.registers.push_back(
        makeRegisterValue("V", zylib::zycon::toHexString((ctx.Psr >> 28) & 1)));

    thread.registers.push_back(
        makeRegisterValue("I", zylib::zycon::toHexString((ctx.Psr >> 7) & 1)));
    thread.registers.push_back(
        makeRegisterValue("F", zylib::zycon::toHexString((ctx.Psr >> 6) & 1)));
    thread.registers.push_back(
        makeRegisterValue("T", zylib::zycon::toHexString((ctx.Psr >> 5) & 1)));

    thread.registers.push_back(
        makeRegisterValue("MODE", zylib::zycon::toHexString(ctx.Psr & 0x1F)));
    registers.addThread(thread);

    CloseHandle(hThread);
  }
  return NaviErrors::SUCCESS;
}

void getFiles(const std::wstring& path, std::vector<std::wstring>& files,
              std::vector<std::wstring>& dirs) {
  return;
}

NaviError WinCESystem::readProcessList(ProcessListContainer& processList) {
  HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

  if (hSnapshot == INVALID_HANDLE_VALUE) {
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }

  PROCESSENTRY32 pe;
  pe.dwSize = sizeof(PROCESSENTRY32);

  BOOL retval = Process32First(hSnapshot, &pe);

  while (retval) {
    unsigned long pid = pe.th32ProcessID;
    std::string name = TCharToString(pe.szExeFile);

    ProcessDescription process(pid, name);
    processList.push_back(process);

    pe.dwSize = sizeof(PROCESSENTRY32);
    retval = Process32Next(hSnapshot, &pe);
  }

  CloseToolhelp32Snapshot(hSnapshot);
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::readFiles(FileListContainer& fileList) {
  return NaviErrors::COULDNT_GET_FILELIST;
}

NaviError WinCESystem::readFiles(FileListContainer& fileList,
                                 const std::string& path) {
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::getInstructionPointer(unsigned int threadId,
                                             CPUADDRESS& addr) {

  CONTEXT threadContext;
  threadContext.ContextFlags = CONTEXT_FULL;
  DWORD oldPermission = 0;

  // Save old thread access permissions
  oldPermission = GetCurrentPermissions();

  // Set full thread access permissions
  SetProcPermissions(0xFFFFFFFF);

  if (GetThreadContext(hThread, &threadContext) == FALSE) {
    msglog->log(LOG_VERBOSE,
                "Error: GetThreadContext failed %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  // restore old thread access permissions
  SetProcPermissions(oldPermission);

  addr = threadContext.Pc;
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::resumeThread(unsigned int tid) {
  return
      WinCE_arm_resume(tid, DBG_CONTINUE) ?
          NaviErrors::SUCCESS : NaviErrors::COULDNT_RESUME_THREAD;
}

NaviError WinCESystem::suspendThread(unsigned int tid) {
  HANDLE handle = (HANDLE) tid;

  if (!handle) {
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }

  if (SuspendThread(handle) == -1) {
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }

  CloseHandle(handle);

  return NaviErrors::SUCCESS;
}

/**
 * Terminates the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinCESystem::terminateProcess() {


  DWORD orgPermissions = GetCurrentPermissions();
  SetProcPermissions(0xFFFFFFFF);

  if (!DebugActiveProcessStop(getPID())) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Couldn't stop receiving debug events at %s:%d with (Code: %d)",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::COULDNT_TERMINATE_TARGET_PROCESS;
  }

  if (!TerminateProcess(hProcess, 0)) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Coudn't terminate the target process at %s:%d with (Code: %d)",
        __FUNCTION__, __LINE__, GetLastError());
    return NaviErrors::COULDNT_TERMINATE_TARGET_PROCESS;
  }

  SetProcPermissions(orgPermissions);
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::doSingleStep(unsigned int& tid, CPUADDRESS& address) {


  CPUADDRESS nextpc;
  if (getNextInstructionAddress(tid, nextpc))
    return NaviErrors::COULDNT_SINGLE_STEP;

  BREAKPOINT bp;
  bp.addr = nextpc;
  bp.bpx_type = BPX_stepping;

  unsigned int opcode;
  if (readDWORD(hProcess, nextpc, opcode))
    return NaviErrors::COULDNT_SINGLE_STEP;

  if (setBreakpoint(bp))
    return NaviErrors::COULDNT_SINGLE_STEP;

  Sleep(100);

  NaviError resumeResult = resumeThread(tid);

  // Keep track of the last thread that caused the last debug event
  setActiveThread(tid);

  // writeDWORD uses CacheSync which we believe take a while :)
  Sleep(100);

  if (resumeResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't resume thread at %s:%d",
                __FUNCTION__, __LINE__);
    return resumeResult;
  }

  DEBUG_EVENT dbg_evt;

  // Attempt to catch the next exception (single-step event)
  if (!WinCE_arm_wait_for_debug_event(&dbg_evt, 1000)) {
    msglog->log(LOG_VERBOSE,
                "Error: The single step event did not occur in time at %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_SINGLE_STEP;
  }

  if (dbg_evt.dwDebugEventCode != EXCEPTION_DEBUG_EVENT) {
    msglog->log(
        LOG_VERBOSE,
        "Error: should've gotten EXCEPTION_BREAKPOINT, but got %d at %s:%d",
        dbg_evt.dwDebugEventCode, __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_SINGLE_STEP;
  }

  if (dbg_evt.u.Exception.ExceptionRecord.ExceptionCode
      != EXCEPTION_BREAKPOINT) {
    msglog->log(LOG_VERBOSE, "Error: Non BPX at %08X, continuing at %s:%d",
                dbg_evt.u.Exception.ExceptionRecord.ExceptionAddress,
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_SINGLE_STEP;
  }

  writeDWORD(hProcess, nextpc, opcode);
  address = nextpc;
  return NaviErrors::SUCCESS;
}

/**
 * Returns register descriptions of the target platform.
 *
 * @return The list of register descriptions.
 */
std::vector<RegisterDescription> WinCESystem::getRegisterNames() const {
  std::vector < RegisterDescription > regNames;

  RegisterDescription r0("R0", 4, true);
  RegisterDescription r1("R1", 4, true);
  RegisterDescription r2("R2", 4, true);
  RegisterDescription r3("R3", 4, true);
  RegisterDescription r4("R4", 4, true);
  RegisterDescription r5("R5", 4, true);
  RegisterDescription r6("R6", 4, true);
  RegisterDescription r7("R7", 4, true);
  RegisterDescription r8("R8", 4, true);
  RegisterDescription r9("R9", 4, true);
  RegisterDescription r10("R10", 4, true);
  RegisterDescription r11("R11", 4, true);
  RegisterDescription r12("R12", 4, true);

  RegisterDescription sp("R13(SP)", 4, true);
  RegisterDescription lr("R14(LR)", 4, true);
  RegisterDescription pc("R15(PC)", 4, true);
  RegisterDescription psr("R16(PSR)", 4, true);

  RegisterDescription nflag("N", 0, true);
  RegisterDescription zflag("Z", 0, true);
  RegisterDescription cflag("C", 0, true);
  RegisterDescription vflag("V", 0, true);
  RegisterDescription iflag("I", 0, true);
  RegisterDescription fflag("F", 0, true);
  RegisterDescription tflag("T", 0, true);
  RegisterDescription mode("MODE", 1, true);

  regNames.push_back(r0);
  regNames.push_back(r1);
  regNames.push_back(r2);
  regNames.push_back(r3);
  regNames.push_back(r4);
  regNames.push_back(r5);
  regNames.push_back(r6);
  regNames.push_back(r7);
  regNames.push_back(r8);
  regNames.push_back(r9);
  regNames.push_back(r10);
  regNames.push_back(r11);
  regNames.push_back(r12);

  regNames.push_back(lr);
  regNames.push_back(sp);
  regNames.push_back(pc);
  regNames.push_back(psr);

  regNames.push_back(nflag);
  regNames.push_back(zflag);
  regNames.push_back(cflag);
  regNames.push_back(vflag);
  regNames.push_back(iflag);
  regNames.push_back(fflag);
  regNames.push_back(tflag);
  regNames.push_back(mode);

  return regNames;
}

NaviError WinCESystem::setRegister(unsigned int tid, unsigned int index,
                                   CPUADDRESS address) {


  CONTEXT threadContext;

  threadContext.ContextFlags = CONTEXT_FULL;

  DWORD oldPermission = 0;

  // Save old thread access permissions
  oldPermission = GetCurrentPermissions();

  // Get full thread access permissions
  SetProcPermissions(0xFFFFFFFF);

  if (GetThreadContext(hThread, &threadContext) == FALSE) {
    msglog->log(LOG_VERBOSE,
                "Error: GetThreadContext failed %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  switch (index) {
    case 0:
      threadContext.R0 = address;
      break;
    case 1:
      threadContext.R1 = address;
      break;
    case 2:
      threadContext.R2 = address;
      break;
    case 3:
      threadContext.R3 = address;
      break;
    case 4:
      threadContext.R4 = address;
      break;
    case 5:
      threadContext.R5 = address;
      break;
    case 6:
      threadContext.R6 = address;
      break;
    case 7:
      threadContext.R7 = address;
      break;
    case 8:
      threadContext.R8 = address;
      break;
    case 9:
      threadContext.R9 = address;
      break;
    case 10:
      threadContext.R10 = address;
      break;
    case 11:
      threadContext.R11 = address;
      break;
    case 12:
      threadContext.R12 = address;
      break;

    case 13:
      threadContext.Sp = address;
      break;
    case 14:
      threadContext.Lr = address;
      break;
    case 15:
      threadContext.Pc = address;
      break;
    case 16:
      threadContext.Psr = address;
      break;

    default:
      return NaviErrors::INVALID_REGISTER_INDEX;
  }

  if (!SetThreadContext(hThread, &threadContext)) {
    msglog->log(LOG_VERBOSE,
                "Error: SetThreadContext failed at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }

  // Restore old thread access permissions
  SetProcPermissions(oldPermission);
  return NaviErrors::SUCCESS;
}

NaviError WinCESystem::getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                      CPUADDRESS& to) {


  // TODO: Improve - What if just a single page is allocated?
  unsigned int PAGE_SIZE = 0x1000;

  CPUADDRESS startOffset = start & (~(PAGE_SIZE - 1));

  CPUADDRESS current = startOffset;

  CPUADDRESS low = (unsigned int) current;
  CPUADDRESS high = (unsigned int) current;

  MEMORY_BASIC_INFORMATION mem;

  do {
    //TODO: ensure correct address is querried
    if (!VirtualQuery(reinterpret_cast<void*>(current), &mem,
                      sizeof(MEMORY_BASIC_INFORMATION))) {
      break;
    }

    if (mem.State != MEM_COMMIT) {
      break;
    }

    current -= PAGE_SIZE;
  } while (true);

  if (current == startOffset) {
    // No valid memory
    return NaviErrors::NO_VALID_MEMORY;
  }

  low = current + PAGE_SIZE;

  current = startOffset;

  do {
    //TODO: ensure correct address is querried
    if (!VirtualQuery(reinterpret_cast<void*>(current), &mem,
                      sizeof(MEMORY_BASIC_INFORMATION))) {
      break;
    }

    if (mem.State != MEM_COMMIT) {
      break;
    }

    current += PAGE_SIZE;
  } while (true);

  high = current;

  from = low;
  to = high;
  return low != high ? NaviErrors::SUCCESS : NaviErrors::NO_VALID_MEMORY;
}

unsigned int WinCESystem::getAddressSize() const {
  return 32;
}

NaviError WinCESystem::getMemmap(std::vector<CPUADDRESS>& addresses) {


  unsigned int consecutiveRegions = 0;
  unsigned int address = 0x00010000;  // first accessable address in slot0
  MEMORY_BASIC_INFORMATION mem;
  memset(&mem, 0, sizeof(mem));

  while (VirtualQuery(reinterpret_cast<void*>(address), &mem, sizeof(mem))) {
    if (mem.State == MEM_COMMIT) {
      ++consecutiveRegions;

      if (consecutiveRegions == 1) {
        msglog->log(LOG_VERBOSE, "Found memory section between %X and %X",
                    (CPUADDRESS) mem.BaseAddress,
                    (CPUADDRESS) mem.BaseAddress + mem.RegionSize - 1);

        addresses.push_back((CPUADDRESS) mem.BaseAddress);
        addresses.push_back(
            ((CPUADDRESS) mem.BaseAddress + mem.RegionSize - 1));
      } else {
        msglog->log(
            LOG_VERBOSE, "Extending memory section to %X",
            addresses[addresses.size() - 1] + (CPUADDRESS) mem.RegionSize);

        addresses[addresses.size() - 1] += (CPUADDRESS) mem.RegionSize;
      }
    } else {
      consecutiveRegions = 0;
    }

    address = (unsigned int) mem.BaseAddress + mem.RegionSize;
  }

  msglog->log(LOG_VERBOSE, "VirtualQuery() failed with %X", GetLastError());
  return NaviErrors::SUCCESS;
}

/**
 * Detaches from the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinCESystem::detach() {


  // Make sure to resume suspended processes before detaching
  // Otherwise it's going to crash
  if (getActiveThread()) {
    resumeThread(getActiveThread());
  }

    // Don't kill the process when detaching
  DebugSetProcessKillOnExit(false);

  // Stop receiving debug events from the target process
  DebugActiveProcessStop(getPID());
  return NaviErrors::SUCCESS;
}

DebuggerOptions WinCESystem::getDebuggerOptions() const {
  DebuggerOptions empty;
  empty.canMultithread = false;
  SYSTEM_INFO system_info;
  GetSystemInfo(&system_info);
  empty.pageSize = system_info.dwPageSize;
  return empty;
}

/**
 * Resumes the thread with the given thread ID.
 *
 * @param tid The thread ID of the thread.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError WinCESystem::resumeProcess() {


  // Tricky: We have to ignore tid here because we always
  // have to resume the process with the thread that caused
  // the last debug event.

  return
      WinCE_arm_resume(getActiveThread(), nextContinue) ?
          NaviErrors::SUCCESS : NaviErrors::COULDNT_RESUME_THREAD;
}

NaviError WinCESystem::getRegisterTable(unsigned int* registers) {


  CONTEXT ctx;

  ctx.ContextFlags = CONTEXT_FULL;

  DWORD oldPermission = 0;

  // Save old thread access permissions
  oldPermission = GetCurrentPermissions();

  // Get full thread access permissions
  SetProcPermissions(0xFFFFFFFF);

  if (GetThreadContext(hThread, &ctx) == FALSE) {
    msglog->log(LOG_VERBOSE,
                "Error: GetThreadContext failed at %s:%d with (Code: %d)",
                __FUNCTION__, __LINE__, GetLastError());
    SetProcPermissions(oldPermission);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  // Restore old thread access permissions
  SetProcPermissions(oldPermission);

  registers[0] = ctx.R0;
  registers[1] = ctx.R1;
  registers[2] = ctx.R2;
  registers[3] = ctx.R3;
  registers[4] = ctx.R4;
  registers[5] = ctx.R5;
  registers[6] = ctx.R6;
  registers[7] = ctx.R7;
  registers[8] = ctx.R8;
  registers[9] = ctx.R8;
  registers[10] = ctx.R10;
  registers[11] = ctx.R11;
  registers[12] = ctx.R12;
  registers[13] = ctx.Sp;
  registers[14] = ctx.Lr;
  registers[15] = ctx.Pc;
  registers[16] = ctx.Psr;

  registers[17] = ctx.Fpscr;
  registers[18] = ctx.FpExc;
  registers[18] = ctx.FpExc;

  return NaviErrors::SUCCESS;
}

//taken from arm-tdep.c of the gdb project
NaviError WinCESystem::getNextInstructionAddress(unsigned int tid,
                                                 CPUADDRESS& address) {
  unsigned int pc_val;
  unsigned int this_instr;
  unsigned int status;
  unsigned int registers[sizeof(CONTEXT) / sizeof(unsigned int) - 1];
  CPUADDRESS nextpc;
  CPUADDRESS pc;
  //TODO: implement thumb_get_next_pc
  /*if (arm_pc_is_thumb (pc))
   return thumb_get_next_pc (pc);
   */

  if (getRegisterTable(registers))
    return NaviErrors::COULDNT_READ_REGISTERS;

  pc = registers[15];
  status = registers[16];

  pc_val = (unsigned int) pc;
  readDWORD(hProcess, pc, this_instr);
  nextpc = (CPUADDRESS)(pc_val + 4); /* Default case */

  if (condition_true(bits(this_instr, 28, 31), status)) {
    switch (bits(this_instr, 24, 27)) {
      case 0x0:
      case 0x1: /* data processing */
      case 0x2:
      case 0x3: {
        unsigned int operand1, operand2, result = 0;
        unsigned int rn;
        int c;

        if (bits(this_instr, 12, 15) != 15)
          break;

        if (bits(this_instr, 22, 25) == 0 && bits(this_instr, 4, 7) == 9)
          msglog->log(LOG_VERBOSE, "ERROR: invalid update to pc at %s:%d",
                      __FUNCTION__, __LINE__);

        /* BX <reg>, BLX <reg> */
        if (bits(this_instr, 4, 28) == 0x12fff1
            || bits(this_instr, 4, 28) == 0x12fff3) {
          rn = bits(this_instr, 0, 3);
          result = (rn == 15) ? pc_val + 8 : registers[rn];
          nextpc = (CPUADDRESS) arm_addr_bits_remove(result);

          if (nextpc == pc)
            msglog->log(LOG_VERBOSE, "ERROR: infinite loop detected at %s:%d",
                        __FUNCTION__, __LINE__);

          address = nextpc;
          return NaviErrors::SUCCESS;
        }

        /* Multiply into PC */
        c = (status & FLAG_C) ? 1 : 0;
        rn = bits(this_instr, 16, 19);
        operand1 = (rn == 15) ? pc_val + 8 : registers[rn];

        if (bit(this_instr, 25)) {
          unsigned int immval = bits(this_instr, 0, 7);
          unsigned int rotate = 2 * bits(this_instr, 8, 11);
          operand2 = ((immval >> rotate) | (immval << (32 - rotate)))
              & 0xffffffff;
        } else {
          /* operand 2 is a shifted register */
          operand2 = shifted_reg_val(this_instr, c, pc_val, status, registers);
        }

        switch (bits(this_instr, 21, 24)) {
          case 0x0: /*and */
            result = operand1 & operand2;
            break;

          case 0x1: /*eor */
            result = operand1 ^ operand2;
            break;

          case 0x2: /*sub */
            result = operand1 - operand2;
            break;

          case 0x3: /*rsb */
            result = operand2 - operand1;
            break;

          case 0x4: /*add */
            result = operand1 + operand2;
            break;

          case 0x5: /*adc */
            result = operand1 + operand2 + c;
            break;

          case 0x6: /*sbc */
            result = operand1 - operand2 + c;
            break;

          case 0x7: /*rsc */
            result = operand2 - operand1 + c;
            break;

          case 0x8:
          case 0x9:
          case 0xa:
          case 0xb: /* tst, teq, cmp, cmn */
            result = (unsigned int) nextpc;
            break;

          case 0xc: /*orr */
            result = operand1 | operand2;
            break;

          case 0xd: /*mov */
            /* Always step into a function.  */
            result = operand2;
            break;

          case 0xe: /*bic */
            result = operand1 & ~operand2;
            break;

          case 0xf: /*mvn */
            result = ~operand2;
            break;
        }
        nextpc = (CPUADDRESS) arm_addr_bits_remove(result);

        if (nextpc == pc)
          msglog->log(LOG_VERBOSE, "ERROR: infinite loop detected at %s:%d",
                      __FUNCTION__, __LINE__);
        break;
      }

      case 0x4:
      case 0x5: /* data transfer */
      case 0x6:
      case 0x7:
        if (bit(this_instr, 20)) {
          /* load */
          if (bits(this_instr, 12, 15) == 15) {
            /* rd == pc */
            unsigned int rn;
            unsigned int base;

            if (bit(this_instr, 22))
              msglog->log(LOG_VERBOSE, "Error: infinite loop detected at %s:%d",
                          __FUNCTION__, __LINE__);

            /* byte write to PC */
            rn = bits(this_instr, 16, 19);
            base = (rn == 15) ? pc_val + 8 : registers[rn];
            if (bit(this_instr, 24)) {
              /* pre-indexed */
              int c = (status & FLAG_C) ? 1 : 0;
              unsigned int offset = (
                  bit(this_instr, 25) ?
                      shifted_reg_val(this_instr, c, pc_val, status,
                                      registers) :
                      bits(this_instr, 0, 11));

              if (bit(this_instr, 23))
                base += offset;
              else
                base -= offset;
            }
            readDWORD(hProcess, (CPUADDRESS) base, nextpc);
            nextpc = arm_addr_bits_remove(nextpc);

            if (nextpc == pc)
              msglog->log(LOG_VERBOSE, "Error: infinite loop detected at %s:%d",
                          __FUNCTION__, __LINE__);
          }
        }
        break;

      case 0x8:
      case 0x9: /* block transfer */
        if (bit(this_instr, 20)) {
          /* LDM */
          if (bit(this_instr, 15)) {
            /* loading pc */
            int offset = 0;

            if (bit(this_instr, 23)) {
              /* up */
              unsigned int reglist = bits(this_instr, 0, 14);
              offset = bitcount(reglist) * 4;
              if (bit(this_instr, 24)) /* pre */
                offset += 4;
            } else if (bit(this_instr, 24))
              offset = -4;

            {
              unsigned int rn_val = registers[bits(this_instr, 16, 19)];
              readDWORD(hProcess, (CPUADDRESS)(rn_val + offset), nextpc);
            }
            nextpc = arm_addr_bits_remove(nextpc);
            if (nextpc == pc)
              msglog->log(LOG_VERBOSE, "Error: infinite loop detected at %s:%d",
                          __FUNCTION__, __LINE__);
          }
        }
        break;

      case 0xb: /* branch & link */
      case 0xa: /* branch */
      {
        nextpc = BranchDest(pc, this_instr);

        /* BLX */
        if (bits(this_instr, 28, 31) == INST_NV)
          nextpc |= bit(this_instr, 24) << 1;

        nextpc = arm_addr_bits_remove(nextpc);
        if (nextpc == pc)
          msglog->log(LOG_VERBOSE, "Error: infinite loop detected at %s:%d",
                      __FUNCTION__, __LINE__);
        break;
      }

      case 0xc:
      case 0xd:
      case 0xe: /* coproc ops */
      case 0xf: /* SWI */
        break;

      default:
        msglog->log(LOG_VERBOSE, "Error: bad bit field extraction at %s:%d",
                    __FUNCTION__, __LINE__);
        address = pc;
    }
  }
  address = nextpc;

  return NaviErrors::SUCCESS;
}
