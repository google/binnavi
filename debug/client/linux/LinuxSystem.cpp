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

#include <boost/foreach.hpp>
#include "LinuxSystem.hpp"

#include <fstream>
#include <string>
#include <cstdlib>

#include <zycon/src/zycon.h>

#include <dirent.h>

#include <sys/stat.h>
#include <sys/ptrace.h>
#include <sys/user.h>
#include <sys/wait.h>

#include <errno.h>

#include "../logger.hpp"
#include "../DebuggerOptions.hpp"
#include <unistd.h>

#define PTRACE_GETSIGINFO 1

union DATA {
  unsigned int value;
  unsigned char chars[sizeof(unsigned int)];
};

#define STOP_PROCESS_IF_NECESSARY 	int stop_ctr = ptrace_stop(getPID()); \
					if (stop_ctr == -1) \
					{ \
						msglog->log(LOG_ALWAYS, "Error: Couldn't stop target process"); \
					} \
					else if (stop_ctr) \
					{ \
						wait(0); \
					}

#define RESUME_PROCESS_IF_NECESSARY	if (stop_ctr) \
					{ \
						msglog->log(LOG_VERBOSE, "Resuming process"); \
						resumeProcess(); \
					} \

unsigned int getFileSize(const std::string& filename) {
  std::ifstream file(filename.c_str(), std::ios_base::binary);

  file.seekg(-1);

  return file.tellg();
}

int ptrace_stop(unsigned int pid) {
  int tries = 0;
  int ret = 0;

  while (tries < 10) {

    msglog->log(LOG_VERBOSE, "Trying to stop process %d (try: %d)", pid, tries);

    /* try to read from address NULL */
    ptrace(PTRACE_PEEKTEXT, pid, 0, 0);

    /*
     * ESRCH is only done on:
     * - non existing
     * - non traced
     * - stopped process
     * errno == 0 means it was successful
     * EFAULT and EIO means invalid address, the expected error
     */
    if ((errno == 0) || (errno == EFAULT) || (errno == EIO)) {
      return tries;
    }
    if (errno != ESRCH) {
      msglog->log(LOG_VERBOSE,
                  "Unknown reply %d (%s) to PTRACE_PEEKTEXT request", errno,
                  strerror(errno));
      msglog->log(LOG_VERBOSE, "Assuming the process isn't stopped");
      return 1;
    }

    msglog->log(LOG_VERBOSE, "Stopping the process now");

    /* Ok so it's not stopped, stop it */
    ret = 1;
    if (kill(pid, SIGSTOP)) {
      /*
       * XXX can't continue in fact, need better error
       * handling
       */
      exit(-1);
    }

    /* Two options:
     * - We were for some reason stopped, we will never get a
     *   sigchld, the sleep will run it's course
     * - We get a SIGCHLD stopping the sleep early
     * - SIGCHLD arrives before we enter sleep -> we will have to
     *   wait as well,
     * XXX lower the waiting time.
     * YYY lowered to 1ms noticed during multi threaded operations
     */
    usleep(1000);

    ++tries;
  }
  return -1;
}

std::string getModuleName(const std::string& path) {
  size_t lastSlash = path.rfind("/");

  if (lastSlash == std::string::npos) {
    return path;
  } else {
    return path.substr(lastSlash + 1);
  }
}

/**
 * Determine all loaded modules as well as the process module itself for the given process id.
 */
NaviError LinuxSystem::fillModules(pid_t pid, ModulesMap& modules) {
  std::string filename = "/proc/" + zylib::zycon::toString(pid) + "/maps";

  msglog->log(LOG_ALL, "Trying to read the modules map from file %s",
              filename.c_str());

  std::ifstream file(filename.c_str());

  if (!file) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read modules map file");

    return NaviErrors::COULDNT_READ_MEMORY;
  }

  std::vector < std::string > lines;

  std::string line;

  while (std::getline(file, line)) {
    msglog->log(LOG_ALL, "Successfully read line %s from modules map file",
                line.c_str());

    lines.push_back(line);
  }

