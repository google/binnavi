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

#include "GdbSystem.hpp"

#include <algorithm>
#include <iostream>
#include <functional>
#include <numeric>

#include <zycon/src/zycon.h>

#include "../errors.hpp"
#include "../logger.hpp"
#include "../DebuggerOptions.hpp"
#include "GdbFactory.hpp"

namespace {
const int SIGILL = 4;
const int SIGFPE = 8;
const int SIGSEGV = 11;
const int SIGTERM = 15;
const int SIGBREAK = 21;
const int SIGABRT = 22;

const DebugException ExceptionsArray[] = { DebugException("Illegal instruction",
                                                          SIGILL, HALT),
    DebugException("Floating point exception", SIGFPE, HALT), DebugException(
        "Segmentation violation", SIGSEGV, HALT), DebugException("Kill signal",
                                                                 SIGTERM, HALT),
    DebugException("Ctrl-break signal", SIGBREAK, HALT), DebugException(
        "Abort signal", SIGABRT, HALT) };

DebugExceptionContainer exceptions(
    ExceptionsArray,
    ExceptionsArray + sizeof(ExceptionsArray) / sizeof(DebugException));
}

/**
 * Attaches to the target process.
 *
 * @param tids The thread IDs of the threads that belong to the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::attachToProcess() {


  std::vector < Thread > tids;
  NaviError attachResult = cpu->attach(tids, this);

  if (attachResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't attach to GDB server (Code: %d)",
                attachResult);
  } else {
    unsigned int activeThread;
    cpu->getActiveThread(activeThread, this);
    setActiveThread(activeThread);
    // The module constructor parameters are an optimistic over-approximation
    // since
    // Gdb can not provide us with better info about the executable image.
    processStart(Module("", "", 0, 0xFFFFFFFF),
                 Thread(activeThread, SUSPENDED));
  }

  return attachResult;
}

/**
 * We do not support starting the process via Gdb.
 */
NaviError GdbSystem::startProcess(
    const NATIVE_STRING /*path*/,
    const std::vector<const NATIVE_STRING>& /*commands*/) {


  msglog->log(LOG_ALWAYS,
              "Internal Error (start process not supported - attach instead)");
  std::exit(0);

  return NaviErrors::SUCCESS;
}

