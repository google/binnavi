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

#ifndef BASECPU_HPP
#define BASECPU_HPP

#include <vector>
#include <string>

#include "../../BaseSystem.hpp"
#include "../../defs.hpp"
#include "../../DebuggerOptions.hpp"
#include "../Transport.hpp"
#include "../IEventCallback.hpp"

// Flips the bytes in a hex-string
std::string flipBytesInString(const std::string& str);

/**
 * Base class that can be used for all CPU description objects.
 */
class GdbCpu {
#ifdef UNITTESTING
public:
#else
 private:
#endif
  // Connection to the gdbserver.
  Transport* transport;

  // Flag that keeps track of the suspended/running state of the target
  // process.
  mutable bool suspended;

  // Used to cache register strings to minimize the number of messages
  mutable std::string cachedRegisterString;

  std::string createWriteMemoryCommand(CPUADDRESS address, unsigned int size,
                                       const char* buffer) const;

  // Checks whether a message is an expected OK reply
  bool isOkMessageReply(const std::string& message) const;

  // Checks whether a message is an expected data message reply
  bool isDataMessageReply(const std::string& msg) const;

  // Checks whether a message is an expected TID message reply
  bool isTidMessageReply(const std::string& msg) const;

  bool isActiveThreadReply(const std::string& msg) const;

  // Waits for an expected message while processing stop-reply messages
  NaviError waitForMessage(
      std::string& msg, IEventCallback* cb,
      bool (GdbCpu::*isExpectedMessage)(const std::string&) const, bool loop =
          true) const;

  // Starts the GDB server
  NaviError startServer() const;

  bool testCommand(const std::string& outputMessage, const std::string& msg,
                   bool (GdbCpu::*isExpectedMessage)(const std::string&) const,
                   bool printReply = false) const;

#ifdef UNITTESTING
public:
#else
 protected:
#endif

  // Returns the page size of the target CPU (unused)
  virtual unsigned int getPageSize() const {
    return 4096;
  }

  // Maximum number of bytes that can be read from the target process memory in
  // one go
  virtual unsigned int getMaximumReadSize() const {
    return 0x1000;
  }

  /**
   * Checks whether the target process is suspended or not.
   *
   * @return True, if the target process is suspended. False, otherwise.
   */
  bool isSuspended() const {
    return suspended;
  }

  /**
   * Sets the status of the target process.
   *
   * @param suspended If true, the target process is assumed to be suspended.
   *                  If false, the target process is assumed to be running.
   */
  void setSuspended(bool suspended) const {
    this->suspended = suspended;
  }

  /**
   * Returns the transport object that is used to communicate with the GDB
   * server.
   *
   * @return The transport object that is used to communicate with the GDB
   * server.
   */
  const Transport* getTransport() const {
    return transport;
  }

  // Sends a string to the GDB server
  NaviError send(const std::string& msg) const;

  // Determines whether a string is a GDB OK message
  virtual bool isOkMessage(const std::string& message) const;

  // Determines whether a string is a GDB Error message
  virtual bool isErrorMessage(const std::string& message) const;

  // Determines whether a string is a GDB Unsupported message
  virtual bool isUnsupportedMessage(const std::string& message) const;

  // Determines whether a string is a GDB data message
  virtual bool isDataMessage(const std::string& message) const;

  // Determines whether a string is a GDB prefix message
  virtual bool isPrefixMessage(char prefix, const std::string& message) const;

  // Waits until a GDB Ack message arrived
  virtual NaviError waitForAckMessage(char& ack, IEventCallback* cb) const;

  // Waits until a GDB OK message arrived
  virtual NaviError waitForOKMessage(std::string& message,
                                     IEventCallback* cb) const;

  // Waits until a GDB data message arrived
  virtual NaviError waitForDataMessage(std::string& msg,
                                       IEventCallback* cb) const;

  // Waits until a GDB Thread message arrived
  virtual NaviError waitForTidMessage(std::string& msg,
                                      IEventCallback* cb) const;

  virtual NaviError waitForActiveThreadMessage(std::string& msg,
                                               IEventCallback* cb) const;

  // Waits until a GDB Halt message arrived
  virtual NaviError waitForHaltMessage(std::string& msg) const;

  // Determines whether the target GDB server needs to be restarted sometimes
  virtual bool needsRestarting() const;

  // Returns the restart message of the target platform
  virtual std::string getRestartMessage() const;

  // Waits until a greet message is received
  NaviError waitForGreetMessage() const;

  // Returns the greet message of the target platform
  virtual std::string getGreetMessage() const;

  // Converts a register index between BinNavi and GDB server
  virtual unsigned int naviIndexToGdbIndex(unsigned int index) const;

  //
  virtual std::string createRegisterString(const std::string& regString,
                                           unsigned int index,
                                           CPUADDRESS value) const;

  // Parses a GDB register string and fills the registers parameter with the
  //information
  virtual NaviError parseRegistersString(
      std::vector<RegisterValue>& registers,
      const std::string& regString) const = 0;

  // Decodes a packed string that was received from the GDB server
  virtual bool RunlengthDecode(std::string& encoded) const;

  // Decodes a packed string that was received from the GDB server
  virtual std::string decodeMessage(const std::string& msg) const;

  // Reads the register string from the GDB server
  virtual NaviError readRegisterString(unsigned int tid, std::string& regString,
                                       IEventCallback* cb) const;