// 08048000-08053000 r-xp 00000000 08:01 282412     /usr/bin/kdeinit
// 08053000-08054000 r--p 0000a000 08:01 282412     /usr/bin/kdeinit
// 08054000-08055000 rw-p 0000b000 08:01 282412     /usr/bin/kdeinit
// 09f73000-0a016000 rw-p 09f73000 00:00 0          [heap]
// b6638000-b6672000 rw-p b6672000 00:00 0
// b66bb000-b66d9000 r-xp 00000000 08:01 352021     /usr/lib/kde3/plugins/styles/plastik.so
// b66d9000-b66da000 r--p 0001d000 08:01 352021     /usr/lib/kde3/plugins/styles/plastik.so
// b66da000-b66db000 rw-p 0001e000 08:01 352021     /usr/lib/kde3/plugins/styles/plastik.so
// b66db000-b66e1000 r--s 00000000 08:01 396230     /var/cache/fontconfig/945677eb7aeaf62f1d50efc3fb3ec7d8-x86.cache-2
// b66e1000-b66e4000 r--s 00000000 08:01 396239     /var/cache/fontconfig/e383d7ea5fbe662a33d9b44caf393297-x86.cache-2
// b66e4000-b66e5000 r--s 00000000 08:01 396225     /var/cache/fontconfig/4c73fe0c47614734b17d736dbde7580a-x86.cache-2
// b66e5000-b66e8000 r--s 00000000 08:01 396231     /var/cache/fontconfig/a755afe4a08bf5b97852ceb7400b47bc-x86.cache-2
// b66e8000-b66ef000 r--s 00000000 08:01 396608     /var/cache/fontconfig/6d41288fd70b0be22e8c3a91e032eec0-x86.cache-2
// b66ef000-b66f7000 r--s 00000000 08:01 396240     /var/cache/fontconfig/e3de0de479f42330eadf588a55fb5bf4-x86.cache-2
// b66f7000-b6702000 r--s 00000000 08:01 396220     /var/cache/fontconfig/0f34bcd4b6ee430af32735b75db7f02b-x86.cache-2
// b6702000-b6709000 r--s 00000000 08:01 396234     /var/cache/fontconfig/d52a8644073d54c13679302ca1180695-x86.cache-2

  std::string lastName = "";
  CPUADDRESS start = 0;
  CPUADDRESS end = 0;

  for (std::vector<std::string>::iterator Iter = lines.begin();
      Iter != lines.end(); ++Iter) {
    std::string line = *Iter;

    size_t position = line.find("/");

    if (position != std::string::npos) {
      // OK, we found a line with a name; now we gotta figure out if we found a new module
      // or just with a new section in the same module.

      std::string name = line.substr(position);

      // TODO: Only works in 32bit platforms
      std::string startString = Iter->substr(0, 8);
      std::string endString = Iter->substr(9, 8);

      if (name != lastName) {
        // We found a new module, so we can take the information from the previous
        // module and add  the previous module to the list of modules.

        if (lastName != "") {
          Module newModule(getModuleName(lastName), lastName, start,
                           end - start);
          modules.insert(std::make_pair(lastName, newModule));
        }

        lastName = name;
        start = zylib::zycon::parseHexString < CPUADDRESS > (startString);
        end = zylib::zycon::parseHexString < CPUADDRESS > (endString);
      } else {
        // The module in the current line is the same module we already processed
        // in the previous line. Only the end information has to be updated in this
        // case.

        end = zylib::zycon::parseHexString < CPUADDRESS > (endString);
      }
    } else if (lastName != "") {
      // We found a line without a name and the previous module has
      // a valid name => Add the previous module to the list of modules.

      Module newModule(getModuleName(lastName), lastName, start, end - start);
      modules.insert(std::make_pair(lastName, newModule));

      lastName = "";
    }
  }

  if (lastName != "") {
    // Add the last module in the list.

    Module newModule(getModuleName(lastName), lastName, start, end - start);
    modules.insert(std::make_pair(lastName, newModule));
  }

  return NaviErrors::SUCCESS;
}

