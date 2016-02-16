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

#include "GdbCpu.hpp"

#include <algorithm>
#include <numeric>
#include <functional>
#include <cassert>

#include <zycon/src/zycon.h>

#include "../utils.hpp"
#include "../../logger.hpp"

#include "../SimpleCallback.hpp"

std::string flipBytesInString(const std::string& str) {
  // We can only flip if we get something to flip.
  assert(str.size() > 0);

  // We can only flip if the number of characters in the string is even.
  assert(str.size() % 2 == 0);

  std::string retval;

  for (unsigned int i = 0; i < str.size() / 2; i++) {
    retval += str.substr(str.size() - 2 * i - 2, 2);
  }

  return retval;
}

/**
 * Creates a GDB command that can be sent to the GDB server to write to the
 * process memory.
 *
 * @param address Start address of the memory write operation.
 * @param size Number of bytes to write to the process memory.
 * @param buffer Input buffer from where the new memory bytes are taken.
 *
 * @return The GDB memory write command that is built from the input values.
 */
std::string GdbCpu::createWriteMemoryCommand(CPUADDRESS address,
                                             unsigned int size,
                                             const char* buffer) const {
  std::string command = "X" + zylib::zycon::toHexString(address) + ","
      + zylib::zycon::toHexString(size) + ":";

  for (unsigned int i = 0; i < size; i++) {
    command += buffer[i];
  }

  return command;
}

/**
 * Determines whether a message is either OK, ERROR, or UNSUPPORTED.
 *
 * @param msg The message in question.
 *
 * @return True, if the message is either OK, ERROR, or UNSUPPORTED. False,
 * otherwise.
 */
bool GdbCpu::isOkMessageReply(const std::string& msg) const {
  // When expecting an OK message, it's also possible to receive
  // an Unsupported message or an Error message.
  return isOkMessage(msg) || isUnsupportedMessage(msg) || isErrorMessage(msg);
}

bool GdbCpu::isActiveThreadReply(const std::string& msg) const {
  return msg.find("$QC") == 0;
}

/**
 * Determines whether a message is either DATA, ERROR, or UNSUPPORTED.
 *
 * @param msg The message in question.
 *
 * @return True, if the message is either DATA, ERROR, or UNSUPPORTED. False,
 * otherwise.
 */
bool GdbCpu::isDataMessageReply(const std::string& msg) const {
  // When expecting a data message, it's also possible to receive
  // an Error message.
  return isErrorMessage(msg) || isUnsupportedMessage(msg) || isDataMessage(msg);
}

/**
 * Determines whether a message is either TID, LAST TID, ERROR, or UNSUPPORTED.
 *
 * @param msg The message in question.
 *
 * @return True, if the message is either TID, LAST TID, ERROR, or UNSUPPORTED.
 * False, otherwise.
 */
bool GdbCpu::isTidMessageReply(const std::string& msg) const {
  // When processing TID messages, it's also possible to receive
  // an Unsupported message or an Error message.
  return isPrefixMessage('l', msg) || isPrefixMessage('m', msg)
      || isUnsupportedMessage(msg) || isErrorMessage(msg);
}

/**
 * Waits for a certain message while processing incoming stop-reply messages.
 *
 * @param msg The message that arrived is stored here.
 * @param cb The callback object that handles stop-reply packets.
 * @param isExpectedMessage A function that decides whether a received message is
 * expected or not.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForMessage(
    std::string& msg, IEventCallback* cb,
    bool (GdbCpu::*isExpectedMessage)(const std::string&) const,
    bool loop) const {


  do {
    // Receive and decode the next incoming message
    msg = "";
    HANDLE_NAVI_ERROR(receiveMessage(msg),
                      "Error: Couldn't receive message (Code %d)");

    msg = decodeMessage(msg);

    if (isExpectedMessage == 0 || (this->*isExpectedMessage)(msg)) {
      // If the expected message arrived, we can return successfully.
      return NaviErrors::SUCCESS;
    } else {
      // The only unexpected messages that arrive can be
      // stop-reply messages. Let the callback handle them.
      cb->processMessage(msg);
    }
  } while (loop);

  return NaviErrors::SUCCESS;
}

/**
 * Sends a packet to the GDB server and waits for an ACK message to arrive.
 *
 * @param packet The packet to send to the GDB server.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::sendAndWaitForAck(const std::string& packet,
                                    IEventCallback* cb) const {
  char ack;

  do {
    msglog->log(LOG_VERBOSE, "Sending %s %d", packet.c_str(), packet.size());

    HANDLE_NAVI_ERROR(send(packet),
                      "Error: Couldn't send packet to GDB server (Code %d)");

    HANDLE_NAVI_ERROR(
        waitForAckMessage(ack, cb),
        "Error: Couldn't receive packet reply from GDB server (Code %d)");
  } while (ack == '-');

  return NaviErrors::SUCCESS;
}

/**
 * Starts the GDB server on the target platform.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::startServer() const {
  msglog->log(LOG_VERBOSE, "Restarting the GDB server");

  // Note: Do not use send(restartMessage) obviously
  std::string restartMessage = getRestartMessage();

  // Servers that need restarting must provide a restart message.
  assert(restartMessage.size() > 0);

  // Try to restart the server
  HANDLE_NAVI_ERROR(
      transport->send(restartMessage.c_str(), restartMessage.size()),
      "Couldn't send restart command to the GDB server (Code %d)");

  // After the GDB server was restarted; we have to wait until the greeting
  // arrived before we can continue.
  HANDLE_NAVI_ERROR(
      waitForGreetMessage(),
      "Couldn't receive Greet message from the GDB server (Code %d)");

  // How is it possible to receive a greet message from a suspended process?
  assert(isSuspended() == false);

  // If the greet message arrived, the target is suspended and the GDB server is
  // waiting for input
  setSuspended(true);

  return NaviErrors::SUCCESS;
}

/**
 * Switches the active thread for the given command.
 *
 * @param tid The thread ID of the thread to switch to.
 * @param commandIdentifier The identifier of the command for which to switch to
 * the thread.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::switchThread(unsigned int tid,
                               const std::string& commandIdentifier,
                               IEventCallback* cb) const {
  msglog->log(LOG_VERBOSE, "Switching active thread to %d", tid);

  std::string command = "H" + commandIdentifier
      + zylib::zycon::toHexString(tid);

  std::string packet = packetify(command);

  // Send the Set TID command to the GDB server and wait for the ACK/NACK
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck(packet, cb),
      "Error: Couldn't send Set ThreadID command to the GDB server (Code %d)");

  // Wait for the message that contains the result of the TID operation.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForOKMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Handle the possible reply messages: OK, UNSUPPORTED, ERROR
  handleStandardReply(message, "Error: Set ThreadID operation unsupported",
                      "Error: Couldn't set ThreadID (Code %s)");

  return NaviErrors::SUCCESS;
}

void GdbCpu::invalidateCachedRegisterString() const {
  cachedRegisterString = "";
}

/**
 * Standard code for reply messages that are expected to be either OK, ERROR, or
 * UNSUPPORTED.
 *
 * @param message The message to categorize.
 * @param unsupportedMessage Error message that's shown if message is an
 * UNSUPPORTED message.
 * @param unsupportedMessage Error message that's shown if message is an ERROR
 * message.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::handleStandardReply(const std::string& message,
                                      const char* unsupportedMessage,
                                      const char* errorMessage) const {
  // Many messages get the same replies. Either OK, ERROR, or UNSUPPORTED is
  // sent.
  // All of these messages can be handled equally.
  // If OK is sent, everything is fine. If ERROR or UNSUPPORTED are sent, an
  // error
  // message must be logged.

  if (isOkMessage(message)) {
    return NaviErrors::SUCCESS;
  } else if (isUnsupportedMessage(message)) {
    msglog->log(LOG_ALWAYS, unsupportedMessage);
    return NaviErrors::UNSUPPORTED;
  } else if (isErrorMessage(message)) {
    msglog->log(LOG_ALWAYS, errorMessage, unpacketify(message).c_str());
    return NaviErrors::GENERIC_ERROR;
  } else {
    msglog->log(LOG_ALWAYS, "Error: Unexpected GDB reply %s", message.c_str());
    return NaviErrors::UNEXPECTED_GDB_REPLY;
  }
}

/**
 * Determines whether the target CPU is a little endian CPU or not.
 */
