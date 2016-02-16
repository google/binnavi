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

#ifndef BASECONNECTION_HPP
#define BASECONNECTION_HPP

#include <string>

#include "defs.hpp"
#include "errors.hpp"
#include "commands.hpp"

#undef htonl
#undef ntohl

// Some forward declarations
class InformationProvider;

/**
 * Base class for connections between BinNavi and the debug client.
 *
 * All connection policies must inherit from this class.
 **/
class BaseConnection {
 private:

  // Creates a packet header
  RDBG_PROTO_HDR createPacketHeader(const commandtype_t command,
                                    unsigned int id,
                                    unsigned int argnr) const;

  // Creates an integer argument header that can be used in packets
  DBG_PROTO_ARG createIntegerArgumentHeader() const;

  // Creates an address argument header that can be used in packets
  DBG_PROTO_ARG createAddressArgumentHeader() const;

  // Creates a string argument header that can be used in packets
  DBG_PROTO_ARG createStringArgumentHeader(unsigned int size) const;

  // Creates an address argument that can be used in packets
  DBG_PROTO_ARG_ADDRESS createAddressArgument(CPUADDRESS address) const;

  // Adds an integer argument header and an integer argument to a packet
  void addIntegerArgument(PacketBuffer& buffer, unsigned int value) const;

  // Adds an address argument header and an address argument to a packet
  void addAddressArgument(PacketBuffer& buffer, CPUADDRESS address) const;

  /**
  * Adds a string argument header and a string argument to a packet.
  *
  * @param buffer The packet to add to.
  * @param str The string to add to the packet.
  **/
  template <typename T>
  void addStringArgument(PacketBuffer& buffer, const T& str) const {
    // This function needs to be templatized because T can be either
    // std::string or std::vector<char>

    // String argument header
    buffer.add(createStringArgumentHeader(str.size()));

    // String argument
    buffer.add(&str[0], str.size());
  }

  // Sends a packet with a single integer value to BinNavi
  NaviError sendIntegerReply(const commandtype_t command, unsigned int id,
                             unsigned int value) const;

  // Sends a packet with two integer values to BinNavi
  NaviError sendIntegerIntegerReply(const commandtype_t command,
                                    unsigned int id, unsigned int value1,
                                    unsigned int value2) const;

  // Sends a packet with three integer values to BinNavi
  NaviError sendIntegerIntegerIntegerReply(const commandtype_t command,
                                           unsigned int id,
                                           unsigned int value1,
                                           unsigned int value2,
                                           unsigned int value3) const;

  // Sends a packet with a variable number of integer values to BinNavi
  NaviError sendIntegersReply(const commandtype_t, unsigned int id,
                              unsigned int* values,
                              unsigned int nrvalues) const;

  // Sends a packet with one address to BinNavi
  NaviError sendAddressReply(const commandtype_t command, unsigned int id,
                             CPUADDRESS address) const;

  // Sends a packet with two addresses to BinNavi
  NaviError sendDoubleAddressReply(const commandtype_t command,
                                   unsigned int id,
                                   CPUADDRESS address1,
                                   CPUADDRESS address2) const;

  // Sends a packet with information about multiple addresses to BinNavi
  NaviError sendAddressesReply(const commandtype_t command, unsigned int id,
                               const CPUADDRESS* addresses,
                               unsigned int nraddresses) const;

  NaviError sendBreakpointsReply(
      const commandtype_t command,
      unsigned int id,
      const std::vector<std::pair<CPUADDRESS, unsigned int> >& results) const;

  // Sends a packet with an integer value and an address to BinNavi
  NaviError sendIntegerAddressReply(const commandtype_t command,
                                    unsigned int id, unsigned int value,
                                    CPUADDRESS address) const;

  // Sends a packet with an integer value and two addresses to BinNavi
  NaviError sendIntegerAddressAddressReply(const commandtype_t command,
                                           unsigned int id,
                                           unsigned int value,
                                           CPUADDRESS address1,
                                           CPUADDRESS address2) const;

  // Sends a packet with two integer values and an address to BinNavi
  NaviError sendIntegerIntegerAddressReply(const commandtype_t command,
                                           unsigned int id,
                                           unsigned int value1,
                                           unsigned int value2,
                                           CPUADDRESS value3) const;

  // Sends a packet with string information to BinNavi
  NaviError sendString(const commandtype_t type, unsigned int id,
                       const std::string& str) const;

  // Sends a packet with register values to BinNavi
  NaviError sendRegistersReply(commandtype_t type, unsigned int id,
                               const std::string& regString) const;

  // Sends a packet with information about a breakpoint event to BinNavi
  NaviError sendEventReply(commandtype_t type, unsigned int id,
                           unsigned int tid,
                           const std::string& regString) const;

  // Sends a packet with the process information of the running processes to
  //BinNavi.
  NaviError sendListProcessesReply(
      commandtype_t type, unsigned int id,
      const std::string& processListString) const;

  // Sends a packet with file system information to BinNavi
  NaviError sendListFilesReply(commandtype_t type, unsigned int id,
                               const std::string& str) const;