/**
 * Attaches to the target process.
 *
 * @param tids The thread IDs of the threads that belong to the target process.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::attachToProcess() {


  msglog->log(LOG_VERBOSE, "Trying to attach to process %d", getPID());

  int result = ptrace(PTRACE_ATTACH, getPID(), 0, 0);

  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't attach to process");
    return errno;
  }

  // TODO: Right now multi-threading is not supported
  Thread ts(getPID(), SUSPENDED);
  tids.push_back(ts);
  setActiveThread (getPID());

std  ::string path;
  NaviError pathError = getExecutablePath(getPID(), path);
  if (pathError) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Unable to determine the executable path of the debuggee.");
    return pathError;
  }

  // Generate processStart message and send it to BinNavi.
  fillModules(getPID(), this->modules);
  std::map<std::string, Module>::const_iterator cit = this->modules.find(path);
  if (cit != this->modules.end()) {
    Module processModule = cit->second;
    processStart(processModule, ts);
  } else {
    msglog->log(LOG_ALWAYS,
                "Error: Unable to determine main process module for '%s'",
                path.c_str());
    exit(0);
  }

  return NaviErrors::SUCCESS;
}

/**
 * Starts a new process for debugging.
 *
 * @param path The path to the executable of the process.
 * @param tids The thread IDs of the threads that belong to the target process.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::startProcess(
    const NATIVE_STRING path,
    const std::vector<const NATIVE_STRING>& commands) {


  pid_t pid = fork();

  if (pid == -1) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't fork process");
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  } else if (pid == 0) {
    ptrace(PTRACE_TRACEME, 0, 0, 0);

    char** child_arguments = new char*[1 + commands.size() + 1];
    child_arguments[0] = new char[strlen(path) + 1];
    strcpy(child_arguments[0], path);

    for (unsigned int i = 0; i < commands.size(); i++) {
      child_arguments[i + 1] = new char[strlen(commands[i]) + 1];
      strcpy(child_arguments[i + 1], commands[i]);
    }

    child_arguments[1 + commands.size()] = 0;

    // Child process
    if (execvp(path, child_arguments) == -1) {
      msglog->log(LOG_ALWAYS, "Error: Could not start the child process '%s'",
                  path);
      exit(0);
    }

    return NaviErrors::SUCCESS;
  } else {
    int status;
    if (waitpid(pid, &status, __WALL) == -1) {
      msglog->log(LOG_ALWAYS, "Error: Wait for target process failed '%s'",
                  path);
      exit(0);
    }

    if (WIFSTOPPED(status)) {
      if (WSTOPSIG(status) == SIGTRAP) {
        msglog->log(LOG_VERBOSE, "Initial STOP signal received");
      } else {
        msglog->log(LOG_ALWAYS, "Error: Received unexpected STOP signal");
        exit(0);
      }
    } else {
      msglog->log(LOG_ALWAYS, "Error: Did not receive initial STOP signal");
      exit(0);
    }

    if (ptrace(
        PTRACE_SETOPTIONS,
        pid,
        0,
        PTRACE_O_TRACECLONE | PTRACE_O_TRACEFORK | PTRACE_O_TRACEVFORK
            | PTRACE_O_TRACEVFORKDONE) == -1) {
      msglog->log(LOG_ALWAYS, "Error: Could not set ptrace options");
      exit(0);
    }

    msglog->log(LOG_VERBOSE, "PID of the child process is %d", pid);

    setPID(pid);
    Thread ts(pid, SUSPENDED);
    tids.push_back(ts);
    setActiveThread(pid);

    lastMapFileSize = getFileSize(
        "/proc/" + zylib::zycon::toString(pid) + "/maps");

    fillModules(pid, modules);
    this->modules = modules;

    std::map<std::string, Module>::const_iterator cit = this->modules.find(
        getTargetApplicationPath().string());
    if (cit != this->modules.end()) {
      Module processModule = cit->second;
      processStart(processModule, ts);
    } else {
      msglog->log(LOG_ALWAYS,
                  "Error: Unable to determine main process module for '%s'",
                  getTargetApplicationPath().string().c_str());
      exit(0);
    }

    return NaviErrors::SUCCESS;
  }
}

/**
 * Detaches from the target process.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::detach() {


  msglog->log(LOG_VERBOSE, "Trying to detach from process %d", getPID());

  STOP_PROCESS_IF_NECESSARY

  int result = ptrace(PTRACE_DETACH, getPID(), 0, 0);

  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't detach from target process %d",
                getPID());
    return NaviErrors::COULDNT_DETACH;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Terminates the target process.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::terminateProcess() {


  ptrace(PTRACE_KILL, getPID(), 0, 0);

  return NaviErrors::SUCCESS;
}

/**
 * Stores the original data that is replaced by a given breakpoint.
 *
 * @param bp The breakpoint in question.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::storeOriginalData(const BREAKPOINT& bp) {


  std::string addressString = cpuAddressToString(bp.addr);

  msglog->log(LOG_VERBOSE, "Trying to store the original data of address %s",
              addressString.c_str());

  if (originalBytes.find(addressString) != originalBytes.end()) {
    // Already backed up.
    return NaviErrors::SUCCESS;
  }

  DATA data;

  STOP_PROCESS_IF_NECESSARY

  data.value = ptrace(PTRACE_PEEKTEXT, getPID(), bp.addr, NULL);

  RESUME_PROCESS_IF_NECESSARY

  if (errno) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read byte from address %s",
                addressString.c_str());
    return NaviErrors::COULDNT_READ_MEMORY;
  }

  originalBytes[addressString] = data.value;

  return NaviErrors::SUCCESS;
}

/**
 * Sets a breakpoint in the target process.
 *
 * @param breakpoint The breakpoint to be set.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::setBreakpoint(const BREAKPOINT& bp, bool) {


  std::string addressString = cpuAddressToString(bp.addr);

  msglog->log(LOG_VERBOSE, "Trying to set a breakpoint at address %s",
              addressString.c_str());

  DATA data;

  data.value = originalBytes[addressString];

  // TODO: Not platform independent
  data.chars[0] = 0xCC;

  STOP_PROCESS_IF_NECESSARY

  int result = ptrace(PTRACE_POKETEXT, getPID(), bp.addr, data.value);

  RESUME_PROCESS_IF_NECESSARY

  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't set breakpoint at address %s",
                addressString.c_str());
    return NaviErrors::COULDNT_SET_BREAKPOINT;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Removes a breakpoint from the target process.
 *
 * @param bp The breakpoint to be removed.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::removeBreakpoint(const BREAKPOINT& bp, bool) {


  std::string addressString = cpuAddressToString(bp.addr);

  unsigned int value = originalBytes[addressString];

  msglog->log(LOG_VERBOSE,
              "Removing breakpoint at address %s with original value %08X",
              addressString.c_str(), value);

  STOP_PROCESS_IF_NECESSARY

  int result = ptrace(PTRACE_POKETEXT, getPID(), bp.addr, value);

  if (result) {
    msglog->log(LOG_VERBOSE,
                "Failed to remove breakpoint with result %d (errno: %d)",
                result, errno);
  } else {
    msglog->log(LOG_VERBOSE, "Successfully removed breakpoint");
  }

  DATA data;

  data.value = ptrace(PTRACE_PEEKTEXT, getPID(), bp.addr, NULL);

  msglog->log(LOG_VERBOSE, "New memory value at address %s is %08X",
              addressString.c_str(), data.value);

  RESUME_PROCESS_IF_NECESSARY

  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't remove breakpoint");
    return NaviErrors::COULDNT_REMOVE_BREAKPOINT;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Executes a single instruction.
 *
 * @param tid The thread ID of the thread that executes the instruction.
 * @param address The address of the instruction pointer after the single step.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::doSingleStep(unsigned int& pid, CPUADDRESS& address) {


  msglog->log(LOG_VERBOSE, "Trying to do single step");

  STOP_PROCESS_IF_NECESSARY

  if (ptrace(PTRACE_SINGLESTEP, getPID(), 0, 0)) {
    RESUME_PROCESS_IF_NECESSARY

    msglog->log(LOG_ALWAYS, "Error: Couldn't perform single step");
    return NaviErrors::COULDNT_SINGLE_STEP;
  }

  wait(0);

  RESUME_PROCESS_IF_NECESSARY

  NaviError eipResult = getInstructionPointer(getPID(), address);

  if (eipResult) {
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  return NaviErrors::SUCCESS;
}

NaviError LinuxSystem::resumeProcess() {


  int resumeResult = ptrace(PTRACE_CONT, getPID(), 0, 0);

  if (resumeResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't resume process %d", getPID());
    return NaviErrors::COULDNT_RESUME_THREAD;
  }

  return NaviErrors::SUCCESS;
}

NaviError LinuxSystem::suspendThread(unsigned int tid) {


  msglog->log(LOG_VERBOSE, "Trying to resume process %d", tid);

  return 1;
}

/**
 * Resumes the thread with the given thread ID.
 *
 * @param tid The thread ID of the thread.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::resumeThread(unsigned int tid) {


  msglog->log(LOG_VERBOSE, "Trying to resume thread %d", tid);

  return
      ptrace(PTRACE_CONT, tid, 0, 0) != -1 ?
          NaviErrors::SUCCESS : NaviErrors::COULDNT_RESUME_THREAD;
}

/**
 * Retrieves the value of the instruction pointer in a given thread.
 *
 * @param tid The thread ID of the thread.
 * @param addr The variable where the value of the instruction pointer is stored.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::getInstructionPointer(unsigned int tid,
                                             CPUADDRESS& addr) {


  msglog->log(LOG_VERBOSE, "Trying to read the instruction pointer");

  user_regs_struct regs;

  if (ptrace(PTRACE_GETREGS, getPID(), 0, &regs)) {
    msglog->log(LOG_ALWAYS, "get_eip: PTRACE_GETREGS: %s", strerror(errno));
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  addr = regs.eip;

  return NaviErrors::SUCCESS;
}

/**
 * Sets the instruction pointer in the target process to a new value.
 *
 * @param tid Thread ID of the target thread.
 * @param address The new value of the instruction pointer.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::setInstructionPointer(unsigned int tid,
                                             CPUADDRESS address) {


  msglog->log(LOG_VERBOSE,
              "Trying to set the instruction pointer to address %08X", address);

  STOP_PROCESS_IF_NECESSARY

  user_regs_struct regs;

  if (ptrace(PTRACE_GETREGS, getPID(), 0, &regs)) {
    RESUME_PROCESS_IF_NECESSARY

    msglog->log(LOG_ALWAYS, "Error: Couldn't get register value: %s",
                strerror(errno));
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  // TODO: Not platform independent
  regs.eip = address;

  if (ptrace(PTRACE_SETREGS, getPID(), 0, &regs)) {
    RESUME_PROCESS_IF_NECESSARY

    msglog->log(LOG_ALWAYS, "Couldn't set register value: %s", strerror(errno));
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }

  RESUME_PROCESS_IF_NECESSARY

  return NaviErrors::SUCCESS;
}

std::vector<char> LinuxSystem::readPointedMemory(CPUADDRESS address) {


  unsigned int currentSize = 128;

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
 * Fills a given register container structure with information about the
 * current values of the CPU registers.
 *
 * @param registers The register information structure.
 * @param tid The thread ID from which the data is read.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::readRegisters(RegisterContainer& registers) {


  user_regs_struct regs;

  STOP_PROCESS_IF_NECESSARY

  if (ptrace(PTRACE_GETREGS, getPID(), 0, &regs)) {
    RESUME_PROCESS_IF_NECESSARY

    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  Thread thread(getPID(), SUSPENDED);

  thread.registers.push_back(
      makeRegisterValue("EAX", zylib::zycon::toHexString(regs.eax),
                        readPointedMemory(regs.eax)));
  thread.registers.push_back(
      makeRegisterValue("EBX", zylib::zycon::toHexString(regs.ebx),
                        readPointedMemory(regs.ebx)));
  thread.registers.push_back(
      makeRegisterValue("ECX", zylib::zycon::toHexString(regs.ecx),
                        readPointedMemory(regs.ecx)));
  thread.registers.push_back(
      makeRegisterValue("EDX", zylib::zycon::toHexString(regs.edx),
                        readPointedMemory(regs.edx)));
  thread.registers.push_back(
      makeRegisterValue("ESI", zylib::zycon::toHexString(regs.esi),
                        readPointedMemory(regs.esi)));
  thread.registers.push_back(
      makeRegisterValue("EDI", zylib::zycon::toHexString(regs.edi),
                        readPointedMemory(regs.edi)));
  thread.registers.push_back(
      makeRegisterValue("ESP", zylib::zycon::toHexString(regs.esp),
                        readPointedMemory(regs.esp), false, true));
  thread.registers.push_back(
      makeRegisterValue("EBP", zylib::zycon::toHexString(regs.ebp),
                        readPointedMemory(regs.ebp)));
  thread.registers.push_back(
      makeRegisterValue("EIP", zylib::zycon::toHexString(regs.eip),
                        readPointedMemory(regs.eip), true));
  thread.registers.push_back(
      makeRegisterValue("EFLAGS", zylib::zycon::toHexString(regs.eflags)));
  thread.registers.push_back(
      makeRegisterValue("CF", zylib::zycon::toHexString(regs.eflags & 1)));
  thread.registers.push_back(
      makeRegisterValue("PF",
                        zylib::zycon::toHexString((regs.eflags >> 2) & 1)));
  thread.registers.push_back(
      makeRegisterValue("AF",
                        zylib::zycon::toHexString((regs.eflags >> 4) & 1)));
  thread.registers.push_back(
      makeRegisterValue("ZF",
                        zylib::zycon::toHexString((regs.eflags >> 6) & 1)));
  thread.registers.push_back(
      makeRegisterValue("SF",
                        zylib::zycon::toHexString((regs.eflags >> 7) & 1)));
  thread.registers.push_back(
      makeRegisterValue("OF",
                        zylib::zycon::toHexString((regs.eflags >> 11) & 1)));

  registers.addThread(thread);

  RESUME_PROCESS_IF_NECESSARY

  return NaviErrors::SUCCESS;
}

/**
 * Updates the value of a given register in a given thread.
 *
 * @param tid The thread ID of the thread.
 * @param index The index of the register.
 * @param value The new value of the register.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::setRegister(unsigned int tid, unsigned int index,
                                   CPUADDRESS address) {


  msglog->log(LOG_VERBOSE, "Trying to set register %d to %X", index, address);

  STOP_PROCESS_IF_NECESSARY

  user_regs_struct regs;

  if (ptrace(PTRACE_GETREGS, getPID(), 0, &regs)) {
    RESUME_PROCESS_IF_NECESSARY

    msglog->log(LOG_ALWAYS, "Couldn't get register value: %s", strerror(errno));
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  switch (index) {
    case 0:
      regs.eax = address;
      break;
    case 1:
      regs.ebx = address;
      break;
    case 2:
      regs.ecx = address;
      break;
    case 3:
      regs.edx = address;
      break;
    case 4:
      regs.esi = address;
      break;
    case 5:
      regs.edi = address;
      break;
    case 6:
      regs.esp = address;
      break;
    case 7:
      regs.ebp = address;
      break;
    case 8:
      regs.eip = address;
      break;
    case 10:
      regs.eflags = (regs.eflags & 0xFFFFFFFE) | (address & 1);
      break;
    case 11:
      regs.eflags = (regs.eflags & 0xFFFFFFFB) | ((address & 1) << 2);
      break;
    case 12:
      regs.eflags = (regs.eflags & 0xFFFFFFEF) | ((address & 1) << 4);
      break;
    case 13:
      regs.eflags = (regs.eflags & 0xFFFFFFBF) | ((address & 1) << 6);
      break;
    case 14:
      regs.eflags = (regs.eflags & 0xFFFFFF7F) | ((address & 1) << 7);
      break;
    case 15:
      regs.eflags = (regs.eflags & 0xFFFFF7FF) | ((address & 1) << 11);
      break;
    default:
      return NaviErrors::INVALID_REGISTER_INDEX;
  }

  if (ptrace(PTRACE_SETREGS, getPID(), 0, &regs)) {
    RESUME_PROCESS_IF_NECESSARY

    msglog->log(LOG_ALWAYS, "Couldn't set register value: %s", strerror(errno));
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }

  RESUME_PROCESS_IF_NECESSARY

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
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                      CPUADDRESS& to) {


  msglog->log(LOG_VERBOSE,
              "Trying to find the valid memory range of address %X", start);

  unsigned int startOffset = start & (~(PAGE_SIZE - 1));

  unsigned int current = startOffset;

  unsigned int low = (unsigned int) current;
  unsigned int high = (unsigned int) current;

  STOP_PROCESS_IF_NECESSARY

  DATA data;

  do {
    data.value = ptrace(PTRACE_PEEKTEXT, getPID(), current, NULL);

    if (errno) {
      break;
    }

    current -= PAGE_SIZE;
  } while (true);

  if (current == startOffset) {
    // No valid memory

    RESUME_PROCESS_IF_NECESSARY

    return NaviErrors::NO_VALID_MEMORY;
  }

  low = current + PAGE_SIZE;

  current = startOffset;

  do {
    data.value = ptrace(PTRACE_PEEKTEXT, getPID(), current, NULL);

    if (errno) {
      break;
    }

    current += PAGE_SIZE;
  } while (true);

  high = current;

  from = low;
  to = high;

  RESUME_PROCESS_IF_NECESSARY

  return low != high ? NaviErrors::SUCCESS : NaviErrors::NO_VALID_MEMORY;
}

/**
 * Returns a list of all memory regions that are available in the target process.
 *
 * @param addresses The memory map is written into this list.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::getMemmap(std::vector<CPUADDRESS>& addresses) {
  std::string filename = "/proc/" + zylib::zycon::toString(getPID()) + "/maps";

  msglog->log(LOG_ALL, "Trying to read the memory map from file %s",
              filename.c_str());

  std::ifstream file(filename.c_str());

  if (!file) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read memory map file");

    return NaviErrors::COULDNT_READ_MEMORY;
  }

  std::vector < std::string > lines;

  std::string line;

  while (std::getline(file, line)) {
    msglog->log(LOG_ALL, "Successfully read line %s from memory map file",
                line.c_str());

    lines.push_back(line);
  }

  CPUADDRESS lastEnd = 0;

  for (std::vector<std::string>::iterator Iter = lines.begin();
      Iter != lines.end(); ++Iter) {
    // TODO: Only works in 32bit platforms
    std::string startString = Iter->substr(0, 8);
    std::string endString = Iter->substr(9, 8);

    CPUADDRESS start = zylib::zycon::parseHexString < CPUADDRESS
        > (startString);
    CPUADDRESS end = zylib::zycon::parseHexString < CPUADDRESS > (endString);

    if (start == lastEnd) {
      msglog->log(LOG_VERBOSE, "Extending memory section to %X", end);

      addresses[addresses.size() - 1] = end - 1;
    } else {
      msglog->log(LOG_VERBOSE, "Found memory section between %X and %X", start,
                  end - 1);

      addresses.push_back(start);
      addresses.push_back(end - 1);
    }

    lastEnd = end;
  }

  return NaviErrors::SUCCESS;
}

NaviError LinuxSystem::halt() {
  STOP_PROCESS_IF_NECESSARY

  return NaviErrors::SUCCESS;
}

/**
 * Fills a buffer with memory data from the current process.
 *
 * @param buffer The buffer to fill.
 * @param address The address from where the memory is read.
 * @param size Number of bytes to read.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::readMemoryData(char* buffer, CPUADDRESS address,
                                      CPUADDRESS size) {


  msglog->log(LOG_ALL, "Trying to read %d bytes of memory from address %X",
              size, address);

  unsigned int len = size;

  STOP_PROCESS_IF_NECESSARY

  DATA data;

  // Read the memory in chunks
  for (unsigned int i = 0; i < len; i += sizeof(int)) {
    unsigned int offset = address + i;

    data.value = ptrace(PTRACE_PEEKTEXT, getPID(), offset, NULL);

    if (errno) {
      RESUME_PROCESS_IF_NECESSARY

      msglog->log(
          LOG_VERBOSE,
          "Error: Couldn't read memory (I) from address 0x%X (%u bytes)",
          address, size);
      return NaviErrors::COULDNT_READ_MEMORY;
    }

    memcpy(buffer + i, &data.chars[0], sizeof(unsigned int));
  }

  // Maybe some bytes are left.
  unsigned int toCopy = len % sizeof(int);

  if (toCopy != 0) {
    unsigned int offset = address + size - toCopy;

    data.value = ptrace(PTRACE_PEEKTEXT, getPID(), offset, NULL);

    if (errno) {
      RESUME_PROCESS_IF_NECESSARY

      msglog->log(
          LOG_ALWAYS,
          "Error: Couldn't read memory (II) from address 0x%X (%u bytes)",
          address, size);
      return NaviErrors::COULDNT_READ_MEMORY;
    }

    memcpy(buffer + len - toCopy, &data.chars[0], toCopy);
  }

  RESUME_PROCESS_IF_NECESSARY

  return NaviErrors::SUCCESS;
}

void fillByte(unsigned char* data, unsigned int index, unsigned int offset,
              CPUADDRESS address, const std::vector<char>& newData) {
  if (offset + index >= address && offset + index < address + newData.size()) {
    data[index] = newData[offset - address + index];
  }
}

NaviError LinuxSystem::writeMemory(CPUADDRESS address,
                                   const std::vector<char>& newData) {
  CPUADDRESS normalizedAddress = address & -4;

  unsigned int bytesToWrite = address - normalizedAddress + newData.size();

  unsigned int dwordsToWrite = bytesToWrite / 4
      + (bytesToWrite % 4 == 0 ? 0 : 1);

  STOP_PROCESS_IF_NECESSARY

  for (unsigned int i = 0; i < dwordsToWrite; i++) {
    DATA data;

    unsigned int offset = normalizedAddress + 4 * i;

    data.value = ptrace(PTRACE_PEEKTEXT, getPID(), offset, NULL);

    fillByte(data.chars, 0, offset, address, newData);
    fillByte(data.chars, 1, offset, address, newData);
    fillByte(data.chars, 2, offset, address, newData);
    fillByte(data.chars, 3, offset, address, newData);

    ptrace(PTRACE_POKETEXT, getPID(), offset, data.value);
  }

  RESUME_PROCESS_IF_NECESSARY

  return NaviErrors::SUCCESS;
}

void LinuxSystem::processOtherEvents() {
  std::map < std::string, Module > newModules;

  fillModules(getPID(), newModules);

  // find all NEW modules, i.e. the ones which are not in this->modules
  BOOST_FOREACH(const ModulesMap::value_type& v, newModules)
  {
    if (this->modules.find(v.first) == this->modules.end())
    {
      // newModule was not seen before => notify BinNavi about the new module
      msglog->log(LOG_ALL, "Discovered newly loaded module: %s", v.second.name.c_str());
      NaviError result = moduleLoaded(v.second, getPID());

      if (result)
      {
        msglog->log(LOG_ALWAYS, "Error: Could not handle module loading");
      }
    }
  }

  // find all OLD modules, i.e. the ones which are only in this->modules
  BOOST_FOREACH(const ModulesMap::value_type& v, this->modules)
  {
    ModulesMap::const_iterator it = newModules.find(v.first);
    if (it == newModules.end())
    {
      msglog->log(LOG_ALL, "Discovered newly unloaded module: %s", v.second.name.c_str());
      NaviError result = moduleUnloaded(v.second);

      if (result)
      {
        msglog->log(LOG_ALWAYS, "Error: Could not handle module unloading");
      }
    }
  }

  this->modules = newModules;
}

bool LinuxSystem::handleCloneEvent(pid_t pid) const {
  msglog->log(LOG_ALWAYS, "PTRACE_EVENT_CLONE");

  unsigned int newThreadId = 0;
  if (ptrace(PTRACE_GETEVENTMSG, pid, 0, &newThreadId) != -1) {
    if (ptrace(
        PTRACE_SETOPTIONS,
        newThreadId,
        0,
        PTRACE_O_TRACECLONE | PTRACE_O_TRACEFORK | PTRACE_O_TRACEVFORK
            | PTRACE_O_TRACEVFORKDONE) != -1) {
      // TODO: handle tids vector!
      return ptrace(PTRACE_CONT, newThreadId, 0, 0) != -1;
    }
  }
  return false;
}

// TODO: Check return values
/**
 * Finds out whether debug events occurred in the target process.
 *
 * @return A NaviError code that describes whether the operation was successful or not.
 */
