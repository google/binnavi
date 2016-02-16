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

#include "InformationProvider.hpp"

/**
 * Adds an address to the list of addresses.
 *
 * @param address The address to add.
 */
void InformationProvider::addAddress(const CPUADDRESS& address) {
  addresses.push_back(address);
}

/**
 * Returns the address with the given index.
 *
 * @param index The index of the address.
 *
 * @return The returned address.
 */
CPUADDRESS InformationProvider::getAddress(unsigned int index) const {
  return addresses[index];
}

/**
 * Modifies the address with the given index.
 *
 * @param index The index of the address.
 * @param address The new value of the address.
 */
void InformationProvider::setAddress(unsigned int index,
                                     const CPUADDRESS& address) {
  addresses[index] = address;
}

/**
 * Returns the number of addresses in the list.
 *
 * @return The number of addresses in the list.
 */
unsigned int InformationProvider::getNumberOfAddresses() const {
  return addresses.size();
}

/**
 * Returns the address list in array form.
 *
 * @return The address list in array form.
 */
const CPUADDRESS* InformationProvider::getAddresses() const {
  return &addresses[0];
}

/**
 * Changes the thread ID.
 *
 * @param tid The new thread ID.
 */
void InformationProvider::setTid(unsigned int tid) {
  this->tid = tid;
}

/**
 * Returns the thread ID.
 *
 * @return The thread ID.
 */
unsigned int InformationProvider::getTid() const {
  return tid;
}

void InformationProvider::setBreakpointResults(
    std::vector<std::pair<CPUADDRESS, unsigned int> > results) {
  breakpointResults = results;
}

std::vector<std::pair<CPUADDRESS, unsigned int> >
    InformationProvider::getBreakpointResults() const {
  return breakpointResults;
}

/**
 * Sets the register string.
 *
 * @param string The new register string.
 */
void InformationProvider::setRegisterString(const std::string& string) {
  regString = string;
}

/**
 * Returns the register string.
 *
 * @return The register string.
 */
std::string InformationProvider::getRegisterString() const {
  return regString;
}

/**
 * Sets the memory data.
 *
 * @param memory The new memory data.
 */
void InformationProvider::setMemoryData(const MemoryContainer& memory) {
  this->memory = memory;
}

/**
 * Returns the memory data.
 *
 * @return The memory data.
 */
MemoryContainer InformationProvider::getMemoryData() const {
  return memory;
}
