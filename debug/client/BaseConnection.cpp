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

#include "BaseConnection.hpp"

#include <iostream>
#include <vector>
#include <cstdlib>

#include "InformationProvider.hpp"
#include "defs.hpp"
#include "logger.hpp"
#include "errors.hpp"

/**
 * Creates a packet header with consideration of the endianness of the
 * target machine.
 *
 * @param command The type of the packet the header belongs to.
 * @param id The packet ID of the packet the header belongs to.
 * @param argnr The number of arguments in the packet the header belongs to.
 *
 * @return The packet header formed by the given information.
 **/
RDBG_PROTO_HDR BaseConnection::createPacketHeader(const commandtype_t command,
                                                  unsigned int id,
                                                  unsigned int argnr) const {
  return RDBG_PROTO_HDR((commandtype_t)htonl(command), htonl(id), htonl(argnr));
}

/**
 * Creates an integer argument header with consideration of the endianness of
 * the target machine.
 *
 * @return An integer argument header
 **/
DBG_PROTO_ARG BaseConnection::createIntegerArgumentHeader() const {
  return DBG_PROTO_ARG(htonl(sizeof(unsigned int)),
                       (argtype_t)htonl(arg_value));
}

/**
 * Creates an address argument header with consideration of the endianness of
 * the target machine.
 *
 * @return An address argument header
 **/
DBG_PROTO_ARG BaseConnection::createAddressArgumentHeader() const {
  return DBG_PROTO_ARG(htonl(sizeof(DBG_PROTO_ARG_ADDRESS)),
                       (argtype_t)htonl(arg_address));
}

/**
 * Creates a string argument header with consideration of the endianness of
 * the target machine.
 *
 * @param size Size of the string.
 *
 * @return A string argument header
 **/
DBG_PROTO_ARG
BaseConnection::createStringArgumentHeader(unsigned int size) const {
  return DBG_PROTO_ARG(htonl(size), (argtype_t)htonl(arg_data_buf));
}

/**
 * Creates an address argument with consideration of the endianness of the
 * target machine.
 *
 * @return The address argument.
 **/
DBG_PROTO_ARG_ADDRESS
BaseConnection::createAddressArgument(CPUADDRESS address) const {
  DBG_PROTO_ARG_ADDRESS addr = catopa(address);
  addr.high32bits = htonl(addr.high32bits);
  addr.low32bits = htonl(addr.low32bits);
  return addr;
}

/**
 * Adds an integer argument header and an integer argument to a packet.
 *
 * @param buffer The packet to add to.
 * @param value The value to add to the packet.
 **/
void BaseConnection::addIntegerArgument(PacketBuffer &buffer,
                                        unsigned int value) const {
  buffer.add(createIntegerArgumentHeader());
  buffer.add(htonl(value));
}

/**
 * Adds an address argument header and an address argument to a packet.
 *
 * @param buffer The packet to add to.
 * @param address The address to add to the packet.
 **/
void BaseConnection::addAddressArgument(PacketBuffer &buffer,
                                        CPUADDRESS address) const {
  buffer.add(createAddressArgumentHeader());
  buffer.add(createAddressArgument(address));
}