bool GdbCpu::isLittleEndian() const {
  // Default is Little Endian
  return true;
}

/**
 * Helper function that sends a message to the GDB server. The GDB server is
 * automatically
 * restarted if necessary.
 *
 * @param msg The message to send to the server
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::send(const std::string& msg) const {


  // Empty messages can't be sent.
  assert(msg.size() > 0);

  // If the GDB server must be restarted, do so before sending the command
  if (needsRestarting()) {
    HANDLE_NAVI_ERROR(startServer(), "Couldn't start the GDB server (Code %d)");
  }

  msglog->log(LOG_VERBOSE, "Sending %s to the GDB server", msg.c_str());

  return transport->send(msg.c_str(), msg.size());
}

/**
 * Determines whether a message is an OK message.
 *
 * @param message The message in question.
 *
 * @return True, if the message is an OK message. False, otherwise.
 */
bool GdbCpu::isOkMessage(const std::string& message) const {
  return message == "$OK#9a";
}

/**
 * Determines whether a message is an Unsupported message ($#00).
 *
 * @param message The message in question.
 *
 * @return True, if the message is an Unsupported message. False, otherwise.
 */
bool GdbCpu::isUnsupportedMessage(const std::string& message) const {
  return message == "$#00";
}

/**
 * Determines whether a message is a Breakpoint message ($T...# or $S...#).
 *
 * @param message The message in question.
 *
 * @return True, if the message is a Breakpoint message. False, otherwise.
 */
bool GdbCpu::isBreakpointMessage(const std::string& msg) const {
  return msg.find("$T") == 0 || msg.find("$S") == 0;
}

/**
 * Determines whether a message is a Process Exit message ($WXX#).
 *
 * @param message The message in question.
 *
 * @return True, if the message is a Process Exit message. False, otherwise.
 */
bool GdbCpu::isProcessExitMessage(const std::string& msg) const {
  return msg.find("$W") == 0;
}

/**
 * Determines whether a message is a Process Terminate message ($XXX#).
 *
 * @param message The message in question.
 *
 * @return True, if the message is a Process Terminate message. False, otherwise.
 */
bool GdbCpu::isProcessTerminateMessage(const std::string& msg) const {
  return msg.find("$X") == 0;
}

/**
 * Determines whether a message only contains lower hex characters.
 *
 * @param msg The message in question.
 *
 * @return True, if all characters of the message are lower hex characters.
 * False, otherwise.
 */
bool GdbCpu::isDataMessage(const std::string& msg) const {
  return msg.size() > 4
      && std::find_if(msg.begin() + 1, msg.end() - 3,
                      std::not1(std::ptr_fun(zylib::zycon::isLowerHex)))
          == msg.end() - 3;
}

/**
 * Determines whether a message is an error message.
 *
 * @param message The message in question.
 *
 * @return True, if the message is an error message. False, otherwise.
 */
bool GdbCpu::isErrorMessage(const std::string& message) const {
  // TODO: Add checks for message[2] and message[3] (watch for 9999)

  return message.find("$E") == 0;
}

/**
 * Determines whether a message starts with the given prefix.
 *
 * @param prefix The necessary prefix.
 * @param message The message in question.
 *
 * @return True, if the message starts with the prefix. False, otherwise.
 */
bool GdbCpu::isPrefixMessage(char prefix, const std::string& message) const {
  return message.size() > 0 && message[1] == prefix;
}