/**
 * Detaches the gdbserver from the target process without shutting down the
 * target process.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::detach() {


  return cpu->detach(this);
}

/**
 * Terminates the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::terminateProcess() {


  return cpu->terminate(this);
}

/**
 * Stores the original data that is replaced by a given breakpoint.
 *
 * @param bp The breakpoint in question.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::storeOriginalData(const BREAKPOINT& bp) {
  return cpu->storeOriginalData(bp.addr, this);
}

/**
 * Sets a breakpoint in the target process.
 *
 * @param breakpoint The breakpoint to be set.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::setBreakpoint(const BREAKPOINT& breakpoint, bool moreToCome) {
  return cpu->setBreakpoint(breakpoint.addr, this);
}

/**
 * Removes a breakpoint in the target process.
 *
 * @param breakpoint Breakpoint description of the breakpoint to remove.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::removeBreakpoint(const BREAKPOINT& bp, bool) {
  return cpu->removeBreakpoint(bp.addr, this);
}

/**
 * Performs a single step operation in the specified thread of the target
 * process.
 *
 * @param tid The thread ID of the target thread.
 * @param provider Information provider where information about the single step
 * operation will be stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::doSingleStep(unsigned int& tid, CPUADDRESS& address) {
  return cpu->doSingleStep(tid, address, this);
}

/**
 * Resumes the thread with the given thread ID in the target process.
 *
 * @param tid Thread ID of the target thread.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::resumeThread(unsigned int tid) {
  return cpu->resumeThread(tid, this);
}

NaviError GdbSystem::resumeProcess() {
  return cpu->resumeProcess(this);
}

NaviError GdbSystem::suspendThread(unsigned int tid) {
  // Not supported by the GDB Agent
  return 1;
}

/**
 * Halts the target process.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::halt() {
  return cpu->halt(this);
}

/**
 * Returns the current value of the instruction pointer in a thread of the target
 * process.
 *
 * @param tid The thread ID of the target thread.
 * @param addr Object where the current value of the instruction pointer is
 * written to.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::getInstructionPointer(unsigned int tid,
                                           CPUADDRESS& value) {


  return cpu->getInstructionPointer(tid, value, this);
}

/**
 * Sets the instruction pointer in a thread of the target process.
 *
 * @param tid The thread ID of the target thread.
 * @param value The new value of the instruction pointer.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::setInstructionPointer(unsigned int tid, CPUADDRESS value) {


  return cpu->setInstructionPointer(tid, value, this);
}

/**
 * Reads the current values of all relevant registers in the given thread of the
 * target process.
 *
 * @param registers Output object that will be filled with register information.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::readRegisters(RegisterContainer& registers) {


  return cpu->readRegisters(registers, this);
}

/**
 * Sets the value of a given register in the given thread of the target process.
 *
 * @param tid Thread ID of the target thread.
 * @param index Index of the register.
 * @param value New value of the register.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::setRegister(unsigned int tid, unsigned int index,
                                 CPUADDRESS value) {


  return cpu->setRegister(tid, index, value, this);
}

/**
 * Reads a part of the memory of the target process.
 *
 * @param buffer Byte buffer where the retrieved memory will be stored.
 * @param address The address from where the memory is read.
 * @param size Number of bytes to read.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::readMemoryData(char* buffer, CPUADDRESS address,
                                    CPUADDRESS size) {


  return cpu->readMemoryData(buffer, address, size, this);
}

NaviError GdbSystem::writeMemory(CPUADDRESS address,
                                 const std::vector<char>& data) {
  return cpu->writeMemoryData(&data[0], address, data.size(), this);
}

NaviError GdbSystem::readProcessList(ProcessListContainer& processList) {
  // Not supported by the GDB Agent
  return NaviErrors::UNSUPPORTED;
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
NaviError GdbSystem::getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                    CPUADDRESS& to) {


  return NaviErrors::UNSUPPORTED;
}

/**
 * Returns a list of all memory regions that are available in the target process.
 *
 * @param addresses The memory map is written into this list.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::getMemmap(std::vector<CPUADDRESS>& addresses) {


  return NaviErrors::UNSUPPORTED;
}

/**
 * Listens on the connection to the gdbserver for messages from the gdbserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::readDebugEvents() {
  while (cpu->hasData()) {
    // If incoming data is available, read the whole next message.

    std::string msg;
    NaviError result = cpu->receiveMessage(msg);

    if (result == NaviErrors::GDB_CONSOLE_OUTPUT) {
      return NaviErrors::SUCCESS;
    } else if (result) {
      msglog->log(LOG_VERBOSE,
                  "Error: Couldn't read message from gdbserver (Code %d)",
                  result);
      return result;
    }

    processMessage(msg);
  }

  return NaviErrors::SUCCESS;
}

/**
 * Returns register descriptions of the target platform.
 *
 * @return The list of register descriptions.
 */
std::vector<RegisterDescription> GdbSystem::getRegisterNames() const {


  return cpu->getRegisterNames();
}

/**
 * Returns the address size of the target architecture.
 *
 * @return The address size of the target architecture.
 */
unsigned int GdbSystem::getAddressSize() const {


  return cpu->getAddressSize();
}

/**
 * Returns the debugger options that are supported by the debug client.
 *
 * @return The debugger options that are supported by the debug client.
 */
DebuggerOptions GdbSystem::getDebuggerOptions() const {
  return cpu->getDebuggerOptions();
}