  // Sends a packet with memory data to BinNavi
  NaviError sendMemoryReply(unsigned int id, CPUADDRESS address,
                            const MemoryContainer& memrange) const;

  // Sends a packet that contains information about a suspended process to
  //BinNavi
  NaviError sendSuspendedReply(const commandtype_t command, unsigned int id,
                               const InformationProvider& provider) const;

  // Reads a packet header from BinNavi
  NaviError readHeader(RDBG_PROTO_HDR& hdr) const;

  // Reads a packet argument header from BinNavi
  NaviError readArgumentHeader(DBG_PROTO_ARG& arg) const;

  // Reads a single integer argument from BinNavi
  NaviError readIntegerArgument(unsigned int& arg) const;

  // Reads a single address argument from BinNavi
  NaviError readAddressArgument(DBG_PROTO_ARG_ADDRESS& arg) const;

  // Reads a packet without additional information from BinNavi
  NaviError readSimplePacket(Packet* p) const;

  // Reads a packet with a single integer argument from BinNavi
  NaviError readIntegerPacket(Packet* p) const;

  // Reads a packet with a single address argument from BinNavi
  NaviError readAddressPacket(Packet* p) const;

  // Reads a packet with a single address argument and without an address
  //header from BinNavi
  NaviError readAddressPacketRaw(Packet* p) const;

  // Reads a single data argument from BinNavi
  NaviError readDataPacket(Packet* p) const;

  // Reads a packet that contains an address and binary data
  NaviError readAddressDataPacket(Packet* p) const;

  // Reads a packet with memory range information from BinNavi
  NaviError readMemoryPacket(Packet* p) const;

  // Reads a packet with information about setting a register value from
  //BinNavi
  NaviError readSetRegisterPacket(Packet* p) const;

  // Reads a packet with memory search information from BinNavi
  NaviError readSearchPacket(Packet* p) const;

  // Reads a packet that contains a bunch of addresses
  NaviError readAddressesPacket(Packet* p) const;

  NaviError readLongPacketRaw(Packet* p) const;

  NaviError readExceptionSettingsPacket(Packet* p) const;

  NaviError readSetDebuggerEventSettingsPacket(Packet* p) const;

 protected:

  /**
  * Sends a specified number of bytes from a given buffer to BinNavi.
  *
  * @param buffer The source buffer.
  * @param size The size of the buffer.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError send(const char* buffer, unsigned int size) const = 0;

  /**
  * Reads a specified number of incoming bytes from BinNavi.
  *
  * Note that the buffer is guaranteed to be large enough to hold the
  * specified number of bytes.
  *
  * @param buffer The destination buffer.
  * @param size Number of bytes to read.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError read(char* buffer, unsigned int size) const = 0;

#ifndef NAVI_GDB_OSX
  /**
  * Converts values between host and network byte order.
  *
  * TODO: Investigate potential problems when passing unsigned ints.
  *
  * @param value The value in host byte order.
  *
  * @return The value in network byte order.
  **/
  virtual int htonl(int value) const = 0;

  /**
  * Converts values between network and host byte order.
  *
  * TODO: Investigate potential problems when passing unsigned ints.
  *
  * @param value The value in network byte order.
  *
  * @return The value in host byte order.
  **/
  virtual int ntohl(int value) const = 0;
#endif
 public:

  /**
  * Initializes the connection to BinNavi.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError initializeConnection() = 0;

  /**
  * Closes the connection to BinNavi.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError closeConnection() = 0;

  /**
  * Indicates whether data from BinNavi is incoming.
  *
  * @return True, if incoming data is ready to be read. False, otherwise.
  **/
  virtual bool hasData() const = 0;

  /**
  * Opens a connection and waits for BinNavi to connect.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError waitForConnection() = 0;

  virtual ~BaseConnection() {}

  // Reads an incoming debug command packet from BinNavi
  NaviError readPacket(Packet* p) const;

  // Sends a debug reply without any additional arguments to BinNavi
  NaviError sendSimpleReply(const commandtype_t command, unsigned int id) const;

  // Sends a debug reply that signals that an earlier received debug command
  // was successfully executed.
  NaviError sendSuccessReply(const Packet* p,
                             const InformationProvider& info) const;

  // Sends a debug reply that signals that an earlier received debug command
  // failed.
  NaviError sendErrorReply(const Packet* p, NaviError error) const;

  // Sends information about a breakpoint event to BinNavi.
  NaviError sendBreakpointEvent(const DBGEVT* dbg) const;

  // Sends information that the target process closed to BinNavi.
  NaviError sendProcessClosedEvent(const DBGEVT* dbg) const;

  // Sends a debug reply that is not the result of an earlier debug command.
  NaviError sendDebugEvent(const DBGEVT* event) const;

  // Sends the initial debugger information string to BinNavi
  NaviError sendInfoString(const std::string& infoString) const;

  // Sends the initial authentication string to BinNavi
  NaviError sendAuthentication() const;

  // Prints information about connection (e.g. IP)
  virtual void printConnectionInfo() = 0;
};

#endif
