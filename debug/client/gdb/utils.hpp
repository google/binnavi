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

#ifndef UTILS_HPP
#define UTILS_HPP

#include <string>
#include <vector>
#include "../defs.hpp"

// A macro that prints an error message and returns from a function if an
//operation failed
#define HANDLE_NAVI_ERROR(cmd, msg)         \
  {                                         \
    NaviError result = cmd;                 \
    if (result) {                           \
      msglog->log(LOG_ALWAYS, msg, result); \
      return result;                        \
    }                                       \
  }

// Turns a GDB command into a GDB command packet
std::string packetify(const std::string& command);

// Turns a GDB command packet into a gdb command
std::string unpacketify(const std::string& command);

// Calculates the checksum of a GDB command
unsigned char checksum(const std::string& command);

// Checks whether a character can appear in a thread ID string
bool isTidChar(char c);

// Checks whether a string is a thread ID string
bool isTidString(const std::string& msg);

// Parses a thread ID string
void processTidString(const std::string& tidString, std::vector<Thread>& tids);

#endif