  // Sends a packet to the GDB server and waits until the packet is
  //ACKed/NACKed
  virtual NaviError sendAndWaitForAck(const std::string& packet,
                                      IEventCallback* cb) const;

  // Handles standard OK/ERROR/UNSUPPORTED messages
  NaviError handleStandardReply(const std::string& message,
                                const char* unsupportedMessage,
                                const char* errorMessage) const;

  NaviError switchThread(unsigned int tid, const std::string& commandIdentifier,
                         IEventCallback* cb) const;

  void invalidateCachedRegisterString() const;

 public:

  /**
   * Creates a new GDB Cpu object.
   *
   * @param transport The transport object that is used to talk to the GDB
   server.
   */
  GdbCpu(Transport* transport)
      : transport(transport),
        suspended(false) {
  }

  virtual ~GdbCpu() {
  }

  // Returns the descriptions of the target platform registers.
  virtual std::vector<RegisterDescription> getRegisterNames() const = 0;

  // Returns the register index of the instruction pointer.
  virtual unsigned int getInstructionPointerIndex() const = 0;

  // Returns the size of the addressable memory of the target memory.
  virtual unsigned int getAddressSize() const = 0;

  // Returns the debugger options supported on the target platform.
  virtual DebuggerOptions getDebuggerOptions() const = 0;

  // Sets a breakpoint in the target process.
  virtual NaviError setBreakpoint(CPUADDRESS address,
                                  IEventCallback* cb) const = 0;

  // Removes a breakpoint from the target process.
  virtual NaviError removeBreakpoint(CPUADDRESS address,
                                     IEventCallback* cb) = 0;

  // Stores the original data that is replaced by a breakpoint in the target
  //process.
  virtual NaviError storeOriginalData(CPUADDRESS address,
                                      IEventCallback* cb) = 0;

  /**
   * Connects to the gdbserver.
   *
   * @return A NaviError code that indicates whether the operation succeeded or
   not.
   */
  NaviError connect() {
    return transport->open();
  }

  /**
   * Determines whether data from gdbserver is available.
   *
   * @return True, if data is available. False, otherwise.
   */
  bool hasData() const {
    return transport->hasData();
  }

  // Converts a breakpoint exception address to a breakpoint instruction
  //address
  virtual CPUADDRESS correctBreakpointAddress(CPUADDRESS address) const;

  // Sends a GDB Ack message to the GDB server.
  virtual NaviError sendAck() const;

  // Reads a GDB message from the GDB server.
  virtual NaviError receiveMessage(std::string& message,
                                   bool hasDollar = false) const;

  // Determines whether a string is a GDB Breakpoint message
  virtual bool isBreakpointMessage(const std::string& msg) const;

  virtual bool hasRegularBreakpointMessage() const {
    return true;
  }

  // Determines whether a string is a GDB Exit Process message
  virtual bool isProcessExitMessage(const std::string& msg) const;

  // Determines whether a string is a GDB Terminate Message message
  virtual bool isProcessTerminateMessage(const std::string& msg) const;

  // Attaches to the target process.
  virtual NaviError attach(std::vector<Thread>& tids, IEventCallback* cb) const;

  // Detaches from the target process.
  virtual NaviError detach(IEventCallback* cb) const;

  // Terminates the target process.
  virtual NaviError terminate(IEventCallback* cb) const;

  // Performs a single step operation in a thread of the target process.
  virtual NaviError doSingleStep(unsigned int tid, CPUADDRESS& address,
                                 IEventCallback* cb) const;

  // Resumes a thread of the target process.
  virtual NaviError resumeThread(unsigned int tid, IEventCallback* cb) const;

  virtual NaviError resumeProcess(IEventCallback* cb) const;

  // Halts the target process.
  virtual NaviError halt(IEventCallback* cb) const;

  // Retrieves the instruction pointer in a thread of the target process.
  virtual NaviError getInstructionPointer(unsigned int tid, CPUADDRESS& address,
                                          IEventCallback* cb) const;

  // Sets the instruction pointer in a thread of the target process.
  virtual NaviError setInstructionPointer(unsigned int tid, CPUADDRESS address,
                                          IEventCallback* cb) const;

  // Reads the registers values of all threads in the target process.
  virtual NaviError readRegisters(RegisterContainer& registers,
                                  IEventCallback* cb) const;

  // Sets a register value of a thread in the target process.
  virtual NaviError setRegister(unsigned int tid, unsigned int index,
                                CPUADDRESS address, IEventCallback* cb) const;

  // Reads binary data from the target process memory.
  virtual NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to,
                                   IEventCallback* cb) const;

  // Writes binary data to the target process memory.
  virtual NaviError writeMemoryData(const char* buffer, CPUADDRESS from,
                                    unsigned int size,
                                    IEventCallback* cb) const;

  // Returns the memory map of the target process
  virtual NaviError getMemmap(std::vector<CPUADDRESS>& addresses,
                              IEventCallback* cb) const;

  // Returns a description of the threads of the target process.
  virtual NaviError getThreads(std::vector<Thread>& tids,
                               IEventCallback* cb) const;

  virtual NaviError getActiveThread(unsigned int& activeThread,
                                    IEventCallback* cb) const;

  void testRun() const;

  // Determines whether the target platform is little endian or big endian.
  virtual bool isLittleEndian() const;
};

#endif