NaviError LinuxSystem::readDebugEvents() {
  int status;
  rusage ru;

  while (1) {
    // Wait for event in debuggee - wait4 does not block however but the process is halted when signals occur.
    // WNOHANG: return immediately if child already died.
    // WUNTRACED: also return if debuggee is already stopped.
    pid_t pid = wait4(-1, &status, WNOHANG | WUNTRACED | __WALL, &ru);

    /* No childs that we were waiting for */
    if (pid == 0) {
      processOtherEvents();

      return 1;
    }

    if (pid == -1) {
      return 1;
    }

    if (WIFSTOPPED(status)) {
      if (WSTOPSIG(status) == SIGTRAP) {
        msglog->log(LOG_VERBOSE, "Received SIGTRAP signal");

        switch ((status >> 16) & 0xFFFF) {
          case 0: {
            int sr = ptrace_stop(getPID());

            if (sr == -1) {
              msglog->log(LOG_ALWAYS, "Error: Couldn't stop target process");
            } else if (sr) {
              wait(0);
            }

            CPUADDRESS addr;
            NaviError eipResult = getInstructionPointer(0, addr);

            addr--;

            if (eipResult) {
              msglog->log(LOG_ALWAYS, "Error: Couldn't get breakpoint address");
              return 1;
            }

            char tmp[50];

            sprintf(tmp, ADDRESS_FORMAT_MASK, addr);

            NaviError hitResult = breakpointHit(tmp, getPID(),
                                                true /* resume on echo bp */);

            if (hitResult) {
              msglog->log(LOG_ALWAYS, "Error: Breakpoint handler failed");

              resumeProcess();
            }

            return NaviErrors::SUCCESS;
          }

          case PTRACE_EVENT_FORK:
            msglog->log(LOG_ALWAYS, "PTRACE_EVENT_FORK");
            break;

          case PTRACE_EVENT_CLONE:
            handleCloneEvent(pid);
            resumeThread(pid);
            break;

          case PTRACE_EVENT_VFORK_DONE:
            msglog->log(LOG_ALWAYS, "PTRACE_EVENT_VFORK_DONE");
            break;

          case PTRACE_EVENT_EXIT:
            msglog->log(LOG_ALWAYS, "PTRACE_EVENT_EXIT");
            break;

          case PTRACE_EVENT_EXEC:
            msglog->log(LOG_ALWAYS, "PTRACE_EVENT_EXEC");
            break;

          default:
            msglog->log(LOG_ALWAYS, "Received unhandled signal %d",
                        (status >> 16) & 0xFFFF);
            break;
        }
      } else if (WSTOPSIG(status) == SIGSTOP) {
        // Check if we received the SIGSTOP message due to a new thread being spawned
        msglog->log(LOG_VERBOSE, "Received STOP signal");

        unsigned int newThreadId = 0;
        if (ptrace(PTRACE_GETEVENTMSG, pid, 0, &newThreadId) != -1) {
          return
              ptrace(PTRACE_CONT, newThreadId, 0, SIGSTOP) != -1 ?
                  NaviErrors::SUCCESS : NaviErrors::COULDNT_OPEN_THREAD;
        }
        return NaviErrors::COULDNT_OPEN_THREAD;

      }
#ifdef PTRACE_GETSIGINFO
      else if (WSTOPSIG(status) == SIGSEGV) {
        msglog->log(LOG_VERBOSE, "Received SIGSEGV signal");

        siginfo_t info;
        // PTRACE_GETSIGINFO is used to copy more info about the signal to the info struct
        int result = ptrace((__ptrace_request) PTRACE_GETSIGINFO, pid, 0, &info);

        if (result == -1) {
          msglog->log(
              LOG_ALWAYS,
              "Error: Couldn't get detailed segfault information (%d: %s)",
              errno, strerror(errno));
        } else {
          CPUADDRESS address = (CPUADDRESS) info.si_addr;

          msglog->log(LOG_VERBOSE, "Segfault at address %X", address);

          exceptionRaised(0, address, SIGSEGV);
        }
      } else if (WSTOPSIG(status) == SIGCHLD) {
        msglog->log(LOG_VERBOSE, "Received SIGCHLD signal");

        resumeProcess();
      }
#endif
      else {
        msglog->log(LOG_ALWAYS, "Error: Received unknown signal %d",
                    WSTOPSIG(status));

        resumeProcess();
      }
    } else if (WIFEXITED(status)) {
      processExit();

      return NaviErrors::SUCCESS;
    } else {
      msglog->log(LOG_ALWAYS, "Error: Received unknown status %d", status);

      resumeProcess();
    }
  }

  return NaviErrors::SUCCESS;
}