/**
 * Waits for either a '+' or '-' message from gdbserver.
 *
 * @param ack The character where the received message is stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForAckMessage(char& ack, IEventCallback* cb) const {


  do {
    // Read the next byte from the GDB server
    HANDLE_NAVI_ERROR(transport->read(&ack, 1),
                      "Error: Couldn't read from GDB server (Code %d)");

    if (ack == '+' || ack == '-') {
      // The expected ACK/NACK message arrived.
      return NaviErrors::SUCCESS;
    } else if (ack == '$') {
      // We received a $ => A new stop-reply packet is coming in.

      // Read the entire stop-reply packet in one step
      std::string message = "$";
      NaviError readResult = receiveMessage(message, true);

      if (readResult == NaviErrors::SUCCESS) {
        cb->processMessage(decodeMessage(message));
      } else {
        // TODO: What to do here
        msglog->log(LOG_ALWAYS, "Error: Invalid data received and there is no "
                    "way to handle it (%c / %X)",
                    ack, ack);
      }
    } else {
      msglog->log(LOG_ALWAYS, "Error: Invalid data received (%c / %X)", ack,
                  ack);
    }
  } while (true);
}

/**
 * Waits for either an OK message, an Error message, or an Unsupported message
 * from gdbserver.
 *
 * @param msg The string where the received message is stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForOKMessage(std::string& msg, IEventCallback* cb) const {


  return waitForMessage(msg, cb, &GdbCpu::isOkMessageReply);
}

NaviError GdbCpu::waitForActiveThreadMessage(std::string& msg,
                                             IEventCallback* cb) const {


  return waitForMessage(msg, cb, &GdbCpu::isActiveThreadReply);
}

/**
 * Waits for either a data message or an Error message.
 *
 * @param msg The string where the received message is stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForDataMessage(std::string& msg,
                                     IEventCallback* cb) const {


  return waitForMessage(msg, cb, &GdbCpu::isDataMessageReply);
}

/**
 * Waits for either a TID message, an unsupported message, or an error message.
 *
 * @param msg The string where the received message is stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForTidMessage(std::string& msg,
                                    IEventCallback* cb) const {


  return waitForMessage(msg, cb, &GdbCpu::isTidMessageReply);
}

/**
 * Waits for a HALT.
 *
 * @param msg The string where the received message is stored.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForHaltMessage(std::string& msg) const {


  return waitForMessage(msg, 0, &GdbCpu::isBreakpointMessage);
}

/**
 * Determines whether the GDB server must be restarted before the next command is
 * sent.
 */
bool GdbCpu::needsRestarting() const {
  // Don't restart by default
  return false;
}

/**
 * Returns the message that is necessary to restart the GDB server
 */
std::string GdbCpu::getRestartMessage() const {
  // Empty default message because no restarting is necessary by default
  return "";
}

/**
 * Returns the greet message that identifies the GDB server.
 */
std::string GdbCpu::getGreetMessage() const {
  // Empty default greet message.
  return "";
}

/**
 * Waits for the GDB server greet message to arrive.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::waitForGreetMessage() const {


  // While waiting for a greet message we only have to distinguish
  // between the greet message and console output. Other data can
  // not arrive before the greet message.

  char c;

  std::string greetMessage = getGreetMessage();

  std::string receivedMessage = "";

  while (greetMessage != receivedMessage) {
    // Read the next byte from the GDB server
    HANDLE_NAVI_ERROR(transport->read(&c, 1),
                      "Error: Couldn't read from GDB server (Code %d)");

    if (c == greetMessage[receivedMessage.size()]) {
      // If the next character fits into the expected greet message,
      // add it to the received message.
      receivedMessage += c;
    } else {
      // A character arrived that doesn't fit into the greet message.
      // All earlier received parts of the potential greet message
      // must be discarded.
      receivedMessage = "";
    }
  }

  return NaviErrors::SUCCESS;
}

/**
 * Maps between registers as displayed in BinNavi and registers as positioned
 * in GDB register strings.
 *
 * @param index The index of a register in BinNavi
 *
 * @return The index of the same register in the GDB server.
 */
unsigned int GdbCpu::naviIndexToGdbIndex(unsigned int index) const {
  // On most platforms, the BinNavi index should equal the GDB index.
  return index;
}

/**
 * Takes a register String and updates the register with the given index to the
 * given value.
 */
std::string GdbCpu::createRegisterString(const std::string& regString,
                                         unsigned int index,
                                         CPUADDRESS value) const {
  // TODO: Doesn't work for registers that are bigger than getArchSize()

  if (isLittleEndian()) {
    return regString.substr(0, 8 * index)
        + flipBytesInString(zylib::zycon::toHexString(value, true))
        + regString.substr(8 * (index + 1));
  } else {
    return regString.substr(0, 8 * index)
        + zylib::zycon::toHexString(value, true)
        + regString.substr(8 * (index + 1));
  }
}

///
/// \brief Decodes potentially run-length encoded messages from the remote
/// agent.
///
/// The method implements run-length decoding of the form:
/// "x*8", where "x" is the character that is repeated and "8" is the number
/// of times the character appears. The method returns false for the cases of
/// - missing repeat character "*7"
/// - missing repeat count "a*"
/// - repeat cound of 0
/// The run-length decoding implemented here is the GDB standard version with
/// one repeat character, considered to be hexadecimal (not very well documented
/// for GDB itself, so this is more of a safe assumption).
///
bool GdbCpu::RunlengthDecode(std::string& encoded) const {
  std::string expanded;
  char to_repeat[2];
  char to_conv[2];
  unsigned int repeat;

  to_conv[1] = to_repeat[1] = 0;

  for (unsigned int i = 0; i < encoded.length(); i++) {
    if (encoded.at(i) == '*') {
      // "*7" is not allowed
      if (0 == i)
        return false;

      // "lalala*" not allowed
      if ((encoded.length() - 1) == i)
        return false;

      to_repeat[0] = encoded.at(i - 1);

      // convert
      repeat = encoded.at(i + 1) - 29;
      //			repeat = strtoul(to_conv, NULL, 16 );

      if (0 == repeat)
        return false;

      // minus one for the already copied?
      //
      // NOTE: Cisco doesn't do this and since we didn't find a GDB target doing
      // it so far, we assume the repeat count is excluding the already copied
      //
      // repeat--;

      for (unsigned int j = 0; j < repeat; j++)
        expanded.append(to_repeat);

      // skip over multiplier
      i++;
    } else {
      to_repeat[0] = encoded.at(i);
      expanded.append(to_repeat);
    }
  }

  encoded.clear();
  encoded.append(expanded);

  return true;
}

/**
 * Decodes a gdbserver message.
 *
 * @param msg The message to decode.
 *
 * @return The decoded message.
 */
