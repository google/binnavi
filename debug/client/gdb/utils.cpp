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

#include "utils.hpp"

#include <algorithm>
#include <numeric>
#include <functional>

#include <zycon/src/zycon.h>

#include "../logger.hpp"

/**
 * Turns a command into a gdbserver message (by surrounding it with $ and #XX).
 *
 * @param command The command to turn into a valid packet.
 *
 * @return The gdbserver message.
 */
std::string packetify(const std::string& command) {
  return "$" + command + "#"
      + zylib::zycon::toHexString(checksum(command), true);
}

/**
 * Extracts the command from a gdbserver message (by dropping $ and #XX).
 *
 * Note that it is expected, that the size of the passed msg is at least 4.
 *
 * @param msg The gdbserver message.
 *
 * @return The extracted message.
 */
std::string unpacketify(const std::string& msg) {
  return msg.substr(1, msg.size() - 4);
}

/**
 * Calculates the checksum of a command.
 *
 * @param command
 *
 * @return The checksum of the command.
 */
unsigned char checksum(const std::string& command) {
  return std::accumulate(command.begin(), command.end(), 0);
}

/**
 * Determines whether a character can occur in a TID string.
 *
 * @param c The character in question.
 *
 * @return True, if the character is a valid TID string character. False,
 * otherwise.
 */
bool isTidChar(char c) {
  // Valid characters for TID strings are lower case hex values and commas to
  // separate the TIDs.

  return zylib::zycon::isLowerHex(c) || c == ',';
}

/**
 * Determines whether a given string is a valid TID string.
 *
 * @param msg The string in question.
 *
 * @return True, if the string is a valid TID string. False, otherwise.
 */
bool isTidString(const std::string& msg) {
  // TID strings have the format mTID[,TID]*

  return msg.size() > 1 && msg[0] == 'm'
      && std::find_if(msg.begin() + 1, msg.end(),
                      std::not1(std::ptr_fun(isTidChar))) == msg.end();
}

/**
 * Splits a TID string into individual TIDs.
 *
 * @param tidString The tidString to split.
 * @tids The output vector where the TIDs are stored.
 */
void processTidString(const std::string& tidString, std::vector<Thread>& tids) {


  unsigned int index = 1;

  do {
    unsigned int comma = tidString.find(",", index);

    if (comma != std::string::npos) {
      std::string tid = tidString.substr(index, comma - index);

      Thread t(zylib::zycon::parseHexString<unsigned int>(tid), SUSPENDED);

      tids.push_back(t);

      index = comma + 1;
    } else {
      std::string tid = tidString.substr(index);
      Thread t(zylib::zycon::parseHexString<unsigned int>(tid), SUSPENDED);

      tids.push_back(t);

      return;
    }
  } while (index < tidString.size());
}
