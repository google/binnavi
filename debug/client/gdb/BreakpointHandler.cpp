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

#include "BreakpointHandler.hpp"

#include "../logger.hpp"
#include "../errors.hpp"

/**
 * Sets a breakpoint at the specified address.
 *
 * @param address The address of the breakpoint.
 * @param cb Callback object for OOB messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError BreakpointHandler::setBreakpoint(CPUADDRESS address,
                                           IEventCallback* cb) const {


  return writeMemoryData(&breakpointData[0], address, breakpointData.size(), cb);
}

/**
 * Removes a breakpoint from the specified address.
 *
 * @param address The address of the breakpoint.
 * @param cb Callback object for OOB messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError BreakpointHandler::removeBreakpoint(CPUADDRESS address,
                                              IEventCallback* cb) {


  // Write the original data back to the address
  std::vector<char> buffer = originalData[address];

  return writeMemoryData(&buffer[0], address, buffer.size(), cb);
}

/**
 * Stores the memory data that is overwritten by a breakpoint at the specified
 * address.
 *
 * @param address The address in question.
 * @param cb Callback object for OOB messages from gbdserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError BreakpointHandler::storeOriginalData(CPUADDRESS address,
                                               IEventCallback* cb) {


  // Don't save the original data more than once.
  if (originalData.find(address) != originalData.end()) {
    return NaviErrors::SUCCESS;
  }

  // Read the memory of the target process.
  std::vector<char> buffer(breakpointData.size());
  NaviError result = readMemoryData(&buffer[0], address, breakpointData.size(),
                                    cb);

  if (result) {
    msglog->log(LOG_VERBOSE, "Error: Couldn't read memory");
    return result;
  }

  originalData[address] = buffer;

  return NaviErrors::SUCCESS;
}