/**
 * Sends a debug reply with a single integer argument and the given command
 * and packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value The integer value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerReply(const commandtype_t command,
                                           unsigned int id,
                                           unsigned int value) const {
  return sendIntegersReply(command, id, &value, 1);
}

/**
 * Sends a debug reply with two integer arguments and the given command and
 * packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value1 The first integer value to send.
 * @param value2 The second integer value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerIntegerReply(const commandtype_t command,
                                                  unsigned int id,
                                                  unsigned int value1,
                                                  unsigned int value2) const {
  unsigned int values[] = {value1, value2};
  return sendIntegersReply(command, id, (unsigned int *)&values,
                           sizeof(values) / sizeof(*values));
}

/**
 * Sends a debug reply with three integer arguments and the given command and
 * packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value1 The first integer value to send.
 * @param value2 The second integer value to send.
 * @param value3 The second integer value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerIntegerIntegerReply(
    const commandtype_t command, unsigned int id, unsigned int value1,
    unsigned int value2, unsigned int value3) const {
  unsigned int values[] = {value1, value2, value3};
  return sendIntegersReply(command, id, (unsigned int *)&values,
                           sizeof(values) / sizeof(*values));
}

/**
 * Sends a debug reply with a variable number of integer arguments and the
 * given command and packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param values The integer arguments to send to BinNavi.
 * @param nrvalues The number of integer arguments to send to BinNavi.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegersReply(const commandtype_t command,
                                            unsigned int id,
                                            unsigned int *values,
                                            unsigned int nrvalues) const {
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, nrvalues));
  for (unsigned int i = 0; i < nrvalues; i++) {
    addIntegerArgument(buffer, values[i]);
  }
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send integer reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply with a single address argument and the given command
 * and packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param address The address value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendAddressReply(const commandtype_t command,
                                           unsigned int id,
                                           CPUADDRESS address) const {
  return sendAddressesReply(command, id, &address, 1);
}

/**
 * Sends a debug reply with two address arguments and the given command and
 * packet ID
 * to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param address1 The first address value to send.
 * @param address2 The second address value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendDoubleAddressReply(const commandtype_t command,
                                                 unsigned int id,
                                                 CPUADDRESS address1,
                                                 CPUADDRESS address2) const {
  CPUADDRESS addresses[] = {address1, address2};
  return sendAddressesReply(command, id, (CPUADDRESS *)&addresses,
                            sizeof(addresses) / sizeof(*addresses));
}

/**
 * Sends a debug reply with a variable number of address arguments and the
 * given command and packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param addresses The address arguments to send to BinNavi.
 * @param nraddresses The number of address arguments to send to BinNavi.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendAddressesReply(const commandtype_t command,
                                             unsigned int id,
                                             const CPUADDRESS *addresses,
                                             unsigned int nraddresses) const {
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, nraddresses));
  for (unsigned int i = 0; i < nraddresses; ++i) {
    addAddressArgument(buffer, addresses[i]);
  }
  msglog->log(LOG_ALL, "%d", nraddresses);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send address reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply that indicates what breakpoints were correctly set and
 * which ones were not.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param result Vector that contains the error codes for the individual
 * breakpoints. 0 = breakpoint was set.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendBreakpointsReply(
    const commandtype_t command, unsigned int id,
    const std::vector<std::pair<CPUADDRESS, unsigned int>> &results) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 1 + 2 * results.size();
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, results.size());
  for (const auto &breakpoint : results) {
    addAddressArgument(buffer, breakpoint.first);
    addIntegerArgument(buffer, breakpoint.second);
  }
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't send integer address reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply with one integer argument and one address argument and
 * the given command and packet ID to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value The integer value to send.
 * @param address The address value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerAddressReply(const commandtype_t command,
                                                  unsigned int id,
                                                  unsigned int value,
                                                  CPUADDRESS address) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 2;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, value);
  addAddressArgument(buffer, address);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't send integer address reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply with one integer argument and two address arguments and
 * the given command and packet ID
 * to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value The integer value to send.
 * @param address1 The first address value to send.
 * @param address2 The second address value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerAddressAddressReply(
    const commandtype_t command, unsigned int id, unsigned int value,
    CPUADDRESS address1, CPUADDRESS address2) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 3;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, value);
  addAddressArgument(buffer, address1);
  addAddressArgument(buffer, address2);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't send integer address reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply with two integer arguments and one address argument and
 * the given command and packet ID
 * to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param value1 The first integer value to send.
 * @param value2 The second integer value to send.
 * @param address The address value to send.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendIntegerIntegerAddressReply(
    const commandtype_t command, unsigned int id, unsigned int value1,
    unsigned int value2, CPUADDRESS address) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 3;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, value1);
  addIntegerArgument(buffer, value2);
  addAddressArgument(buffer, address);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't send integer integer address reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply with one string argument and the given command and
 * packet ID to BinNavi.
 *
 * @param command The type of the command that identifies the information.
 * @param id The message ID of the reply.
 * @param string The string that is sent to BinNavi.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendString(const commandtype_t command,
                                     unsigned int id,
                                     const std::string &str) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 1;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addStringArgument(buffer, str);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send string message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a reply to a register values request to BinNavi.
 *
 * @param id The message ID of the reply.
 * @param regString The register data string.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendRegistersReply(commandtype_t type,
                                             unsigned int id,
                                             const std::string &str) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 1;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(type, id, NUMBER_OF_ARGUMENTS));
  addStringArgument(buffer, str);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send registers reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug reply to inform BinNavi about a breakpoint event.
 *
 * @param type The type of the debug reply.
 * @param id The ID of the debug reply.
 * @param str Register information at the time when the breakpoint was hit.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendEventReply(commandtype_t type, unsigned int id,
                                         unsigned int tid,
                                         const std::string &str) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 2;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(type, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, tid);
  addStringArgument(buffer, str);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send registers reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a reply to a register values request to BinNavi.
 *
 * @param id The message ID of the reply.
 * @param tid The thread ID of the thread the register values belong to.
 * @param regString The register data string.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendListProcessesReply(commandtype_t type,
                                                 unsigned int id,
                                                 const std::string &str) const {
  return sendString(type, id, str);
}

/**
 * Sends a debug reply that contains information about the files of a requested
 * directory.
 *
 * @param type Type of the debug reply.
 * @param id ID of the debug reply.
 * @param str Contains the requested information.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendListFilesReply(commandtype_t type,
                                             unsigned int id,
                                             const std::string &str) const {
  return sendString(type, id, str);
}

/**
 * Sends the initial authentication string to BinNavi that tells BinNavi
 * that it is dealing with a valid debug client.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError BaseConnection::sendAuthentication() const { return send("NAVI", 4); }

/**
 * Sends a reply to a memory data request to BinNavi.
 *
 * @param id The message ID of the reply.
 * @param address The address from the original memory request.
 * @param memrange The memory data.
 *
 * @return A NaviError code that describes whether the operation was
 * successful or not.
 **/