/**
 * Returns a list of the names of the registers of the underlying platform.
 *
 * @return A list of register names.
 */
std::vector<RegisterDescription> LinuxSystem::getRegisterNames() const {


  std::vector < RegisterDescription > regNames;

  RegisterDescription eax("EAX", 4, true);
  RegisterDescription ebx("EBX", 4, true);
  RegisterDescription ecx("ECX", 4, true);
  RegisterDescription edx("EDX", 4, true);
  RegisterDescription esi("ESI", 4, true);
  RegisterDescription edi("EDI", 4, true);
  RegisterDescription ebp("EBP", 4, true);
  RegisterDescription esp("ESP", 4, true);
  RegisterDescription eip("EIP", 4, true);
  RegisterDescription eflags("EFLAGS", 4, false);
  RegisterDescription cf("CF", 0, true);
  RegisterDescription pf("PF", 0, true);
  RegisterDescription af("AF", 0, true);
  RegisterDescription zf("ZF", 0, true);
  RegisterDescription sf("SF", 0, true);
  RegisterDescription of("OF", 0, true);

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

  return regNames;
}

/**
 * Returns the maximum size of a memory address of the target machine.
 *
 * @return The maximum size of a memory address of the target machine.
 */
unsigned int LinuxSystem::getAddressSize() const {
#if ARCHSIZE==32
  return 32;
#else
#error Architecture sizes other than 32 bits are not yet supported
#endif
}