std::string GdbCpu::decodeMessage(const std::string& msg) const {


  std::string tString = msg;

  RunlengthDecode(tString);

  return tString;
}

/**
 * Reads the register string that contains the current register values from the
 * GDB server.
 *
 * @param tid The thread ID of the thread whose registers to read.
 * @param regString The register string is stored here.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::readRegisterString(unsigned int tid, std::string& regString,
                                     IEventCallback* cb) const {


  if (getDebuggerOptions().canMultithread) {
    HANDLE_NAVI_ERROR(switchThread(tid, "g", cb),
                      "Error: Couldn't switch thread (Code %d)");
  }

  if (cachedRegisterString != "") {
    regString = cachedRegisterString;

    return NaviErrors::SUCCESS;
  }

  // Send the Read Registers command to the GDB server and wait for the ACK/NACK
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck("$g#67", cb),
      "Error: Couldn't send Read Register command to the GDB server (Code% d)");

  // Wait for the message that contains the result of the memory write
  // operation.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForDataMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Handle the possible reply messages: DATA, UNSUPPORTED, ERROR
  if (isUnsupportedMessage(message)) {
    msglog->log(LOG_ALWAYS, "Error: Register read operations are unsupported");
    return NaviErrors::UNSUPPORTED;
  } else if (isErrorMessage(message)) {
    msglog->log(
        LOG_ALWAYS,
        "Error: GDB server couldn't read the specified memory (Code: %s)",
        unpacketify(message).c_str());
    return NaviErrors::COULDNT_READ_MEMORY;
  } else if (isDataMessage(message)) {
    regString = unpacketify(message);

    return NaviErrors::SUCCESS;
  } else {
    msglog->log(LOG_ALWAYS, "Error: Unexpected GDB reply %s", message.c_str());
    return NaviErrors::UNEXPECTED_GDB_REPLY;
  }
}

/**
 * Sends an acknowledgment message to the GDB server.
 */
NaviError GdbCpu::sendAck() const {
  return send("+");
}

/**
 * Corrects a breakpoint address.
 *
 * On some platforms, breakpoint exceptions have the address of the breakpoint
 * instruction. On others, breakpoint exceptions have the address of the
 * instruction
 * after the breakpoint instruction. This function standardizes this behavior by
 * returning the address of the breakpoint instruction for a given breakpoint
 * exception address.
 *
 * @param The breakpoint exception address.
 *
 * @return The breakpoint instruction address.
 */
CPUADDRESS GdbCpu::correctBreakpointAddress(CPUADDRESS address) const {
  // By default we assume that breakpoint address equals
  // exception address.

  return address;
}

/**
 * Receives a single message from the GDB server.
 *
 * @param message The received message is stored here.
 * @param hasDollar True, if a $ was already received before entering the
 * function. False, if a $
 *        is expected to arrive inside the function.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not. Pay special
 *         attention to the return value GDB_CONSOLE_OUTPUT. This return value is
 * used whenever
 *         data was received that does not belong to a GDB command.
 */
NaviError GdbCpu::receiveMessage(std::string& message, bool hasDollar) const {


  // Receiving messages can be complicated because the user can exit the GDB
  // mode. When GDB mode
  // is inactive, console output arrives over the COM port. This console output
  // must be
  // distinguished from the GDB commands. When GDB mode is entered again, the
  // GDB server
  // writes the greet message to the COM port.

  char nextByte = 0;

  const Transport* transport = getTransport();

  const std::string greet = getGreetMessage();

  std::string msg = "";

  // In some cases the $ character was already received outside. If not, it has
  // to be received first. If we don't have a $, we can't possibly be in a GDB
  // message.
  if (hasDollar == false) {
    while (nextByte != '$') {
      // Read the next byte from the GDB server
      HANDLE_NAVI_ERROR(transport->read(&nextByte, 1),
                        "Error: Couldn't read from GDB server (Code %d)");

      // At this point we try to filter console output from GDB commands.
      // TODO: The next line could cause read-access to random memory; do empty
      // std::strings[0] return the terminating 0?
      if (greet != "" && nextByte == greet[msg.size()]) {
        // Try to form a greet message from the incoming bytes
        msg += nextByte;

        if (msg == greet) {
          //We have received a complete greet string. This means the process is
          //suspended
          // and GDB is waiting for new input.

          // How is it possible to receive a greet message from a suspended
          // process?
          assert(isSuspended() == false);

          setSuspended(true);

          message = msg;

          return NaviErrors::SUCCESS;
        }
      } else if (nextByte != '$') {
        // If no greet message can be formed and no GDB command identifier was
        // found,
        // the incoming data must be from console output. Partially received
        // greet
        // messages must be thrown away.
        message = nextByte;

        return NaviErrors::GDB_CONSOLE_OUTPUT;
      }
    }

    message += "$";
  }

  // If we come here, we can assume that we're reading a GDB command.

  do {
    // Read all bytes of the message
    HANDLE_NAVI_ERROR(transport->read(&nextByte, 1),
                      "Error: Couldn't read from GDB server (Code %d)");

    message += nextByte;

    if (nextByte == '#') {
      // If a end-of-packet characters arrived, read the checksum and
      // return successfully.

      char checksum[3] = { 0 };

      // Read the packet checksum
      HANDLE_NAVI_ERROR(transport->read(checksum, 2),
                        "Error: Couldn't read from GDB server (Code %d)");

      message += checksum;

      return NaviErrors::SUCCESS;
    }

  } while (true);
}

