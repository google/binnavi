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

#include "boost/assign.hpp"
#include "defs.hpp"
#include <map>

#if ARCHSIZE == 32
#if UINT_MAX == 4294967295UL

// Format mask that is used to format 32bit addresses
const char* ADDRESS_FORMAT_MASK = "%08X";

/**
 * Converts a 32bit address to a 64bit address packet structure.
 *
 * @param address The address to convert.
 *
 * @return The converted address.
 */
DBG_PROTO_ARG_ADDRESS catopa(const CPUADDRESS& address) {
  DBG_PROTO_ARG_ADDRESS addr;
  addr.high32bits = 0;
  addr.low32bits = address;

  return addr;
}

/**
 * Converts a 64bit address packet structure to a 32bit address.
 *
 * @param address The address to convert.
 *
 * @return The converted address.
 */
CPUADDRESS patoca(const DBG_PROTO_ARG_ADDRESS& address) {
  return address.low32bits;
}
#else
#error You need to change some typedefs. Please read the documentation.
#endif
#elif ARCHSIZE == 64
#if ULLONG_MAX == 18446744073709551615UL

// Format mask that is used to format 64bit addresses
const char* ADDRESS_FORMAT_MASK = "%llu";

/**
 * Converts a 64bit address to a 64bit address packet structure.
 *
 * @param address The address to convert.
 *
 * @return The converted address.
 */
DBG_PROTO_ARG_ADDRESS catopa(const CPUADDRESS& address) {
  DBG_PROTO_ARG_ADDRESS addr;
  addr.high32bits = (address >> 32);
  addr.low32bits = address & 0xFFFFFFFF;

  return addr;
}

/**
 * Converts a 64bit address packet structure to a 64bit address.
 *
 * @param address The address to convert.
 *
 * @return The converted address.
 */
CPUADDRESS patoca(const DBG_PROTO_ARG_ADDRESS& address) {
  return (((CPUADDRESS) address.high32bits) << 32) | address.low32bits;
}
#else
#error You need to change some typedefs. Please read the documentation.
#endif
#else
#error Unknown architecture size
#endif

/**
 * Small helper function to create a RegisterValue object
 */
RegisterValue makeRegisterValue(const std::string& name,
                                const std::string& value,
                                const std::vector<char>& memory, bool isPc,
                                bool isSp) {
  RegisterValue rv(name, value, memory, isPc, isSp);
  return rv;
}

/**
 * Small helper function to create a RegisterValue object
 */
RegisterValue makeRegisterValue(const std::string& name,
                                const std::string& value, bool isPc,
                                bool isSp) {
  std::vector<char> memory;

  RegisterValue rv(name, value, memory, isPc, isSp);
  return rv;
}

/**
 * Small helper function to create a RegisterDescription object
 */
RegisterDescription makeRegisterDescription(const std::string& name,
                                            unsigned int size, bool editable) {
  RegisterDescription rd(name, size, editable);
  return rd;
}

/**
 * Small helper function that duplicates a string.
 */
char* duplicate(const char* str) {
  size_t numberOfElements = strlen(str) + 1;
  char* buffer = new char[numberOfElements];
#ifdef WIN32
  strcpy_s(buffer, numberOfElements, str);
#else
  strncpy(buffer, str, numberOfElements - 1);
  buffer[numberOfElements - 1] = 0;
#endif

  return buffer;
}

wchar_t* duplicate(const wchar_t* str) {
  size_t numberOfElements = wcslen(str) + 1;
  wchar_t* buffer = new wchar_t[numberOfElements];

#ifdef WIN32
  wcscpy_s(buffer, numberOfElements, str);
#else
  wcsncpy(buffer, str, numberOfElements - 1);
  buffer[numberOfElements - 1] = 0;
#endif

  return buffer;
}

bool operator==(const Module& lhs, const Module& rhs) {
  return lhs.name == rhs.name && lhs.baseAddress == rhs.baseAddress
      && lhs.size == rhs.size;
}

bool operator<(const Module& lhs, const Module& rhs) {
  return lhs.path.compare(rhs.path) < 0;
}

std::map<dbgevt_t, const char*> DebugEventMap = boost::assign::map_list_of(
    dbgevt_bp_hit, "breakpoint hit")(dbgevt_bpe_hit, "echo breakpoint hit")(
    dbgevt_bps_hit, "stepping breakpoint hit")(dbgevt_bpe_rem,
                                               "breakpoint removed")(
    dbgevt_thread_created, "thread created")(dbgevt_thread_closed,
                                             "thread closed")(
    dbgevt_module_loaded, "module loaded")(dbgevt_module_unloaded,
                                           "module unloaded")(
    dbgevt_process_closed, "process closed")(dbgevt_exception,
                                             "exception raised")(
    dbgevt_process_start, "process start");

const char* debugEventToString(dbgevt_t event) {
  std::map<dbgevt_t, const char*>::const_iterator cit = DebugEventMap.find(
      event);
  if (cit != DebugEventMap.end())
    return cit->second;
  else
    return "Unknown debug event";
}
