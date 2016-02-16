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

#ifndef BREAKPOINTHANDLER_HPP
#define BREAKPOINTHANDLER_HPP

#include <map>
#include <vector>

#include "../defs.hpp"
#include "IEventCallback.hpp"

/**
 * Handler class that keeps track of breakpoints in the GDB client.
 */
class BreakpointHandler {

#ifdef UNITTESTING
public:
#else
 private:
#endif

  // Used to store the original data of memory values.
  std::map<CPUADDRESS, std::vector<char> > originalData;

  // Opcode of the instruction that causes a breakpoint exception
  std::vector<char> breakpointData;

 public:

  /**
   * Creates a new breakpoint handler.
   *
   * @param breakpointData Breakpoint opcode
   */
  BreakpointHandler(std::vector<char> breakpointData)
      : breakpointData(breakpointData) {
  }

  // Sets a breakpoint in the target process
  NaviError setBreakpoint(CPUADDRESS address, IEventCallback* cb) const;

  // Removes a breakpoint from the target process
  NaviError removeBreakpoint(CPUADDRESS address, IEventCallback* cb);

  // Stores the original data that is replaced by a given breakpoint.
  NaviError storeOriginalData(CPUADDRESS address, IEventCallback* cb);

  // Fills a buffer with memory data from the current process.
  virtual NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to,
                                   IEventCallback* cb) const = 0;

  // Writes the content of a buffer to the process memory.
  virtual NaviError writeMemoryData(const char* buffer, CPUADDRESS from,
                                    unsigned int size,
                                    IEventCallback* cb) const = 0;
};

#endif