namespace {
const int NumberOfExceptions = 2;

const DebugException ExceptionsArray[NumberOfExceptions] = {

DebugException("Access violation (SIGSEGV)", SIGSEGV, HALT), DebugException(
    "Illegal instruction (SIGILL)", SIGILL, HALT) };

DebugExceptionContainer exceptions(ExceptionsArray,
                                   ExceptionsArray + NumberOfExceptions);
}

/**
 * Returns the list of known debug exceptions.
 */
DebugExceptionContainer LinuxSystem::getPlatformExceptions() const {
  return exceptions;
}

/**
 * Returns the full path to the executable of the given process id.
 */
NaviError LinuxSystem::getExecutablePath(pid_t pid, std::string& path) const {
  char buf[250];
  std::string filename = "/proc/" + zylib::zycon::toString(pid) + "/exe";
  ssize_t size = readlink(filename.c_str(), buf, sizeof(buf));
  if (size != -1) {
    if ((size_t) size >= sizeof(buf))
      size = sizeof(buf) - 1;
    buf[size] = 0;
    path = std::string(buf);
    return NaviErrors::SUCCESS;
  }
  return NaviErrors::COULDNT_GET_EXE_PATH;
}

/**
 * Returns the debugger options that are supported by the debug client.
 *
 * @return The debugger options that are supported by the debug client.
 */