/**
 * Attaches to the GDB server
 *
 * @tids The output vector where the TIDs are stored.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::attach(std::vector<Thread>& tids, IEventCallback* cb) const {


  if (needsRestarting()) {
    HANDLE_NAVI_ERROR(startServer(),
                      "Error: Couldn't start the GDB server (Code %d)");
  }

  setSuspended(true);

  cachedRegisterString = "";

  return getThreads(tids, cb);
}

/**
 * Detaches the gdbserver from the debug client.
 *
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::detach(IEventCallback* cb) const {


  cachedRegisterString = "";

  // Send the Detach command to the GDB server and wait for the ACK/NACK
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck("$D#44", cb),
      "Error: Couldn't send Detach command to the GDB server (Code %d)");

  // Wait for the message that contains the result of the resume operation.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForOKMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Handle the possible reply messages: OK, UNSUPPORTED, ERROR
  return handleStandardReply(
      message, "Error: Detach operation unsupported",
      "Error: Couldn't Detach from the target process (Code %s)");
}

/**
 * Terminates the target process.
 *
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError GdbCpu::terminate(IEventCallback* cb) const {
  return NaviErrors::UNSUPPORTED;
}

/**
 * Performs a single step operation in the specified thread of the target
 * process.
 *
 * @param tid The thread ID of the target thread.
 * @param provider Information provider where information about the single step
 * operation will be stored.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::doSingleStep(unsigned int tid, CPUADDRESS& address,
                               IEventCallback* cb) const {


  if (getDebuggerOptions().canMultithread) {
    HANDLE_NAVI_ERROR(switchThread(tid, "c", cb),
                      "Error: Couldn't switch thread (Code %d)");
  }

  // Send the single step command and wait for the ACK/NACK message.
  HANDLE_NAVI_ERROR(sendAndWaitForAck("$s#73", cb),
                    "Error: Couldn't send single step packet reply to the GDB "
                    "server (Code %d)");

  cachedRegisterString = "";

  // TODO: This does not work properly
  if (getGreetMessage() == "") {
    do {
      std::string msg;
      HANDLE_NAVI_ERROR(
          receiveMessage(msg),
          "Error: Couldn't read message from the GDB server (Code %d)");

      msg = decodeMessage(msg);

      if (isBreakpointMessage(msg)) {
        HANDLE_NAVI_ERROR(sendAck(),
                          "Error: Couldn't acknowledge message (Code %d)");

        setSuspended(true);

        HANDLE_NAVI_ERROR(getInstructionPointer(tid, address, cb),
                          "Error: Couldn't read instruction pointer (Code %d)");

        return NaviErrors::SUCCESS;
      } else {
        cb->processMessage(msg);
      }
    } while (true);
  } else {
    HANDLE_NAVI_ERROR(waitForGreetMessage(),
                      "Error: Couldn't receive greet message (Code %d)")

    HANDLE_NAVI_ERROR(getInstructionPointer(tid, address, cb),
                      "Error: Couldn't read instruction pointer (Code %d)");

    return NaviErrors::SUCCESS;
  }
}

/**
 * Resumes the thread of the target process with the given TID.
 *
 * @param tid The TID of the thread to resume.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::resumeThread(unsigned int tid, IEventCallback* cb) const {


  if (getDebuggerOptions().canMultithread) {
    HANDLE_NAVI_ERROR(switchThread(tid, "c", cb),
                      "Error: Couldn't switch thread (Code %d)");
  }

  // Send the resume command and wait until it's acknowledged
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck("$vCont;c#a8", cb),
      "Error: Couldn't send resume command to the GDB server (Code %d)");

  cachedRegisterString = "";

  // The process is now running again.
  setSuspended(false);

  return NaviErrors::SUCCESS;
}

NaviError GdbCpu::resumeProcess(IEventCallback* cb) const {


  // Send the resume command and wait until it's acknowledged
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck("$vCont;c#a8", cb),
      "Error: Couldn't send resume command to the GDB server (Code %d)");

  cachedRegisterString = "";

  // The process is now running again.
  setSuspended(false);

  return NaviErrors::SUCCESS;
}

/**
 * Halts the target process.
 *
 * @param tid The TID of the thread to resume.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::halt(IEventCallback* cb) const {


  HANDLE_NAVI_ERROR(send("\x3"),
                    "Error: Couldn't send packet to GDB server (Code %d)");

  // ATTENTION: Interrupts actually create an OOB message. Nevertheless we're
  // going to
  // wait for the message here and hope that nothing happens between the
  // interrupt and
  // the expected OOB message.

  // Wait for the message that contains the result of the halt
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForHaltMessage(message),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  return NaviErrors::SUCCESS;
}

/**
 * Retrieves the current value of the instruction pointer in a thread of the
 * target process.
 *
 * @param tid The thread ID of the target process.
 * @param address Output value where the value of the instruction pointer is
 * stored.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::getInstructionPointer(unsigned int tid, CPUADDRESS& address,
                                        IEventCallback* cb) const {


  RegisterContainer registers;

  HANDLE_NAVI_ERROR(readRegisters(registers, cb),
                    "Error: Couldn't read registers (Code: %d)")

  std::vector < Thread > threads = registers.getThreads();

  for (std::vector<Thread>::iterator Iter = threads.begin();
      Iter != threads.end(); ++Iter) {
    Thread thread = *Iter;

    if (thread.tid == tid) {
      address = zylib::zycon::parseHexString < CPUADDRESS
          > (thread.registers[getInstructionPointerIndex()].getValue());

      return NaviErrors::SUCCESS;
    }
  }

  return NaviErrors::COULDNT_DETERMINE_INSTRUCTION_POINTER;
}

/**
 * Sets the instruction pointer in a thread of the target process.
 *
 * @param tid The thread ID of the target thread.
 * @param value The new value of the instruction pointer.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::setInstructionPointer(unsigned int tid, CPUADDRESS address,
                                        IEventCallback* cb) const {


  cachedRegisterString = "";

  if (correctBreakpointAddress(0) == 0) {
    return NaviErrors::SUCCESS;
  } else {
    return setRegister(tid, getInstructionPointerIndex(), address, cb);
  }
}

/**
 * Reads the current register values of a thread in the target process.
 *
 * @param tid Thread ID of the target thread.
 * @param registers Register container that will be filled with the register
 * information.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::readRegisters(RegisterContainer& registers,
                                IEventCallback* cb) const {


  std::vector < Thread > threads;

  HANDLE_NAVI_ERROR(getThreads(threads, cb),
                    "Error: Couldn't determine threads (Code %d)");

  for (std::vector<Thread>::iterator Iter = threads.begin();
      Iter != threads.end(); ++Iter) {
    Thread thread = *Iter;

    if (getDebuggerOptions().canMultithread) {
      HANDLE_NAVI_ERROR(switchThread(thread.tid, "g", cb),
                        "Error: Couldn't switch thread (Code %d)");
    }

    std::string regString;

    HANDLE_NAVI_ERROR(readRegisterString(thread.tid, regString, cb),
                      "Error: Couldn't read register string (Code %d)");

    cachedRegisterString = regString;

    msglog->log(LOG_VERBOSE, "Parsing registers string %s", regString.c_str());

    HANDLE_NAVI_ERROR(parseRegistersString(thread.registers, regString),
                      "Error: Couldnt't parse registers string (Code %d)");

    registers.addThread(thread);
  }

  return NaviErrors::SUCCESS;
}

/**
 * Sets the value of a given register in the given thread of the target process.
 *
 * @param tid Thread ID of the target thread.
 * @param index Index of the register.
 * @param value New value of the register.
 * @param cb The callback object that handles incoming stop-reply messages.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::setRegister(unsigned int tid, unsigned int index,
                              CPUADDRESS value, IEventCallback* cb) const {


  if (getDebuggerOptions().canMultithread) {
    HANDLE_NAVI_ERROR(switchThread(tid, "g", cb),
                      "Error: Couldn't switch thread (Code %d)");
  }

  cachedRegisterString = "";

  // To set the value of a register, it is necessary to read the values of all
  // registers, replace
  // the old register value with the new value in the register string and to
  // send the register string
  // back to the GDB server.

  // Read the old register values
  std::string regString;
  HANDLE_NAVI_ERROR(readRegisterString(tid, regString, cb),
                    "Error: Couldn't read registers (Code: %d)")

  unsigned int realIndex = naviIndexToGdbIndex(index);

  // Create the Set Register GDB command and packetify it
  // TODO: This will stop working for non 32-bit registers
  std::string command = "G" + createRegisterString(regString, realIndex, value);
  std::string packet = packetify(command);

  // Send the Set Register command to the GDB server and wait for the ACK/NACK
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck(packet, cb),
      "Error: Couldn't send Set Register command to the GDB server (Code %d)");

  // Wait for the message that contains the result of the memory write
  // operation.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForOKMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Handle the possible reply messages: OK, UNSUPPORTED, ERROR
  return handleStandardReply(message,
                             "Error: Set Register operation unsupported",
                             "Error: Couldn't set register value (Code %s)");
}

/**
 * Reads memory from the target process into a buffer.
 *
 * @param buffer The buffer where the memory values are stored.
 * @param address The address from where the memory is read.
 * @param size Number of bytes to read.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::readMemoryData(char* buffer, CPUADDRESS address,
                                 CPUADDRESS size, IEventCallback* cb) const {


  const CPUADDRESS READ_SIZE = getMaximumReadSize();

  CPUADDRESS toRead = size;
  CPUADDRESS alreadyRead = 0;

  do {
    CPUADDRESS readNow = size - alreadyRead;

    if (readNow > READ_SIZE) {
      readNow = READ_SIZE;
    }

    if (readNow <= 0) {
      break;
    }

    char addrbuffer[64] = { 0 };
#ifdef WIN32
    sprintf_s(addrbuffer, "m%x,%x", address + alreadyRead, readNow);
#else
    sprintf(addrbuffer, "m%x,%x", address + alreadyRead, readNow);
#endif

    msglog->log(LOG_VERBOSE, "Sending read memory from %X to %X message",
                address + alreadyRead, address + alreadyRead + readNow);

    std::string packet = packetify(addrbuffer);

    HANDLE_NAVI_ERROR(
        sendAndWaitForAck(packet, cb),
        "Error: Couldn't read memory data from the GDB server (Code %d)")

    // Wait for the message that contains the result of the memory write
    // operation.
    std::string message;
    HANDLE_NAVI_ERROR(
        waitForDataMessage(message, cb),
        "Error: Couldn't receive packet reply from GDB server (Code %d)")

    // Acknowledge the reception of the message.
    HANDLE_NAVI_ERROR(
        sendAck(),
        "Error: Couldn't send ACK message to the GDB server (Code %d)");

    if (isUnsupportedMessage(message)) {
      msglog->log(LOG_ALWAYS, "Error: Memory read operations are unsupported");
      return NaviErrors::UNSUPPORTED;
    } else if (isErrorMessage(message)) {
      msglog->log(
          LOG_ALWAYS,
          "Error: GDB server couldn't read the specified memory (Code: %s)",
          unpacketify(message).c_str());
      return NaviErrors::COULDNT_READ_MEMORY;
    } else if (isDataMessage(message)) {
      std::string data = unpacketify(message);

      for (CPUADDRESS i = 0; i < readNow; i++) {
        char tbuff[3] = { 0 };

        tbuff[0] = data[2 * i];
        tbuff[1] = data[2 * i + 1];

        //				msglog->log(LOG_VERBOSE, "alreadyRead + i: %d", alreadyRead + i);

        buffer[alreadyRead + i] = (char) strtoul(tbuff, 0, 16);
      }
    } else {
      msglog->log(LOG_ALWAYS, "Error: Unexpected GDB reply %s",
                  message.c_str());
      return NaviErrors::UNEXPECTED_GDB_REPLY;
    }

    toRead -= readNow;
    alreadyRead += readNow;

    //		msglog->log(LOG_VERBOSE, "toRead: %d - readNow: %d - alreadyRead: %d",
    //toRead, readNow, alreadyRead);

  } while (toRead > 0);

  return NaviErrors::SUCCESS;
}

/**
 * Overwrites memory in the target process.
 *
 * @param New values of the memory.
 * @param Start address where the buffer is written to in the target process.
 * @param size Number of bytes to copy from whe buffer.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::writeMemoryData(const char* buffer, CPUADDRESS address,
                                  unsigned int size, IEventCallback* cb) const {


  // Create the command and packetify it
  std::string command = createWriteMemoryCommand(address, size, buffer);
  std::string packet = packetify(command);

  // Send the packet and wait for the ACK/NACK message
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck(packet, cb),
      "Error: Couldn't send write memory command to the GDB server (Code %d)");

  // Wait for the message that contains the result of the memory write
  // operation.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForOKMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Handle the possible reply messages: OK, UNSUPPORTED, ERROR
  return handleStandardReply(
      message, "Error: Write Memory operation unsupported",
      "Error: Couldn't write to the memory of the target process (Code %s)");
}

NaviError GdbCpu::getMemmap(std::vector<CPUADDRESS>& addresses,
                            IEventCallback* cb) const {
  // TODO: Useless function; too slow



  CPUADDRESS offset = 0;

  unsigned int consecutiveRegions = 0;

  char buffer[2];

  unsigned int pageSize = getPageSize();

  do {
    // To get the valid pages, we iterate over all pages and try to read the
    // first byte of each page.

    NaviError readResult = readMemoryData(&buffer[0], offset, offset + 1, cb);

    if (readResult == NaviErrors::SUCCESS) {
      // If we can read the first byte, then the entire page is mapped.

      consecutiveRegions++;

      if (consecutiveRegions == 1) {
        msglog->log(LOG_VERBOSE, "Found memory section between %X and %X",
                    (CPUADDRESS) offset, (CPUADDRESS) offset + pageSize);

        addresses.push_back((CPUADDRESS) offset);
        addresses.push_back(((CPUADDRESS) offset + pageSize));
      } else {
        msglog->log(LOG_VERBOSE, "Extending memory section to %X",
                    addresses[addresses.size() - 1] + (CPUADDRESS) pageSize);

        addresses[addresses.size() - 1] += (CPUADDRESS) pageSize;
      }
    } else {
      consecutiveRegions = 0;
    }

    offset += pageSize;

  } while (offset != 0);

  // TODO: Fails if last page is paged
  return NaviErrors::SUCCESS;
}

unsigned int parseActiveThreadId(const std::string& message) {
  // message = "$QC*#??"
  unsigned int start = message.find("$QC") + 3;
  unsigned int end = message.find("#");

  std::string tidString = message.substr(start, end - start);

  return zylib::zycon::parseString<unsigned int>(tidString);
}

NaviError GdbCpu::getActiveThread(unsigned int& activeThread,
                                  IEventCallback* cb) const {


  DebuggerOptions options = getDebuggerOptions();

  if (!options.canMultithread) {
    activeThread = 0;

    return NaviErrors::SUCCESS;
  }

  std::string msg = packetify("qC");

  // Send the thread command to the GDB server and wait for the ACK/NACK
  HANDLE_NAVI_ERROR(
      sendAndWaitForAck(msg, cb),
      "Error: Couldn't send TID packet to the GDB server (Code %d)");

  // Wait for the message that contains TID.
  std::string message;
  HANDLE_NAVI_ERROR(
      waitForActiveThreadMessage(message, cb),
      "Error: Couldn't receive packet reply from GDB server (Code %d)");

  // Acknowledge the reception of the message.
  HANDLE_NAVI_ERROR(sendAck(), "Error: Couldn't acknowledge message (Code %d)");

  // Reply: QCpid
  if (message.find("$QC") == 0) {
    activeThread = parseActiveThreadId(message);

    // The last TID was found.
    return NaviErrors::SUCCESS;
  } else if (isUnsupportedMessage(message)) {
    msglog->log(LOG_ALWAYS, "Error: TID requests are unsupported");
    return NaviErrors::UNSUPPORTED;
  } else if (isErrorMessage(message)) {
    msglog->log(LOG_ALWAYS,
                "Error: GDB server couldn't receive the TIDs (Code: %s)",
                unpacketify(message).c_str());
    return NaviErrors::COULDNT_READ_MEMORY;
  } else {
    msglog->log(LOG_ALWAYS, "Error: Unexpected GDB reply %s", message.c_str());
    return NaviErrors::UNEXPECTED_GDB_REPLY;
  }
}

/**
 * Retrieves the TIDs of the currently active threads in the target process.
 *
 * @tids The output vector where the TIDs are stored.
 * @param cb Callback object for stop-reply messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError GdbCpu::getThreads(std::vector<Thread>& tids,
                             IEventCallback* cb) const {


  DebuggerOptions options = getDebuggerOptions();

  if (!options.canMultithread) {
    Thread t(0, SUSPENDED);

    tids.push_back(t);

    return NaviErrors::SUCCESS;
  }

  // Steps to get the TIDs:
  //   1. Send qfThreadInfo
  //   2. Send qsThreadInfo until reply is 'l'

  const char* msg = "$qfThreadInfo#bb";

  do {
    // Send the thread command to the GDB server and wait for the ACK/NACK
    HANDLE_NAVI_ERROR(
        sendAndWaitForAck(msg, cb),
        "Error: Couldn't send TID packet to the GDB server (Code %d)");

    // Change the message on subsequent operations
    msg = "$qsThreadInfo#c8";

    // Wait for the message that contains the TIDs.
    std::string message;
    HANDLE_NAVI_ERROR(
        waitForTidMessage(message, cb),
        "Error: Couldn't receive packet reply from GDB server (Code %d)");

    // Acknowledge the reception of the message.
    HANDLE_NAVI_ERROR(sendAck(),
                      "Error: Couldn't acknowledge message (Code %d)");

    // Handle the possible reply messages: MORE TIDS, LAST TID, UNSUPPORTED,
    // ERROR
    if (message.find("$l#") == 0) {
      // The last TID was found.
      return NaviErrors::SUCCESS;
    } else if (isPrefixMessage('m', message)) {
      // More TIDs to come.
      processTidString(unpacketify(message), tids);
    } else if (isUnsupportedMessage(message)) {
      msglog->log(LOG_ALWAYS, "Error: TID requests are unsupported");
      return NaviErrors::UNSUPPORTED;
    } else if (isErrorMessage(message)) {
      msglog->log(LOG_ALWAYS,
                  "Error: GDB server couldn't receive the TIDs (Code: %s)",
                  unpacketify(message).c_str());
      return NaviErrors::COULDNT_READ_MEMORY;
    } else {
      msglog->log(LOG_ALWAYS, "Error: Unexpected GDB reply %s",
                  message.c_str());
      return NaviErrors::UNEXPECTED_GDB_REPLY;
    }
  } while (true);
}

bool GdbCpu::testCommand(
    const std::string& outputMessage, const std::string& sendMessage,
    bool (GdbCpu::*isExpectedMessage)(const std::string&) const,
    bool printReply) const {
  msglog->log(LOG_ALWAYS, outputMessage.c_str());

  send(packetify(sendMessage));

  char ack;

  // Read the next byte from the GDB server
  transport->read(&ack, 1);

  if (ack == '+') {
    msglog->log(LOG_ALWAYS, "Success: Command recognized");
  } else if (ack == '-') {
    msglog->log(LOG_ALWAYS, "Success: Command not recognized");

    return false;
  } else {
    msglog->log(LOG_ALWAYS, "FAILURE: Unknown Reply");

    return false;
  }

  SimpleCallback cb;

  std::string message;
  waitForMessage(message, &cb, isExpectedMessage, false);

  sendAck();

  if (isUnsupportedMessage(message)) {
    msglog->log(LOG_ALWAYS, "FAILURE: Command not supported");

    return false;
  } else if (isExpectedMessage == 0 || (this->*isExpectedMessage)(message)) {
    msglog->log(LOG_ALWAYS, "Success: Command supported");

    if (printReply) {
      msglog->log(LOG_ALWAYS, "Received reply: %s", message.c_str());
    }

    return true;
  } else {
    msglog->log(LOG_ALWAYS, "FAILURE: Reply has unexpected format");

    msglog->log(LOG_ALWAYS, "Received reply message %s", message.c_str());

    return false;
  }
}

void GdbCpu::testRun() const {
  msglog->log(
      LOG_ALWAYS,
      "--------------------- Starting Test Mode ----------------------");

  if (needsRestarting()) {
    if (startServer() != NaviErrors::SUCCESS) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't start the GDB server");

      return;
    }

    if (needsRestarting()) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't start the GDB server");

      return;
    }
  }

  setSuspended(true);

  cachedRegisterString = "";

  // This command is actually supposed to return a Stop Reply packet but Ubuntu
  // returns a data message
  testCommand("Testing GDB command '?' (Indicate the reason the target halted)",
              "?", &GdbCpu::isBreakpointMessage);

  // testCommand("Testing GDB command 'A' (Initialized argv[] array passed into
  // program)", "g", &GdbCpu::isDataMessageReply);

  if (testCommand("Testing GDB command 'B/S' (Set Breakpoint; Obsolete)",
                  "B100000,S", &GdbCpu::isOkMessageReply)) {
    testCommand("Testing GDB command 'B/C' (Clear Breakpoint; Obsolete)",
                "B100000,S", &GdbCpu::isOkMessageReply);
  }

  // testCommand("Testing GDB command 'C' (Continue with signal)", "C 12",
  // &GdbCpu::isBreakpointMessage);

  // testCommand("Testing GDB command 'F' (Continue with signal)", "C 12",
  // &GdbCpu::isBreakpointMessage);

  if (testCommand("Testing GDB command 'g' (Read General Registers)", "g",
                  &GdbCpu::isDataMessageReply)) {
    std::string regString;
    SimpleCallback cb;

    readRegisterString(0, regString, &cb);

    msglog->log(LOG_ALWAYS, "Received registers string: %s", regString.c_str());

    testCommand("Testing GDB command 'G' (Write General Registers)",
                "G" + regString, &GdbCpu::isOkMessageReply);
  }

  if (testCommand("Testing GDB command 'qOffsets' (Get current targets "
                  "relocation information)",
                  "qOffsets", &GdbCpu::isDataMessageReply)) {
    std::string relocString;
    SimpleCallback cd;

  }

  testCommand("Testing GDB command 'H' (Set thread for subsequent operations)",
              "Hs0", &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'i' (Step the remote target by a single "
              "clock cycle)",
              "i", &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'I' (Signal, then cycle step)", "I",
              &GdbCpu::isBreakpointMessage);

  // testCommand("Testing GDB command 'k' (Kill request)", "!",
  // &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'm' (Read memory)", "m100000,10",
              &GdbCpu::isDataMessageReply);

  testCommand("Testing GDB command 'M' (Write memory)", "M100000,1:00",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'qC' (Return the current thread ID)", "qC",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'qfThreadInfo' (Obtain a list of all active "
              "thread ids from the target)",
              "qfThreadInfo", &GdbCpu::isTidMessageReply);

  testCommand("Testing GDB command 'p' (Read register)", "p 0",
              &GdbCpu::isDataMessageReply);

  testCommand("Testing GDB command 'P' (Write register)", "P 0=00000000",
              &GdbCpu::isOkMessageReply);

  // Command has no reply => Cannot test
  // testCommand("Testing GDB command 'R' (Restart the program being debugged)",
  // "R", 0);

  testCommand("Testing GDB command 's' (Single Step)", "s",
              &GdbCpu::isBreakpointMessage);

  testCommand("Testing GDB command 'T' (Find out if thread XX is alive)", "T00",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'X' (Write memory)", "M100000,1:00",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'Z0' (Memory Breakpoint)", "Z0,100000,01",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'Z1' (Hardware Breakpoint)", "Z1,100000,01",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'Z2' (Write Watchpoint)", "Z2,100000,01",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'Z3' (Read Watchpoint)", "Z3,100000,01",
              &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'Z4' (Access Watchpoint)", "Z4,100000,01",
              &GdbCpu::isOkMessageReply);

  testCommand(
      "Testing GDB command 'qSupported' (Tell the remote stub about features "
      "supported by GDB, and query the stub for features it supports)",
      "qSupported", 0, true);

  testCommand("Testing GDB command 'qXfer:auxv:read' (Access the target's "
              "auxiliary vector)",
              "qXfer:auxv:read::0,100", 0);

  testCommand("Testing GDB command 'qXfer:libraries:read' (Access the target's "
              "list of loaded libraries)",
              "qXfer:libraries:read::0,100", &GdbCpu::isDataMessageReply);

  // This command is actually supposed to return 'OK' but the Ubuntu GDB server
  // does
  // not do that.
  // testCommand("Testing GDB command '!' (Enable Extended Mode)", "!",
  // &GdbCpu::isOkMessageReply);

  testCommand("Testing GDB command 'c' (Continue)", "c",
              &GdbCpu::isBreakpointMessage);

  // testCommand("Testing GDB command 'D' (Detach)", "D",
  // &GdbCpu::isOkMessageReply);
}
