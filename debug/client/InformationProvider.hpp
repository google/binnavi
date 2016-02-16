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

#ifndef INFORMATIONPROVIDER_H
#define INFORMATIONPROVIDER_H

#include <string>
#include <vector>

#include "defs.hpp"

/**
 * This class is used to collect information about debug events
 * and the results of debug commands.
 */
class InformationProvider {
 private:
  // Used to store memory data
  MemoryContainer memory;

  // Used to store register value strings
  std::string regString;

  // Used to store a thread ID
  unsigned int tid;

  // Used to store addresses
  std::vector<CPUADDRESS> addresses;

  std::vector<std::pair<CPUADDRESS, unsigned int> > breakpointResults;

 public:

  // Adds an address to the list of addresses
  void addAddress(const CPUADDRESS& address);

  // Returns an address from the list of addresses
  CPUADDRESS getAddress(unsigned int index) const;

  // Changes an address in the list of addresses
  void setAddress(unsigned int index, const CPUADDRESS& address);

  // Returns the number of addresses in the list
  unsigned int getNumberOfAddresses() const;

  // Address array of all addresses in the list
  const CPUADDRESS* getAddresses() const;

  // Sets the thread ID
  void setTid(unsigned int tid);

  // Returns the thread ID
  unsigned int getTid() const;

  void setBreakpointResults(
      std::vector<std::pair<CPUADDRESS, unsigned int> > results);

  std::vector<std::pair<CPUADDRESS, unsigned int> > getBreakpointResults() const;

  // Sets the register string
  void setRegisterString(const std::string& string);

  // Returns the register string
  std::string getRegisterString() const;

  // Sets the memory data
  void setMemoryData(const MemoryContainer& memory);

  // Returns the memory data
  MemoryContainer getMemoryData() const;
};

#endif
