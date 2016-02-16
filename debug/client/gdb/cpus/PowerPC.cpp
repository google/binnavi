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

#include "PowerPC.hpp"

std::vector<char> PowerPC::getBreakpointData() const {
  char buffer[4] = { char(0x7F), char(0xE0), char(0x00), char(0x08) };

  std::vector<char> data(buffer, buffer + 4);

  return data;
}

/**
 * PowerPCs are Big Endian
 */
bool PowerPC::isLittleEndian() const {
  return false;
}