/**
 * Creates a connection to the gdbserver.
 *
 * @param connection Connection string that determines the location of the
 * gdbserver and how to communicate with it.
 * @param cpustring Target CPU string.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbSystem::initTarget(const std::string& connection,
                                const std::string& cpustring) {


  // Try to create a CPU description object from the connection string and the
  // cpu
  // string that was specified by the user.
  NaviError factResult = ::getCpu(connection, cpustring, &cpu);

  if (factResult) {
    return factResult;
  }

  // After the CPU description object was created successfully, it is now time
  // to connect to the specified gdbserver.
  NaviError openResult = cpu->connect();

  if (openResult) {
    return openResult;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Handles breakpoint hits.
 *
 * @param address The corrected address of the breakpoint event.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::handleBreakpointHit(CPUADDRESS address) {


  char tmp[50];

#ifdef WIN32
  sprintf_s(tmp, ADDRESS_FORMAT_MASK, address);
#else
  sprintf(tmp, ADDRESS_FORMAT_MASK, address);
#endif

  msglog->log(LOG_VERBOSE, "Breakpoint exception at offset %s", tmp);

  std::vector < Thread > tids;
  NaviError threadResult = cpu->getThreads(tids, this);
  if (threadResult) {
    msglog->log(LOG_ALWAYS, "Error: Unable to determine threads (Code %d)",
                threadResult);
    return threadResult;
  }

  NaviError hitResult;
  // TODO: mega crap code!!!
  if (tids.size()) {
    hitResult = breakpointHit(tmp, tids[0].tid);
  } else {
    hitResult = breakpointHit(tmp, 0);
  }

  if (hitResult) {
    msglog->log(LOG_ALWAYS, "Error: Breakpoint handler failed (Code %d)",
                hitResult);
    return hitResult;
  }

  return hitResult;
}

unsigned int GdbSystem::threadFromBreakpointMessage(const std::string& msg) {
  if (getDebuggerOptions().canMultithread) {
    std::size_t index = msg.find("thread:");

    if (index == std::string::npos) {
      msglog->log(LOG_ALWAYS,
                  "Could not parse thread ID from breakpoint message");

      return 0;
    }

    std::size_t startIndex = index + 7;  // "thread:".length()

    unsigned int stringSize = 0;

    while (zylib::zycon::isHex(msg[startIndex + stringSize])) {
      stringSize++;
    }

    std::string hexString = msg.substr(startIndex, stringSize);

    return zylib::zycon::parseHexString<unsigned int>(hexString);
  } else {
    return 0;
  }
}

NaviError GdbSystem::processBreakpointMessage(const std::string& msg) {
  // If the received message is a breakpoint message, it
  // is necessary to find out where the breakpoint exception
  // occurred.

  // TODO: Not all require this
  cpu->sendAck();

  CPUADDRESS address = 0;
  NaviError getProgramCounterError = getInstructionPointerFromStopMessage(
      msg, cpu->getInstructionPointerIndex(), address);
  if (getProgramCounterError) {
    msglog->log(
        LOG_VERBOSE,
        "Error: Couldn't read the value of the instruction pointer (Code %d)",
        getProgramCounterError);
    return getProgramCounterError;
  }

  synchronizeThreadState();

  unsigned int threadId = threadFromBreakpointMessage(msg);
  NaviError eipResult = getInstructionPointer(threadId, address);

  address = cpu->correctBreakpointAddress(address);

  if (hasBreakpoint(address, BPX_simple) || hasBreakpoint(address, BPX_echo)
      || hasBreakpoint(address, BPX_stepping)) {
    handleBreakpointHit(address);
  } else if (cpu->getDebuggerOptions().canHalt) {
    // Some other reason caused the process to stop
    // If the target CPU can halt arbitrarily, let's hope
    // that the halt was caused by the user.
  } else {
    // None of our breakpoints and the target CPU can not
    // halt arbitrarily => Handle the issue farther up in the chain.
    handleBreakpointHit(address);
  }

  return NaviErrors::SUCCESS;
}

/**
 * Processes a stop-reply message.
 *
 * @param msg The message to process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbSystem::processMessage(const std::string& msg) {


  if (cpu->isBreakpointMessage(msg)) {
    bool isRegular = cpu->hasRegularBreakpointMessage();

    unsigned int signal =
        isRegular ?
            zylib::zycon::parseHexString<unsigned int>(msg.substr(2, 2)) : 0;

    msglog->log(LOG_VERBOSE, "Received stop message %s", msg.c_str());

    if (!isRegular || signal == 5) {
      return processBreakpointMessage(msg);
    } else if (signal == SIGSEGV) {
      // SEGFAULT

      msglog->log(LOG_ALWAYS, "Target process segfaulted");

      cpu->sendAck();

      CPUADDRESS address = 0;

      NaviError eipResult = getInstructionPointer(
          threadFromBreakpointMessage(msg), address);

      if (eipResult) {
        msglog->log(LOG_VERBOSE, "Error: Couldn't read the value of the "
                    "instruction pointer (Code %d)",
                    eipResult);
        return eipResult;
      }

      exceptionRaised(threadFromBreakpointMessage(msg), address, signal);

      return NaviErrors::SUCCESS;
    } else {
      msglog->log(LOG_ALWAYS, "Received unknown stop message %s", msg.c_str());

      cpu->sendAck();

      resume(threadFromBreakpointMessage(msg));

      return NaviErrors::SUCCESS;
    }
  } else if (cpu->isProcessExitMessage(msg)
      || cpu->isProcessTerminateMessage(msg)) {
    cpu->sendAck();

    // Tell the base system that the process exited
    processExit();
  } else {
    msglog->log(LOG_ALWAYS, "Error: Received unknown message %s", msg.c_str());
  }

  return NaviErrors::SUCCESS;
}

NaviError GdbSystem::getInstructionPointerFromStopMessage(
    const std::string& msg, unsigned int index, CPUADDRESS& address) const {
  // option 1 =  $T050b:0*"00;0d:48edc5be;0f:dcdb0* ;thread:6a5;#04
  // option 2 =  $T050b:00000000;0d:48edc5be;0f:2ca20000;thread:6a5;#9f

  unsigned char indexChar = index & 0xFF;

  const std::string hexIndexString = ";"
      + zylib::zycon::toHexString(indexChar, true) + ":";

  const std::size_t stringIndex = msg.find(hexIndexString);

  if (stringIndex == std::string::npos) {
    msglog->log(LOG_ALWAYS,
                "Could not get instruction pointer from stop message!");

    return 0;
  }

  const std::size_t startIndex = stringIndex + hexIndexString.size();

  unsigned int stringSize = 0;

  while (msg.size() > stringSize + stringIndex
      && zylib::zycon::isHex(msg[startIndex + stringSize])) {
    stringSize++;
  }

  std::string hexString = msg.substr(startIndex, stringSize);

  if (hexString.size() < sizeof(CPUADDRESS) * 2) {
    hexString = hexString
        + std::string(sizeof(CPUADDRESS) * 2 - hexString.size(), '0');
  }

  if (cpu->isLittleEndian()) {
    hexString = flipBytesInString(hexString);
  }

  address = zylib::zycon::parseHexString<unsigned int>(hexString);

  return NaviErrors::SUCCESS;
}

NaviError GdbSystem::getFileSystems(
    std::vector<boost::filesystem::path>& roots) const {
  // Not supported by the Gdb agent
  return NaviErrors::UNSUPPORTED;
}

NaviError GdbSystem::getSystemRoot(boost::filesystem::path& root) const {
  // Not supported by the Gdb agent
  return NaviErrors::UNSUPPORTED;
}

DebugExceptionContainer GdbSystem::getPlatformExceptions() const {
  return exceptions;
}

// Enumerate all threads and synchronize with our list of known threads and
// generate debug events accordingly.
void GdbSystem::synchronizeThreadState() {
  std::vector < Thread > determinedThreads;
  cpu->getThreads(determinedThreads, this);

  std::set < Thread > determinedSet;
  BOOST_FOREACH(const Thread & t, determinedThreads) {
    determinedSet.insert(t);
  }
  std::set < Thread > knownSet;
  BOOST_FOREACH(const Thread & t, knownThreads) {knownSet.insert(t);}

  std::set < Thread > newThreads;
  std::set_difference(determinedSet.begin(), determinedSet.end(),
                      knownSet.begin(), knownSet.end(),
                      std::inserter(newThreads, newThreads.end()));

  std::set < Thread > killedThreads;
  std::set_difference(knownSet.begin(), knownSet.end(), determinedSet.begin(),
                      determinedSet.end(),
                      std::inserter(killedThreads, killedThreads.end()));

  // Generate debug events accordingly
BOOST_FOREACH(const Thread & nt, newThreads) {
  threadCreated(nt.tid, nt.state);
}
BOOST_FOREACH(const Thread & kt, killedThreads) {threadExit(kt.tid);}
}