DebuggerOptions LinuxSystem::getDebuggerOptions() const {
  DebuggerOptions options;

  options.canHalt = true;
  options.pageSize = 4096;
  options.canTraceCount = true;
  options.canBreakOnModuleLoad = options.canBreakOnModuleUnload = false;

  return options;
}

unsigned int readTextFile(const std::string& filename, std::string& output) {
  std::string str;
  std::ifstream in(filename.c_str());

  if (!in) {
    msglog->log(LOG_ALL, "Could not open file %s", filename.c_str());

    return 1;
  }

  std::getline(in, str);

  while (in) {
    output += str;
    std::getline(in, str);
  }

  return 0;
}

int getSubDirectories(const std::string& dir, std::vector<std::string>& files) {


  DIR *dp;
  struct dirent *dirp;

  if ((dp = opendir(dir.c_str())) == NULL) {
    return errno;
  }

  while ((dirp = readdir(dp)) != NULL) {
    struct stat buffer;
    stat(dirp->d_name, &buffer);

    if (S_ISDIR(buffer.st_mode)) {
      files.push_back(std::string(dirp->d_name));
    }
  }

  closedir(dp);

  msglog->log(LOG_ALL, "Leaving %s", __FUNCTION__);

  return 0;
}

ProcessDescription getProcessDescription(unsigned int pid) {
// 1 (init) S 0 1 1 0 -1 4194560 1997 638227 40 442 0 447 3218 5874 20 0 1 0 183 3158016 271 4294967295 3086073856 3086172252 3218287280 3218286416 3085947952 0 0 4096 671835171 3223109594 0 0 0 0 0 0 0 0 0



  std::string filename = "/proc/" + zylib::zycon::toString(pid) + "/stat";

  msglog->log(LOG_VERBOSE, "Loading process information file %s",
              filename.c_str());

  std::string output;
  readTextFile(filename, output);

  msglog->log(LOG_ALL, "Parsing process information string %s", output.c_str());

  std::string::size_type openingParenthesis = output.find("(");
  std::string::size_type closingParenthesis = output.find(")");

  if (openingParenthesis != std::string::npos
      && closingParenthesis != std::string::npos) {
    std::string processName = output.substr(
        openingParenthesis + 1, closingParenthesis - openingParenthesis - 1);

    return ProcessDescription(pid, processName);
  } else {
    return ProcessDescription(pid, "Could not determine name");
  }
}