NaviError
BaseConnection::sendMemoryReply(unsigned int id, CPUADDRESS address,
                                const MemoryContainer &memrange) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 2;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(resp_read_memory, id, NUMBER_OF_ARGUMENTS));
  addAddressArgument(buffer, address);
  addStringArgument(buffer, memrange);
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send memory reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a reply to an event that suspended the process to BinNavi.
 *
 * @param command The type of the reply.
 * @param id The message ID of the reply.
 * @param info Object that provides information about the event.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError
BaseConnection::sendSuspendedReply(const commandtype_t command, unsigned int id,
                                   const InformationProvider &info) const {
  if (info.getRegisterString().size() == 0) {
    return NaviErrors::SUCCESS;
  }
  const unsigned int NUMBER_OF_ARGUMENTS = 3;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  addIntegerArgument(buffer, info.getTid());
  addAddressArgument(buffer, info.getAddress(0));
  addStringArgument(buffer, info.getRegisterString());
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send suspended reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Reads a header packet from BinNavi.
 *
 * @param hdr Structure where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readHeader(RDBG_PROTO_HDR &hdr) const {
  return read((char *)&hdr, sizeof(RDBG_PROTO_HDR));
}

/**
 * Reads an argument header packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readArgumentHeader(DBG_PROTO_ARG &arg) const {
  return read((char *)&arg, sizeof(DBG_PROTO_ARG));
}

/**
 * Reads a single integer value from BinNavi.
 *
 * @param arg The value from BinNavi is stored in this argument.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readIntegerArgument(unsigned int &arg) const {
  return read((char *)&arg, sizeof(unsigned int));
}

/**
 * Reads an address packet from BinNavi.
 *
 * @param arg Structure where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError
BaseConnection::readAddressArgument(DBG_PROTO_ARG_ADDRESS &arg) const {
  return read((char *)&arg, sizeof(DBG_PROTO_ARG_ADDRESS));
}

/**
 * Reads a simple packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readSimplePacket(Packet *p) const {
  return p->hdr.argument_num == 0 ? NaviErrors::SUCCESS
                                  : NaviErrors::MALFORMED_PACKET;
}

/**
 * Reads an integer packet from BinNavi
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readIntegerPacket(Packet *p) const {
  DBG_PROTO_ARG arg;
  NaviError argResult = readArgumentHeader(arg);
  if (argResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read argument header");
    return argResult;
  }
  arg.type = (argtype_t)ntohl(arg.type);
  arg.length = ntohl(arg.length);
  if (arg.type != arg_value) {
    msglog->log(
        LOG_ALWAYS,
        "Error: %s: received invalid argument, should be value, but type is %d",
        __FUNCTION__, arg.type);
    return NaviErrors::MALFORMED_PACKET;
  }
  if (arg.length != sizeof(unsigned int)) {
    msglog->log(LOG_ALWAYS,
                "Error: %s: received integer argument with invalid size %d",
                __FUNCTION__, arg.length);
    return NaviErrors::MALFORMED_PACKET;
  }
  unsigned int value = 0;
  NaviError readResult = readIntegerArgument(value);
  if (readResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read integer packet");
    return readResult;
  }
  value = ntohl(value);
  p->ints.push_back(value);
  return NaviErrors::SUCCESS;
}

/**
 * Reads an address packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readAddressPacket(Packet *p) const {
  if (p->hdr.argument_num != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed address packet passed to function");
    return NaviErrors::MALFORMED_PACKET;
  }
  return readAddressPacketRaw(p);
}

/**
 * Reads an address packet from BinNavi without checking whether
 * the header is wellformed.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readAddressPacketRaw(Packet *p) const {
  DBG_PROTO_ARG arg;
  NaviError argResult = readArgumentHeader(arg);
  if (argResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read argument header");
    return argResult;
  }
  arg.type = (argtype_t)ntohl(arg.type);
  arg.length = ntohl(arg.length);
  if (arg.type != arg_address) {
    msglog->log(LOG_ALWAYS, "Error: %s: received invalid argument, should be "
                            "address, but type is %d",
                __FUNCTION__, arg.type);
    return NaviErrors::MALFORMED_PACKET;
  }
  if (arg.length != sizeof(DBG_PROTO_ARG_ADDRESS)) {
    msglog->log(LOG_ALWAYS,
                "Error: %s: received address argument with invalid size %d",
                __FUNCTION__, arg.length);
    return NaviErrors::MALFORMED_PACKET;
  }
  DBG_PROTO_ARG_ADDRESS addr;
  NaviError result = readAddressArgument(addr);
  if (result) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read address argument");
    return result;
  }
  addr.high32bits = ntohl(addr.high32bits);
  addr.low32bits = ntohl(addr.low32bits);
  p->addresses.push_back(patoca(addr));
  return NaviErrors::SUCCESS;
}

/**
 * Reads an long packet from BinNavi without checking whether
 * the header is wellformed.
 *
 * Note: long packets are mapped onto DBG_PROTO_ARG_ADDRESS
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readLongPacketRaw(Packet *p) const {
  DBG_PROTO_ARG arg;
  NaviError argResult = readArgumentHeader(arg);
  if (argResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read argument header");
    return argResult;
  }
  arg.type = (argtype_t)ntohl(arg.type);
  arg.length = ntohl(arg.length);
  if (arg.type != arg_long) {
    msglog->log(
        LOG_ALWAYS,
        "Error: %s: received invalid argument, should be long, but type is %d",
        __FUNCTION__, arg.type);
    return NaviErrors::MALFORMED_PACKET;
  }
  if (arg.length != sizeof(DBG_PROTO_ARG_ADDRESS)) {
    msglog->log(LOG_ALWAYS,
                "Error: %s: received long argument with invalid size %d",
                __FUNCTION__, arg.length);
    return NaviErrors::MALFORMED_PACKET;
  }
  DBG_PROTO_ARG_ADDRESS addr;
  NaviError result = readAddressArgument(addr);
  if (result) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read long argument");
    return result;
  }
  addr.high32bits = ntohl(addr.high32bits);
  addr.low32bits = ntohl(addr.low32bits);
  p->addresses.push_back(patoca(addr));
  return NaviErrors::SUCCESS;
}

/**
 * Reads a data packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readDataPacket(Packet *p) const {
  DBG_PROTO_ARG arg;
  NaviError argResult = readArgumentHeader(arg);
  if (argResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read argument header");
    return argResult;
  }
  arg.type = (argtype_t)ntohl(arg.type);
  arg.length = ntohl(arg.length);
  if (arg.type != arg_data_buf) {
    msglog->log(LOG_ALWAYS, "Error: %s: received invalid argument, should be "
                            "data buffer, but type is %d",
                __FUNCTION__, arg.type);
    return NaviErrors::MALFORMED_PACKET;
  }
  p->data.resize(arg.length);
  return read((char *)&p->data[0], arg.length);
}

/**
 * Reads a packet from BinNavi that contains both an address and data.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readAddressDataPacket(Packet *p) const {
  NaviError addressResult = readAddressPacketRaw(p);
  if (addressResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read address part");
    return addressResult;
  }
  DBG_PROTO_ARG arg;
  NaviError argResult = readArgumentHeader(arg);
  if (argResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read argument header");
    return argResult;
  }
  arg.type = (argtype_t)ntohl(arg.type);
  arg.length = ntohl(arg.length);
  if (arg.type != arg_data_buf) {
    msglog->log(LOG_ALWAYS, "Error: %s: received invalid argument, should be "
                            "data buffer, but type is %d",
                __FUNCTION__, arg.type);
    return NaviErrors::MALFORMED_PACKET;
  }
  if (arg.length == 0) {
    return NaviErrors::SUCCESS;
  }
  p->data.resize(arg.length);
  return read((char *)&p->data[0], arg.length);
}

/**
 * Reads a memory packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readMemoryPacket(Packet *p) const {
  if (p->hdr.argument_num != 2) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed memory packet passed to function");
    return NaviErrors::MALFORMED_PACKET;
  }
  NaviError result1 = readAddressPacketRaw(p);
  if (result1) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read first memory range packet");
    return result1;
  }
  NaviError result2 = readAddressPacketRaw(p);
  if (result2) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read second memory range packet");
    return result2;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Reads a packet from BinNavi that contains a specified number of addresses.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readAddressesPacket(Packet *p) const {
  NaviError counter = readIntegerPacket(p);
  if (counter) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read counter packet");
    return counter;
  }
  for (unsigned int i = 0; i < p->ints[0]; ++i) {
    NaviError result2 = readAddressPacketRaw(p);
    if (result2) {
      msglog->log(LOG_VERBOSE, "Error: Couldn't read address packet");
      return result2;
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Reads a Set Register packet from BinNavi
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readSetRegisterPacket(Packet *p) const {
  if (p->hdr.argument_num != 3) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed register packet passed to function");
    return NaviErrors::MALFORMED_PACKET;
  }
  NaviError tidResult = readIntegerPacket(p);
  if (tidResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read TID packet");
    return tidResult;
  }
  msglog->log(LOG_VERBOSE, "Received TID packet %d", p->ints[0]);
  NaviError result1 = readIntegerPacket(p);
  if (result1) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read register index packet");
    return result1;
  }
  msglog->log(LOG_VERBOSE, "Received register index packet %d", p->ints[1]);
  NaviError result2 = readAddressPacketRaw(p);
  if (result2) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read register value packet");
    return result2;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Reads a Search memory packet from BinNavi
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readSearchPacket(Packet *p) const {
  if (p->hdr.argument_num != 3) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed search packet passed to function");
    return NaviErrors::MALFORMED_PACKET;
  }
  NaviError result1 = readAddressPacketRaw(p);
  if (result1) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read first search address packet");
    return result1;
  }
  NaviError result2 = readAddressPacketRaw(p);
  if (result2) {
    msglog->log(LOG_VERBOSE,
                "Error: Couldn't read second search address packet");
    return result2;
  }
  NaviError result3 = readDataPacket(p);
  if (result3) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read search data packet");
    return result3;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Reads a packet from BinNavi.
 *
 * @param p Packet object where received information is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readPacket(Packet *p) const {
  NaviError header = readHeader(p->hdr);
  if (header) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read packet header");
    return header;
  }
  p->hdr.command = (commandtype_t)ntohl(p->hdr.command);
  p->hdr.argument_num = ntohl(p->hdr.argument_num);
  p->hdr.id = ntohl(p->hdr.id);
  switch (p->hdr.command) {
    case cmd_clearall:
    case cmd_detach:
    case cmd_terminate:
    case cmd_memmap:
    case cmd_halt:
    case cmd_list_processes:
    case cmd_cancel_target_selection:
    case cmd_list_files:
    case cmd_registers:
    case cmd_resume:
    case cmd_single_step:
      return readSimplePacket(p);

    case cmd_select_process:
    case cmd_suspend_thread:
    case cmd_resume_thread:
    case cmd_set_active_thread:
      return readIntegerPacket(p);

    case cmd_setbp:
    case cmd_setbpe:
    case cmd_setbps:
    case cmd_rembp:
    case cmd_rembpe:
    case cmd_rembps:
      return readAddressesPacket(p);

    case cmd_validmem:
      return readAddressPacket(p);

    case cmd_read_memory:
      return readMemoryPacket(p);

    case cmd_set_register:
      return readSetRegisterPacket(p);

    case cmd_search:
      return readSearchPacket(p);

    case cmd_list_files_path:
    case cmd_select_file:
      return readDataPacket(p);

    case cmd_set_breakpoint_condition:
    case cmd_write_memory:
      return readAddressDataPacket(p);

    case cmd_set_exceptions_options:
      return readExceptionSettingsPacket(p);

    case cmd_set_debugger_event_settings:
      return readSetDebuggerEventSettingsPacket(p);

    default:
      msglog->log(LOG_ALWAYS, "Unknown command %d", p->hdr.command);
      return NaviErrors::UNKNOWN_COMMAND;
  }
}

/**
 * Sends a simple reply to BinNavi.
 *
 * @param command The type of the reply.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendSimpleReply(const commandtype_t command,
                                          unsigned int id) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 0;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(command, id, NUMBER_OF_ARGUMENTS));
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send simple reply message");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends the appropriate success reply for a packet that was received from
 * BinNavi.
 *
 * @param p The packet that was sent from BinNavi. A reply for that packet is
 * sent.
 * @param info An InformationProvider object that provides additional event
 * information.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError
BaseConnection::sendSuccessReply(const Packet *p,
                                 const InformationProvider &info) const {
  if (!p) {
    msglog->log(LOG_VERBOSE, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  commandtype_t command = p->hdr.command;
  unsigned int id = p->hdr.id;
  if (command == cmd_validmem) {
    if (info.getNumberOfAddresses() != 2) {
      msglog->log(LOG_VERBOSE,
                  "Error: Not enough information provided for cmd_validmem");
      return NaviErrors::INVALID_PACKET;
    }
  } else if (command == cmd_search) {
    if (info.getNumberOfAddresses() != 1) {
      msglog->log(LOG_VERBOSE,
                  "Error: Not enough information provided for cmd_search");
      return NaviErrors::INVALID_PACKET;
    }
  }

  switch (command) {
  case cmd_setbp:
    return sendBreakpointsReply(resp_bp_set_succ, id,
                                info.getBreakpointResults());
  case cmd_setbpe:
    return sendBreakpointsReply(resp_bpe_set_succ, id,
                                info.getBreakpointResults());
  case cmd_setbps:
    return sendBreakpointsReply(resp_bps_set_succ, id,
                                info.getBreakpointResults());
  case cmd_rembp:
    return sendBreakpointsReply(resp_bp_rem_succ, id,
                                info.getBreakpointResults());
  case cmd_rembpe:
    return sendBreakpointsReply(resp_bpe_rem_succ, id,
                                info.getBreakpointResults());
  case cmd_rembps:
    return sendBreakpointsReply(resp_bps_rem_succ, id,
                                info.getBreakpointResults());
  case cmd_resume:
    return sendSimpleReply(resp_resumed, id);
  case cmd_halt:
    return sendIntegerReply(resp_halted_succ, id,
                            0); // TODO: Do not always return TID 0 here.
  case cmd_detach:
    return sendSimpleReply(resp_detach_succ, id);
  case cmd_registers:
    return sendRegistersReply(resp_registers, id, info.getRegisterString());
  case cmd_read_memory:
    return sendMemoryReply(id, p->addresses[0], info.getMemoryData());
  case cmd_terminate:
    return sendSimpleReply(resp_terminate_succ, id);
  case cmd_set_register:
    return sendIntegerIntegerReply(resp_set_register_succ, id, p->ints[0],
                                   p->ints[1]);
  case cmd_single_step:
    return sendSuspendedReply(resp_single_step_succ, id, info);
  case cmd_validmem:
    return sendDoubleAddressReply(resp_validmem_succ, id, info.getAddress(0),
                                  info.getAddress(1));
  case cmd_search:
    return sendAddressReply(resp_search_succ, id, info.getAddress(0));
  case cmd_memmap:
    return sendAddressesReply(resp_memmap_succ, id, info.getAddresses(),
                              info.getNumberOfAddresses());
  case cmd_list_processes:
    return sendListProcessesReply(resp_list_processes, id,
                                  info.getRegisterString());
  case cmd_cancel_target_selection:
    return NaviErrors::SUCCESS;
  case cmd_select_process:
    return sendSimpleReply(resp_select_process_succ, id);
  case cmd_list_files:
    return sendListFilesReply(resp_list_files_succ, id,
                              info.getRegisterString());
  case cmd_list_files_path:
    return sendListFilesReply(resp_list_files_succ, id,
                              info.getRegisterString());
  case cmd_select_file:
    return sendSimpleReply(resp_select_file_succ, id);
  case cmd_set_breakpoint_condition:
    return sendSimpleReply(resp_set_breakpoint_condition_succ, id);
  case cmd_write_memory:
    return sendSimpleReply(resp_write_memory_succ, id);
  case cmd_suspend_thread:
    return sendIntegerReply(resp_suspend_thread_succ, id, p->ints[0]);
  case cmd_resume_thread:
    return sendIntegerReply(resp_resume_thread_succ, id, p->ints[0]);
  case cmd_set_active_thread:
    return sendIntegerReply(resp_set_active_thread_succ, id, p->ints[0]);
  case cmd_set_exceptions_options:
    return sendSimpleReply(resp_set_exceptions_succ, id);
  case cmd_set_debugger_event_settings:
    return sendSimpleReply(resp_set_debugger_event_settings_succ, id);
  default:
    msglog->log(LOG_ALWAYS,
                "Error: Invalid command received from BinNavi (Command %d)",
                command);
    return NaviErrors::UNKNOWN_COMMAND;
  }
}

/**
 * Sends the appropriate error reply for a packet that was sent by BinNavi.
 *
 * @param p The packet that was sent from BinNavi. A reply for that packet is
 * sent.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendErrorReply(const Packet *p,
                                         NaviError error) const {
  if (!p) {
    msglog->log(LOG_VERBOSE, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  commandtype_t command = p->hdr.command;
  unsigned int id = p->hdr.id;
  switch (command) {
  case cmd_setbp:
    return sendIntegerReply(resp_bp_set_err, id, error);
  case cmd_setbpe:
    return sendIntegerReply(resp_bpe_set_err, id, error);
  case cmd_setbps:
    return sendIntegerReply(resp_bps_set_err, id, error);
  case cmd_rembp:
    return sendIntegerReply(resp_bp_rem_err, id, error);
  case cmd_rembpe:
    return sendIntegerReply(resp_bpe_rem_err, id, error);
  case cmd_rembps:
    return sendIntegerReply(resp_bps_rem_err, id, error);
  case cmd_resume:
    return sendIntegerReply(resp_resume_err, id, error);
  case cmd_halt:
    return sendIntegerReply(resp_halted_err, id, error);
  case cmd_detach:
    return sendIntegerReply(resp_detach_err, id, error);
  case cmd_registers:
    return sendIntegerReply(resp_registers_err, id, error);
  case cmd_read_memory:
    return sendIntegerReply(resp_read_memory_err, id, error);
  case cmd_terminate:
    return sendIntegerReply(resp_terminate_err, id, error);
  case cmd_set_register:
    return sendIntegerReply(resp_set_register_err, id, error);
  case cmd_single_step:
    return sendIntegerReply(resp_single_step_err, id, error);
  case cmd_validmem:
    return sendIntegerReply(resp_validmem_err, id, error);
  case cmd_search:
    return sendIntegerReply(resp_search_err, id, error);
  case cmd_memmap:
    return sendIntegerReply(resp_memmap_err, id, error);
  case cmd_list_processes:
    return sendIntegerReply(resp_select_process_err, id, error);
  case cmd_list_files:
    return sendIntegerReply(resp_list_files_err, id, error);
  case cmd_list_files_path:
    return sendIntegerReply(resp_list_files_err, id, error);
  case cmd_suspend_thread:
    return sendIntegerIntegerReply(resp_resume_thread_err, id, error,
                                   p->ints[0]);
  case cmd_resume_thread:
    return sendIntegerIntegerReply(resp_resume_thread_err, id, error,
                                   p->ints[0]);
  case cmd_set_active_thread:
    return sendIntegerIntegerReply(resp_set_active_thread_err, id, error,
                                   p->ints[0]);
  case cmd_set_breakpoint_condition:
    return sendIntegerReply(resp_set_breakpoint_condition_err, id, error);
  case cmd_write_memory:
    return sendIntegerReply(resp_write_memory_err, id, error);
  case cmd_set_exceptions_options:
    return sendIntegerReply(resp_set_exceptions_err, id, error);
  case cmd_set_debugger_event_settings:
    return sendIntegerReply(resp_set_debugger_event_settings_err, id, error);
  default:
    msglog->log(LOG_ALWAYS,
                "Error: Invalid command received from BinNavi (Command %d)",
                command);
    return NaviErrors::UNKNOWN_COMMAND;
  }
}

/**
 * Sends a breakpoint debug event to BinNavi.
 *
 * @param dbg The debug event to be sent.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendBreakpointEvent(const DBGEVT *dbg) const {
  commandtype_t command;
  switch (dbg->type) {
  case dbgevt_bp_hit:
    command = resp_bp_hit;
    break;
  case dbgevt_bpe_hit:
    command = resp_bpe_hit;
    break;
  case dbgevt_bps_hit:
    command = resp_bps_hit;
    break;
  default:
    msglog->log(LOG_ALWAYS, "Error: Invalid breakpoint type %s:%d",
                __FUNCTION__, __LINE__);
    return NaviErrors::INVALID_BREAKPOINT_TYPE;
  }
  return sendEventReply(command, 0, dbg->tid, dbg->registerString);
}

/**
 * Sends a process closed debug event to BinNavi.
 *
 * @param dbg The debug event to be sent.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendProcessClosedEvent(const DBGEVT *) const {
  const unsigned int NUMBER_OF_ARGUMENTS = 0;
  PacketBuffer buffer;
  buffer.add(createPacketHeader(resp_process_closed, 0, NUMBER_OF_ARGUMENTS));
  NaviError sendResult = send(buffer.data(), buffer.size());
  if (sendResult) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't send debug event");
    return sendResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Sends a debug event to BinNavi.
 *
 * @param dbg The debug event to be sent.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendDebugEvent(const DBGEVT *dbg) const {
  if (!dbg) {
    msglog->log(LOG_VERBOSE, "Error: Invalid debug event passed to function");
    return NaviErrors::INVALID_DEBUG_EVENT;
  }
  if (dbg->type == dbgevt_bp_hit || dbg->type == dbgevt_bpe_hit ||
      dbg->type == dbgevt_bps_hit) {
    return sendBreakpointEvent(dbg);
  } else if (dbg->type == dbgevt_bpe_rem) {
    return sendAddressReply(resp_bpe_rem_succ, 0, dbg->bp.addr);
  } else if (dbg->type == dbgevt_process_closed) {
    return sendProcessClosedEvent(dbg);
  } else if (dbg->type == dbgevt_thread_created) {
    return sendIntegerIntegerReply(resp_thread_created, 0, dbg->tid,
                                   dbg->extra);
  } else if (dbg->type == dbgevt_thread_closed) {
    return sendIntegerReply(resp_thread_closed, 0, dbg->tid);
  } else if (dbg->type == dbgevt_exception) {
    return sendString(resp_exception_occured, 0, dbg->registerString);
  } else if (dbg->type == dbgevt_module_loaded) {
    return sendString(resp_module_loaded, 0, dbg->registerString);
  } else if (dbg->type == dbgevt_module_unloaded) {
    return sendString(resp_module_unloaded, 0, dbg->registerString);
  } else if (dbg->type == dbgevt_process_start) {
    return sendString(resp_process_start, 0, dbg->registerString);
  }
  msglog->log(LOG_ALWAYS, "Error: Unknown debug event");
  return NaviErrors::UNKNOWN_DEBUG_EVENT;
}

/**
 * Sends the given target information string to BinNavi.
 *
 * @param infoString The information string that is sent to BinNavi.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::sendInfoString(const std::string &infoString) const {
  return sendString(resp_info, 0, infoString);
}

/**
 * Reads the given exception settings packet and extracts the exception code
 * together with the desired handling action.
 *
 * @param p The packet to be read
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseConnection::readExceptionSettingsPacket(Packet *p) const {
  // the packet consists of a list of tuples, so the nr of arguments must be a
  // multiple of 2
  if (p->hdr.argument_num % 2) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed exception settings packet received");
    return NaviErrors::MALFORMED_PACKET;
  }
  for (unsigned int i = 0; i < p->hdr.argument_num / 2; ++i) {
    NaviError result = readLongPacketRaw(p);
    if (result) {
      msglog->log(LOG_VERBOSE, "Error: Couldn't read exception code packet");
      return result;
    }
    msglog->log(LOG_ALL, "Parsed exception code packet 0x%X", p->addresses[i]);
    NaviError result2 = readIntegerPacket(p);
    if (result2) {
      msglog->log(LOG_VERBOSE,
                  "Error: Couldn't read exception handling action packet");
      return result2;
    }
    msglog->log(LOG_ALL, "Received exception handling action %d", p->ints[i]);
  }
  return NaviErrors::SUCCESS;
}

/**
 * Read the Set Debugger Event Settings packet which specifies how the debugger
 * should handle certain debugger events.
 *
 * @param p The packet received from BinNavi.
 *
 * @return A NaviError code that describes whether the operation was successful.
 **/
NaviError BaseConnection::readSetDebuggerEventSettingsPacket(Packet *p) const {
  // sanity checks regarding the number of arguments are performed in
  // processSetDebuggerEventsSettingsPacket
  for (unsigned int i = 0; i < p->hdr.argument_num; ++i) {
    NaviError result = readIntegerPacket(p);
    if (result) {
      msglog->log(LOG_ALL,
                  "Error: Couldn't read Set Debugger Event Settings packet");
      return result;
    }
    msglog->log(
        LOG_VERBOSE,
        "Received %dth argument of Set Debugger Event Settings packet: %d", i,
        p->ints[i]);
  }
  return NaviErrors::SUCCESS;
}