NaviError LinuxSystem::readProcessList(ProcessListContainer& processList) {


  std::vector < std::string > subdirectories;

  getSubDirectories("/proc", subdirectories);

  for (std::vector<std::string>::iterator Iter = subdirectories.begin();
      Iter != subdirectories.end(); ++Iter) {
    if (!zylib::zycon::isPositiveNumber(*Iter)) {
      continue;
    }

    ProcessDescription process = getProcessDescription(
        zylib::zycon::parseString<unsigned int>(*Iter));
    processList.push_back(process);
  }

  return NaviErrors::SUCCESS;
}

NaviError LinuxSystem::getFileSystems(
    std::vector<boost::filesystem::path>& roots) const {
  roots.push_back("/");
  return NaviErrors::SUCCESS;
}

NaviError LinuxSystem::getSystemRoot(boost::filesystem::path& root) const {
  root = "/";
  return NaviErrors::SUCCESS;
}

/**
 * This is kind of a HACK since we raise an artificial breakpoint hit if we single-stepped on a stepping breakpoint (e.g. when we do a "step-over").
 * We need to do this since the ptrace API swallows the breakpoint exception if we resume the process after stepping on it.
 */
NaviError LinuxSystem::resumeAfterStepping(unsigned int threadId,
                                           CPUADDRESS address) {
  if (hasBreakpoint(address, BPX_stepping)) {
    removeBreakpoint(getBreakpoint(address, BPX_stepping), BPX_stepping);
    breakpointHit(address, threadId, true /* resume on echo bp */);
  } else {
    return resumeProcess();
  }
  return NaviErrors::SUCCESS;
}
