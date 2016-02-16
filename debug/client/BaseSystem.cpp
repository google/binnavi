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

#include "BaseSystem.hpp"
#include <string>
#include <limits>

#include <zycon/src/zycon.h>

#include "errors.hpp"
#include "InformationProvider.hpp"
#include "logger.hpp"
#include "DebuggerOptions.hpp"
#include "ConditionParser.hpp"

/**
 * Turns a CPUADDRESS into a std::string.
 *
 * @param address The CPUADDRESS value to convert.
 *
 * @return The converted string.
 **/
std::string cpuAddressToString(CPUADDRESS address) {
  char buffer[sizeof(CPUADDRESS) * 2 + 1];
#ifdef WIN32
  _snprintf_s(buffer, sizeof(buffer), ADDRESS_FORMAT_MASK, address);
#else
  snprintf(buffer, sizeof(buffer), ADDRESS_FORMAT_MASK, address);
  buffer[sizeof(buffer) - 1] = 0;
#endif
  return buffer;
}

/**
 * Turns binary data into a printable string.
 *
 * @param memory The binary data to convert.
 *
 * @return The printable representation of the binary data.
 **/
std::string createMemoryString(const std::vector<char>& memory) {
  std::string str;
  for (char byte : memory) {
    str += zylib::zycon::toHexString(byte, true /* pad output */ );
  }
  return str;
}

/**
 * Creates an XML string that contains the names and values of registers.
 * This string can be sent to BinNavi to tell BinNavi about register values.
 *
 * @param registers The register information is taken from here.
 *
 * @return The XML String that contains all relevant register information.
 **/
std::string createRegisterString(const RegisterContainer& registerContainer) {
  std::string ret = "<Registers>";
  std::vector<Thread> threads = registerContainer.getThreads();
  for (const Thread& thread : threads) {
    ret += "<Thread id=\"";
    ret += zylib::zycon::toString(thread.tid);
    ret += "\">";
    for (const RegisterValue& reg : thread.registers) {
      ret += "<Register name=\"";
      ret += reg.getName();
      ret += "\" value=\"";
      ret += reg.getValue();
      ret += "\" memory=\"";
      ret += createMemoryString(reg.getMemory());
      ret += "\"";
      if (reg.isPc()) {
        ret += " pc=\"true\"";
      }
      if (reg.isSp()) {
        ret += " sp=\"true\"";
      }
      ret += "/>";
    }
    ret += "</Thread>";
  }
  ret += "</Registers>";
  msglog->log(LOG_ALL, "Register String created: %s", ret.c_str());
  return ret;
}

/**
 * Creates an XML string that contains the names and process IDs of all running
 * processes.
 *
 * @param processList The process information is taken from here.
 *
 * @return The XML String that contains all relevant process information.
 **/
std::string createProcessListString(const ProcessListContainer& processList) {
  std::string ret =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><Processes>";
  for (const ProcessDescription& process : processList) {
    ret += "<Process name=\"";
    ret += process.getName();
    ret += "\" pid=\"";
    ret += zylib::zycon::toString(process.getPid());
    ret += "\"/>";
  }
  ret += "</Processes>";
  msglog->log(LOG_ALL, "Process List String created: %s", ret.c_str());
  return ret;
}

/**
 * Creates an XML string that contains the information about drives, files, and
 * directories.
 *
 * @param fileList The file system information is taken from here.
 *
 * @return The XML String that contains all relevant file system information.
 **/
std::string createFileListString(const FileListContainer& fileList) {
  std::string ret =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><FileSystem>";
  ret += "<Directory name=\"";
  ret += fileList.getDirectory().string();
  ret += "\"/><Drives>";

  for (const auto& drive : fileList.getDrives()) {
    ret += "<Drive name=\"";
    ret += drive.string();
    ret += "\"/>";
  }
  ret += "</Drives><Directories>";
  for (const auto& dir : fileList.getDirectories()) {
    ret += "<Directory name=\"";
    ret += dir.string();
    ret += "\"/>";
  }
  ret += "</Directories><Files>";
  for (const auto& file : fileList.getFiles()) {
    ret += "<File name=\"";
    ret += file.string();
    ret += "\"/>";
  }
  ret += "</Files></FileSystem>";
  msglog->log(LOG_ALL, "File System String created: %s", ret.c_str());
  return ret;
}

/**
 * Searches for a string of bytes in a byte buffer.
 *
 * @param buffer The byte buffer.
 * @param bufferSize The size of the byte buffer.
 * @param searchString The search string.
 * @param stringLen The size of the searchString.
 *
 * @return The position where the string was found or MAXINT if the string was
 * not found.
 **/
unsigned int search(const char* buffer, unsigned int bufferSize,
                    const char* searchString, unsigned int stringLen) {
  // TODO: that's quadratic time complexity, can be really slow for processes
  //  with large memory use when searching for common patterns,
  //  e.g. 00000000 + smth_nonzero
  for (int i = 0; i < bufferSize; ++i) {
    for (int j = 0; j < stringLen && i + j < bufferSize &&
                        buffer[i + j] == searchString[j];
         ++j) {
      if (j == stringLen - 1) {
        return i;
      }
    }
  }
  return std::numeric_limits<unsigned int>::max();
}

/**
 * Process incoming packets containing information on how to handle runtime
 * exceptions.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful.
 **/
NaviError BaseSystem::processSetExceptionSettings(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  // each argument consists of a 2-tuple: <code, action>
  unsigned int numberOfArguments = p->hdr.argument_num / 2;
  for (unsigned int i = 0; i < numberOfArguments; ++i) {
    if (SetExceptionAction(p->addresses[i],
                           (DebugExceptionHandlingAction)p->ints[i]) !=
        NaviErrors::SUCCESS) {
      msglog->log(LOG_ALWAYS, "Error: Can't change action for exception 0x%08p",
                  p->addresses[i]);
      return NaviErrors::COULDNT_SET_EXCEPTION_ACTION;
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Set Breakpoint operations.
 *
 * @param p The incoming packet.
 * @param info Information about breakpoint setting is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetBreakpoints(const Packet* p,
                                            InformationProvider& info) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  std::vector<std::pair<CPUADDRESS, unsigned int> > results;
  unsigned int counter = 0;
  unsigned int toSet = p->addresses.size();
  for (CPUADDRESS address : p->addresses) {
    if (hasBreakpoint(address, BPX_echo)) {
      // Overwrite echo breakpoints
      BREAKPOINT bp = getBreakpoint(address, BPX_echo);
      bp.bpx_type = BPX_simple;
      removeBreakpointFromList(address, BPX_echo);
      bpxlist.insert(address);
      results.emplace_back(address, 0);
    } else if (hasBreakpoint(address, BPX_stepping)) {
      // Overwrite stepping breakpoints
      BREAKPOINT bp = getBreakpoint(address, BPX_stepping);
      bp.bpx_type = BPX_simple;
      removeBreakpointFromList(address, BPX_stepping);
      bpxlist.insert(address);
      results.emplace_back(address, 0);
    } else {
      NaviError res = setBreakpoint(address, BPX_simple, counter != toSet - 1);
      results.emplace_back(address, res);
    }
    ++counter;
  }
  info.setBreakpointResults(results);
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Set Echo Breakpoint operations.
 *
 * @param p The incoming packet.
 * @param info Information about breakpoint setting is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetEchoBreakpoints(const Packet* p,
                                                InformationProvider& info) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  if (p->addresses.size() != p->ints[0]) {
    msglog->log(
        LOG_ALWAYS, "Error: Malformed packet passed to function; %d addresses "
                    "expected, %d addresses found (Command %s)",
        p->ints[1], p->addresses.size(), commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }

  std::vector<std::pair<CPUADDRESS, unsigned int> > results;
  unsigned int counter = 0;
  unsigned int toSet = p->addresses.size();

  for (CPUADDRESS address : p->addresses) {
    if (hasBreakpoint(address, BPX_simple) ||
        hasBreakpoint(address, BPX_stepping)) {
      // This shouldn't actually ever happen. If it does,
      // there's a bug in BinNavi.
      // Don't overwrite other breakpoints.
      results.emplace_back(address, NaviErrors::HIGHER_BREAKPOINT_EXISTS);
    } else {
      NaviError setResult =
          setBreakpoint(address, BPX_echo, counter != toSet - 1);
      results.emplace_back(address, setResult);
    }
    counter++;
  }
  info.setBreakpointResults(results);
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Set Stepping Breakpoint operations.
 *
 * @param p The incoming packet.
 * @param info Information about breakpoint setting is stored here.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetSteppingBreakpoints(
    const Packet* p, InformationProvider& info) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->addresses.empty()) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  std::vector<std::pair<CPUADDRESS, unsigned int> > results;
  unsigned int counter = 0;
  unsigned int toSet = p->addresses.size();
  for (CPUADDRESS address : p->addresses) {
    if (hasBreakpoint(address, BPX_simple)) {
      // This shouldn't actually ever happen. If it does,
      // there's a bug in BinNavi.
      // Don't overwrite regular breakpoints.
      results.emplace_back(address, NaviErrors::HIGHER_BREAKPOINT_EXISTS);
    } else if (hasBreakpoint(address, BPX_echo)) {
      // Echo breakpoints can be overwritten.
      BREAKPOINT bp = getBreakpoint(address, BPX_echo);
      bp.bpx_type = BPX_stepping;
      removeBreakpointFromList(address, BPX_echo);
      sbpxlist.insert(address);
      results.emplace_back(address, 0);
    } else {
      NaviError res =
          setBreakpoint(address, BPX_stepping, counter != toSet - 1);
      results.emplace_back(address, 0);
    }
    ++counter;
  }
  info.setBreakpointResults(results);
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Remove Simple Breakpoint operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processRemoveBreakpoints(const Packet* p,
                                               InformationProvider& provider) {
  std::vector<std::pair<CPUADDRESS, unsigned int>> result;
  NaviError res = processRemoveBreakpoints(p, BPX_simple, result);
  if (res) {
    return res;
  }
  provider.setBreakpointResults(result);
  return res;
}

/**
 * Processes incoming packets for Remove Echo Breakpoint operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processRemoveEchoBreakpoints(
    const Packet* p, InformationProvider& provider) {
  std::vector<std::pair<CPUADDRESS, unsigned int>> result;
  NaviError res;
  if (!getDebuggerOptions().canTraceCount) {
    res = processFakeRemoveBreakpoints(p, result);
  } else {
    res = processRemoveBreakpoints(p, BPX_echo, result);
    if (res) {
      return res;
    }
  }
  provider.setBreakpointResults(result);
  return res;
}

/**
 * Processes incoming packets for Remove Stepping Breakpoint operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processRemoveSteppingBreakpoint(
    const Packet* p, InformationProvider& provider) {
  std::vector<std::pair<CPUADDRESS, unsigned int>> result;
  NaviError res = processRemoveBreakpoints(p, BPX_stepping, result);
  if (res) {
    return res;
  }
  provider.setBreakpointResults(result);
  return res;
}

/**
 * Frees a breakpoint condition node and all of its child nodes.
 *
 * @param node The breakpoint condition node to free.
 **/
void freeCondition(ConditionNode* node) {
  if (node) {
    for (auto* child: node->getChildren()) {
      freeCondition(child);
    }
  }
  delete node;
}

/**
 * Processes breakpoint condition packets.
 *
 * @param p Contains information about the breakpoint condition.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetBreakpointCondition(const Packet* p) {
  CPUADDRESS address = p->addresses[0];
  if (!hasBreakpoint(address, BPX_simple)) {
    return NaviErrors::INVALID_BREAKPOINT;
  }

  ConditionNode* previousCondition =
      conditions.find(address) == conditions.end() ? 0 : conditions[address];
  freeCondition(previousCondition);
  conditions.erase(address);
  if (p->data.size()) {
    msglog->log(LOG_ALL,
                "Received a new breakpoint condition for breakpoint 0x%X",
                address);
    ConditionNode* condition;
    const char* start = &p->data[0];
    NaviError parseResult =
        parseConditionNodes(start, start + p->data.size(), condition);
    if (parseResult) {
      return NaviErrors::INVALID_CONDITION_TREE;
    }
    conditions[address] = condition;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes Write Memory packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processWriteMemory(const Packet* p) {
  return writeMemory(p->addresses[0], p->data);
}

/**
 * Processes incoming packets for Single Step operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSingleStep(const Packet* p,
                                        InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  CPUADDRESS address;
  NaviError stepResult = doSingleStep(activeThread, address);
  provider.setTid(activeThread);
  provider.addAddress(address);
  if (stepResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't perform single step (Code %d)",
                stepResult);
    return stepResult;
  }
  RegisterContainer registers;
  NaviError readResult = readRegisters(registers);
  if (readResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read register value (Code %d)",
                readResult);
    return readResult;
  }
  provider.setRegisterString(createRegisterString(registers));
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Resume operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processResume(const Packet* p,
                                    InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  provider.setTid(activeThread);
  return resume(activeThread);
}

/**
 * Processes incoming packets for Halt operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processHalt(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  return halt();
}

/**
 * Processes incoming packets for Detach operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processDetach(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  NaviError echoResult = clearBreakpoints(ebpxlist, BPX_echo);
  if (echoResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't delete all echo breakpoints");
  }
  NaviError stepResult = clearBreakpoints(sbpxlist, BPX_stepping);
  if (stepResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't delete all echo breakpoints");
  }
  NaviError regResult = clearBreakpoints(bpxlist, BPX_simple);
  if (regResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't delete all regular breakpoints");
  }
  NaviError detachResult = detach();
  if (echoResult) {
    return echoResult;
  } else if (stepResult) {
    return stepResult;
  } else if (regResult) {
    return regResult;
  } else if (detachResult) {
    return detachResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Terminate operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processTerminate(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->addresses.size() != 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  NaviError tresult = terminateProcess();
  if (tresult) {
    msglog->log(LOG_ALWAYS,
                "Error: Couldn't terminate the target process (Code %d)",
                tresult);
    return tresult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Read Registers operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the register
 * values is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processRegisters(const Packet* p,
                                       InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  msglog->log(LOG_VERBOSE, "Trying to read the registers of all threads");
  RegisterContainer registers;
  NaviError readResult = readRegisters(registers);
  if (readResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read register value (Code %d)",
                readResult);
    return readResult;
  }
  provider.setRegisterString(createRegisterString(registers));
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for List Processes operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the running
 * processes is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processListProcesses(const Packet* p,
                                           InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  msglog->log(LOG_VERBOSE,
              "Trying to read the process list of the target system");
  ProcessListContainer processes;
  NaviError readResult = readProcessList(processes);
  if (readResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read processes list (Code %d)",
                readResult);
    return readResult;
  }
  provider.setRegisterString(createProcessListString(processes));
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for List Files operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the file system
 * is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processListFiles(const Packet* p,
                                       InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  msglog->log(LOG_VERBOSE, "Trying to read the file list of the target system");
  FileListContainer files;
  if (p->data.size() == 0) {
    NaviError readResult = readFiles(files);

    if (readResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't read file list (Code %d)",
                  readResult);
      return readResult;
    }
    provider.setRegisterString(createFileListString(files));
  } else {
    std::string path(p->data.begin(), p->data.end());
    NaviError readResult = readFiles(files, path);
    if (readResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't read file list (Code %d)",
                  readResult);
      return readResult;
    }
    provider.setRegisterString(createFileListString(files));
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes Select Process packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSelectProcess(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  pid = p->ints[0];
  return NaviErrors::SUCCESS;
}

/**
 * Processes Select File packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSelectFile(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  std::string path(p->data.begin(), p->data.end());
#ifdef UNICODE
  msglog->log(LOG_ALWAYS, "Implement this");
  std::exit(0);
#else
  this->path = normalize(boost::filesystem::path(path).make_preferred());
#endif
  return NaviErrors::SUCCESS;
}

/**
 * Normalize the given path, i.e. remove duplicate path separators and use
 * boost::filesystem to normalize the result.
 */
boost::filesystem::path BaseSystem::normalize(
    const boost::filesystem::path& p) const {
  const std::string& tmp = p.string();
  if (tmp.size() > 2) {
    std::string cleanString;
    for (size_t pos = 0; pos + 1 < tmp.size(); ++pos) {
      if (tmp[pos] == tmp[pos + 1] && (tmp[pos] == '\\' || tmp[pos] == '/')) {
        continue;
      }
      cleanString += tmp[pos];
    }
    cleanString += *tmp.rbegin();
    return boost::filesystem::path(cleanString).normalize();
  }
  return p;
}

/**
 * Processes incoming packets for Write Register operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetRegister(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 2) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  if (p->addresses.size() != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  unsigned int tid = p->ints[0];
  unsigned int registerIndex = p->ints[1];
  CPUADDRESS address = p->addresses[0];
  return setRegister(tid, registerIndex, address);
}

/**
 * Reloads previously requested memory.
 *
 * @param p Will be filled with range information.
 * @param provider Will be filled with the memory data.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::reloadMemory(Packet* p, InformationProvider& provider) {
  cachedIndex = (cachedIndex + 1) % 5;
  if (cachedIndex >= cachedMemoryReads.size()) {
    return NaviErrors::NOTHING_TO_REFRESH;
  }
  CPUADDRESS address = cachedMemoryReads[cachedIndex].first;
  CPUADDRESS size = cachedMemoryReads[cachedIndex].second;
  p->addresses.push_back(address);
  p->addresses.push_back(size);
  MemoryContainer memoryData;
  memoryData.resize(size);
  NaviError result = readMemoryData(&memoryData[0], address, size);
  provider.setMemoryData(memoryData);
  return result;
}

/**
 * Processes incoming packets for Read Memory operations.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processMemoryRange(const Packet* p,
                                         InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->addresses.size() != 2) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  CPUADDRESS address = p->addresses[0];
  CPUADDRESS size = p->addresses[1];
  msglog->log(LOG_VERBOSE, "Reading %d bytes from memory address %X", size,
              address);
  if (size <= 0) {
    msglog->log(LOG_ALWAYS, "Error: %s: Size argument must be positive",
                __FUNCTION__);
    return NaviErrors::INVALID_MEMORY_RANGE;
  }
  MemoryContainer memoryData;
  memoryData.resize(size);
  NaviError result = readMemoryData(&memoryData[0], address, size);
  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read memory data (Code %d)",
                result);
    return result;
  }
  provider.setMemoryData(memoryData);
  if (cachedMemoryReads.size() == 5) {
    cachedMemoryReads.resize(4);
  }
  cachedMemoryReads.insert(cachedMemoryReads.begin(),
                           std::make_pair(address, size));
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Valid Memory operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the memory region
 * is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processValidMem(const Packet* p,
                                      InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->addresses.size() != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  CPUADDRESS from;
  CPUADDRESS to;
  msglog->log(LOG_VERBOSE, "Trying to find the valid memory around %X",
              p->addresses[0]);
  NaviError result = getValidMemory(p->addresses[0], from, to);
  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't get range of valid memory");
    return result;
  }
  msglog->log(LOG_VERBOSE, "Found lower bound %X", from);
  msglog->log(LOG_VERBOSE, "Found upper bound %X", to);
  provider.addAddress(from);
  provider.addAddress(to);
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Memory Map operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the memory
 * regions is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processMemmap(const Packet* p,
                                    InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  std::vector<CPUADDRESS> addresses;
  NaviError memerror = getMemmap(addresses);
  if (memerror) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't get memory ranges");
    return memerror;
  }
  for (CPUADDRESS address : addresses) {
    provider.addAddress(address);
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes incoming packets for Search operations.
 *
 * @param p The incoming packet.
 * @param provider Information provider where information about the search result
 * is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSearch(const Packet* p,
                                    InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->addresses.size() != 2) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  if (p->data.size() == 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  CPUADDRESS from = p->addresses[0];
  CPUADDRESS to = p->addresses[1];
  const char* searchString = &p->data[0];
  return searchData(from, to, searchString, p->data.size(), provider);
}

/**
 * Processes incoming packets for generic Remove Breakpoint operations.
 *
 * @param p The incoming packet.
 * @param type The type of the breakpoint.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processRemoveBreakpoints(
    const Packet* p, BPXType type,
    std::vector<std::pair<CPUADDRESS, unsigned int> >& result) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  unsigned int counter = p->ints[0];
  if (p->addresses.size() != counter) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %s)",
                commandToString(p->hdr.command));
    return NaviErrors::MALFORMED_PACKET;
  }
  for (size_t i = 0; i < p->addresses.size(); i++) {
    msglog->log(LOG_ALL, "INFO: Address in cmd_rembpe packet %X",
                p->addresses[i]);
  }
  unsigned int removed = 0;
  unsigned int toRemove = p->addresses.size();
  for (CPUADDRESS address : p->addresses) {
    if (hasBreakpoint(address, type)) {
      BREAKPOINT bp = getBreakpoint(address, type);
      NaviError removeResult = removeBreakpoint(bp, removed != toRemove - 1);
      removed++;
      if (removeResult) {
        result.emplace_back(address, removeResult);
        msglog->log(
            LOG_ALWAYS,
            "Error: Couldn't remove breakpoint from the target process");
        continue;
      }
      removeBreakpointFromList(address, type);
      std::list<unsigned int> toRemove;
      for (auto breakpoint : currentBreakpoints) {
        if (breakpoint.second == address) {
          toRemove.push_back(breakpoint.first);
        }
      }
      for (unsigned int threadId : toRemove) {
        currentBreakpoints.erase(threadId);
      }
      result.emplace_back(address, NaviErrors::SUCCESS);
    } else {
      msglog->log(LOG_ALL, "Error: No breakpoint set at address 0x%X", address);
      result.emplace_back(address, NaviErrors::INVALID_BREAKPOINT);
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Processes Suspend Thread packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSuspendThread(const Packet* p) {
  return suspendThread(p->ints[0]);
}

/**
 * Processes Resume Thread packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processResumeThread(const Packet* p) {
  return resumeThread(p->ints[0]);
}

/**
 * Processes Set Active Thread packets.
 *
 * @param p The incoming packet.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processSetActiveThread(const Packet* p) {
  setActiveThread(p->ints[0]);
  return NaviErrors::SUCCESS;
}

/**
 * Sets a breakpoint in the target process.

 * @param address The address of the breakpoint.
 * @param type The type of the breakpoint.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/

NaviError BaseSystem::setBreakpoint(CPUADDRESS address, const BPXType type,
                                    bool moreToCome) {
  BREAKPOINT breakPoint;
  breakPoint.bpx_type = type;
  breakPoint.addr = address;
  msglog->log(LOG_VERBOSE, "Setting breakpoint at offset 0x%X", address);
  if (hasBreakpoint(address, type)) {
    msglog->log(LOG_ALWAYS, "Error: Duplicate breakpoint at offset 0x%X",
                address);
    return NaviErrors::DUPLICATE_BREAKPOINT;
  }
  NaviError storeResult = storeOriginalData(breakPoint);
  if (storeResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't store original data");
    return storeResult;
  }
  NaviError setResult = setBreakpoint(breakPoint, moreToCome);
  if (setResult) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Couldn't set breakpoint at offset 0x%X in the target process",
        address);
    return setResult;
  } else {
    if (type == BPX_simple) {
      bpxlist.insert(address);
    } else if (type == BPX_echo) {
      ebpxlist.insert(address);
    } else if (type == BPX_stepping) {
      sbpxlist.insert(address);
    } else {
      msglog->log(LOG_ALWAYS, "Error: Invalid breakpoint type %d", type);
      return NaviErrors::INVALID_BREAKPOINT_TYPE;
    }
    return NaviErrors::SUCCESS;
  }
}

/**
 * Removes a breakpoint from the breakpoint list.
 *
 * @param address The address of the breakpoint.
 * @param type The type of the breakpoint.
 **/
void BaseSystem::removeBreakpointFromList(CPUADDRESS addr, const BPXType type) {
  if (!hasBreakpoint(addr, type)) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Trying to remove non-existing breakpoint 0x%X (Type: %d)", addr,
        type);
    return;
  }
  if (type == BPX_simple) {
    bpxlist.erase(addr);
    bpxlistrem.insert(addr);
  } else if (type == BPX_echo) {
    ebpxlist.erase(addr);
    ebpxlistrem.insert(addr);
  } else if (type == BPX_stepping) {
    sbpxlist.erase(addr);
    sbpxlistrem.insert(addr);
  } else {
    msglog->log(LOG_ALWAYS, "Error: Invalid breakpoint type %d", type);
  }
}

/**
 * Handles hits of regular breakpoints.
 *
 * @param bp Information about the breakpoint.
 * @param tid Thread ID of the thread that hit the process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::simpleBreakpointHit(const BREAKPOINT& bp,
                                          unsigned int tid, bool correctPc) {
  if (!hasBreakpoint(bp.addr, BPX_simple) && bpxlistrem.count(bp.addr) == 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Non-existing breakpoint at address 0x%X was hit",
                bp.addr);
    return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
  }
  NaviError preError = NaviErrors::SUCCESS;
  if (correctPc) {
    NaviError setResult = setInstructionPointer(tid, bp.addr);
    if (setResult) {
      msglog->log(
          LOG_ALWAYS,
          "Error: Couldn't set instruction pointer to address 0x%X (Code %d)",
          bp.addr, setResult);
      preError = setResult;
    }
  }
  RegisterContainer registers;
  if (preError == NaviErrors::SUCCESS) {
    NaviError readResult = readRegisters(registers);
    if (readResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't read register value (Code %d)",
                  readResult);
      preError = readResult;
    }
  }
  ConditionNode* condition =
      preError || conditions.find(bp.addr) == conditions.end()
          ? 0
          : conditions[bp.addr];
  if (condition) {
    msglog->log(LOG_VERBOSE, "Hit breakpoint is conditional");
  }
  bool conditionMet = !condition || condition->evaluate(tid, registers, this);
  if (conditionMet) {
    msglog->log(LOG_VERBOSE, "Breakpoint condition was met");
  } else {
    msglog->log(LOG_VERBOSE, "Breakpoint condition was not met");
  }
  DBGEVT dbgevt;
  dbgevt.bp = bp;
  dbgevt.tid = tid;
  dbgevt.type = dbgevt_bp_hit;
  if (currentBreakpoints.find(tid) != currentBreakpoints.end()) {
    currentBreakpoints.erase(tid);
  }
  currentBreakpoints[tid] = bp.addr;
  NaviError remResult = removeBreakpoint(bp, false);
  if (remResult) {
    msglog->log(LOG_ALWAYS,
                "Error: Couldn't remove breakpoint at address 0x%X (Code %d)",
                bp.addr, remResult);
    return remResult;
  }
  if (preError) {
    return preError;
  }
  dbgevt.registerString = createRegisterString(registers);
  if (conditionMet) {
    addDebugEvent(dbgevt);
    return NaviErrors::SUCCESS;
  } else {
    return resume(tid);
  }
}

/**
 * Handles hits of echo breakpoints.
 *
 * @param bp Information about the breakpoint.
 * @param tid Thread ID of the thread that hit the process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::echoBreakpointHit(const BREAKPOINT& bp, unsigned int tid,
                                        bool correctPc, bool doResume) {
  const bool breakpointExists = hasBreakpoint(bp.addr, BPX_echo);
  if (!breakpointExists && ebpxlistrem.count(bp.addr) == 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Non-existing echo breakpoint at address 0x%X was hit",
                bp.addr);
    return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
  }
  DBGEVT dbgevt;
  dbgevt.bp = bp;
  dbgevt.tid = tid;
  dbgevt.type = dbgevt_bpe_hit;
  if (breakpointExists) {
    NaviError remResult = removeBreakpoint(bp, false);
    if (remResult) {
      msglog->log(
          LOG_ALWAYS,
          "Error: Couldn't remove echo breakpoint at address 0x%X (Code %d)",
          bp.addr, remResult);
      return remResult;
    }
    ebpxlist.erase(bp.addr);
  }
  if (correctPc) {
    NaviError setResult = setInstructionPointer(tid, bp.addr);
    if (setResult) {
      msglog->log(
          LOG_ALWAYS,
          "Error: Couldn't set instruction pointer to address 0x%X (Code %d)",
          bp.addr, setResult);
      return setResult;
    }
  }
  RegisterContainer registers;
  NaviError readResult = readRegisters(registers);
  if (readResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read register value (Code %d)",
                readResult);
    return readResult;
  }
  dbgevt.registerString = createRegisterString(registers);
  addDebugEvent(dbgevt);

  // After an echo breakpoint was hit, we offer the option to resume the target
  // process. This speeds up trace mode. Automatic resuming is only possible if
  // we can set an unlimited number of echo breakpoints though. Otherwise we
  // need the help of BinNavi to simulate the trace mode using the limited
  // number of echo breakpoints available on the target platform.
  if (doResume && getDebuggerOptions().breakpointCount == -1) {
    if (breakpointExists && getDebuggerOptions().canTraceCount) {
      CPUADDRESS address;
      doSingleStep(tid, address);
      setBreakpoint(bp.addr, BPX_echo, false);
    }
    unsigned resumeResult = resumeProcess();
    if (resumeResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't resume target thread",
                  resumeResult);
      return resumeResult;
    }
  }
  return NaviErrors::SUCCESS;
}

/**
 * Handles hits of stepping breakpoints.
 *
 * @param bp Information about the breakpoint.
 * @param tid Thread ID of the thread that hit the process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::steppingBreakpointHit(const BREAKPOINT& bp,
                                            unsigned int tid, bool correctPc) {
  if (!hasBreakpoint(bp.addr, BPX_stepping) &&
      sbpxlistrem.count(bp.addr) == 0) {
    msglog->log(LOG_ALWAYS,
                "Error: Non-existing echo breakpoint at address 0x%X was hit",
                bp.addr);
    return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
  }
  DBGEVT dbgevt;
  dbgevt.bp = bp;
  dbgevt.tid = tid;
  dbgevt.type = dbgevt_bps_hit;
  clearBreakpoints(sbpxlist, BPX_stepping);
  if (correctPc) {
    NaviError setResult = setInstructionPointer(tid, bp.addr);
    if (setResult) {
      msglog->log(
          LOG_ALWAYS,
          "Error: Couldn't set instruction pointer to address 0x%X (Code %d)",
          bp.addr, setResult);
      return setResult;
    }
  }
  RegisterContainer registers;
  NaviError readResult = readRegisters(registers);
  if (readResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read register value (Code %d)",
                readResult);
    return readResult;
  }
  dbgevt.registerString = createRegisterString(registers);
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

/**
 * Clears all breakpoints of a given type.
 *
 * @param addresses The breakpoints to remove.
 * @param type The type of the breakpoints to remove.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
// Don't pass "addresses" by reference:
// otherwise clearBreakpoints(sbpxlist, ...) will fail during iteration
// because of concurrent modification of sbpxlist
NaviError BaseSystem::clearBreakpoints(const std::set<CPUADDRESS>& addresses,
                                       BPXType type) {
  std::set<CPUADDRESS> addressesCopy = addresses;
  NaviError ret = NaviErrors::SUCCESS;
  unsigned int counter = 0;
  unsigned int maxRemove = addressesCopy.size();

  for (CPUADDRESS address : addressesCopy) {
    NaviError remResult = removeBreakpoint(getBreakpoint(address, type),
                                           counter != maxRemove - 1);
    counter++;
    if (remResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't remove breakpoint");
      ret = remResult;
    } else {
      removeBreakpointFromList(address, type);
    }
  }
  return ret;
}

/**
 * Searches for a sequence of bytes in memory.
 *
 * @param address The start address of the search.
 * @param size Number of bytes to search through.
 * @param searchString The byte sequence to search for.
 * @param stringLen The length of the byte sequence.
 * @param provider The information provider that is used to store the result.
 **/
NaviError BaseSystem::searchData(CPUADDRESS address, CPUADDRESS size,
                                 const char* searchString,
                                 unsigned int stringLen,
                                 InformationProvider& provider) {
  const unsigned int STEPSIZE = 1000;
  char buffer[STEPSIZE + 1];
  CPUADDRESS currentFrom = address;
  CPUADDRESS currentTo = currentFrom + STEPSIZE;
  msglog->log(LOG_ALL, "Searching data in %d bytes from address %X", size,
              address);
  while (currentFrom + STEPSIZE < address + size) {
    NaviError res = readMemoryData(buffer, currentFrom, STEPSIZE);
    if (res) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't read memory");
      return res;
    }
    unsigned int searchResult =
        search(buffer, STEPSIZE, searchString, stringLen);
    if (searchResult == std::numeric_limits<unsigned int>::max()) {
      currentFrom = currentFrom + (STEPSIZE - stringLen);
      currentTo = currentTo + (STEPSIZE - stringLen);
    } else {
      provider.addAddress(currentFrom + searchResult);
      return NaviErrors::SUCCESS;
    }
  }

  CPUADDRESS diff = address + size - currentFrom;
  unsigned int restMemory = diff;
  if (restMemory >= stringLen) {
    NaviError res = readMemoryData(buffer, currentFrom, diff);
    if (res) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't read memory");
      return res;
    }
    unsigned int searchResult =
        search(buffer, restMemory, searchString, stringLen);
    if (searchResult != std::numeric_limits<unsigned int>::max()) {
      provider.addAddress(currentFrom + searchResult);
      return NaviErrors::SUCCESS;
    }
  }
  msglog->log(LOG_ALWAYS, "Error: Couldn't find search string");
  return NaviErrors::COULDNT_FIND_DATA;
}

/**
 * Determines whether there is a breakpoint of a given type at a given address.
 *
 * @param address The address of the breakpoint.
 * @param type The type of the breakpoint.
 *
 * @return True, if there is a breakpoint of the given type at the given address.
 * False, otherwise.
 **/
bool BaseSystem::hasBreakpoint(CPUADDRESS address, const BPXType type) const {
  if (type == BPX_simple) {
    return bpxlist.count(address) != 0;
  } else if (type == BPX_echo) {
    return ebpxlist.count(address) != 0;
  } else if (type == BPX_stepping) {
    return sbpxlist.count(address) != 0;
  } else {
    msglog->log(LOG_ALWAYS, "Error: Invalid breakpoint type %d", type);
    return false;
  }
}

/**
 * Returns a breakpoint of a given type and a given address.
 *
 * @param address The address of the breakpoint.
 * @param type The type of the breakpoint.
 *
 * @return The breakpoint of the given type at the given address.
 **/
BREAKPOINT BaseSystem::getBreakpoint(CPUADDRESS addr, const BPXType type) const {
  if (!hasBreakpoint(addr, type) && bpxlistrem.count(addr) == 0 &&
      ebpxlistrem.count(addr) == 0 && sbpxlistrem.count(addr) == 0) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Trying to return non-existing breakpoint 0x%X (Type: %d)", addr,
        type);
  }
  // TODO: Does not work for 64bit GDB machines because bp.addr is 32bit there
  BREAKPOINT bp;
  bp.addr = addr;
  bp.bpx_type = type;
  return bp;
}

std::vector<BREAKPOINT> BaseSystem::getBreakpoints(CPUADDRESS address) const {
  std::vector<BREAKPOINT> break_points;
  if (hasBreakpoint(address, BPX_echo)) {
	break_points.emplace_back(getBreakpoint(address, BPX_echo));
  } if (hasBreakpoint(address, BPX_simple)) {
    break_points.emplace_back(getBreakpoint(address, BPX_simple));
  } if (hasBreakpoint(address, BPX_stepping)) {
   break_points.emplace_back(getBreakpoint(address, BPX_stepping));
  }
  return break_points;
}

/**
 * This function must be called by more concrete system classes whenever a
 * breakpoint
 * is hit in the target process.
 *
 * @param bp Information about the breakpoint.
 * @param tid Thread ID of the thread that hit the process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::breakpointHit(CPUADDRESS addr, unsigned int tid,
                                    bool resume_on_echo) {
  if (hasBreakpoint(addr, BPX_echo)) {
    return echoBreakpointHit(getBreakpoint(addr, BPX_echo), tid,
                             true /* correct pc */, resume_on_echo);
  } else if (hasBreakpoint(addr, BPX_stepping)) {
    return steppingBreakpointHit(getBreakpoint(addr, BPX_stepping), tid);
  } else if (hasBreakpoint(addr, BPX_simple)) {
    return simpleBreakpointHit(getBreakpoint(addr, BPX_simple), tid,
                               true /* correct pc */);
  } else if (bpxlistrem.count(addr) != 0) {
    return simpleBreakpointHit(getBreakpoint(addr, BPX_simple), tid,
                               true /* correct pc */);
  } else if (ebpxlistrem.count(addr) != 0) {
    return echoBreakpointHit(getBreakpoint(addr, BPX_echo), tid,
                             true /* correct pc */, resume_on_echo);
  } else if (sbpxlistrem.count(addr) != 0) {
    return steppingBreakpointHit(getBreakpoint(addr, BPX_stepping), tid);
  } else {
    msglog->log(LOG_VERBOSE,
                "Error: No breakpoint found at the specified address: 0x%X",
                addr);
    return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
  }
}

/**
 * Deprecated: use breakpointHit which takes a CPUADDRESS.
 **/
NaviError BaseSystem::breakpointHit(const std::string& addressString,
                                    unsigned int tid) {
  CPUADDRESS addr = zylib::zycon::parseHexString<CPUADDRESS>(addressString);
  return breakpointHit(addr, tid, true);
}

/**
 * Resume the process after we stepped onto a stepping breakpoint.
 * This can happen if the user issues a "step over" which transfers control flow
 * to the next instruction.
 * In this case, we single-step onto the stepping breakpoint and some debugger
 * architectures need special
 * handling for this situation (e.g. ptrace).
 */
NaviError BaseSystem::resumeAfterStepping(unsigned int threadId,
                                          CPUADDRESS /*address*/) {
  unsigned resumeResult = resumeProcess();
  if (resumeResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't resume target thread",
                resumeResult);
    return resumeResult;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Check whether a breakpoint was hit in the given thread.
 */
bool BaseSystem::hasCurrentBreakpoint(unsigned int threadId) const {
  return currentBreakpoints.find(threadId) != currentBreakpoints.end();
}

/**
 * Resumes the thread with the given TID.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::resume(unsigned int tid) {
  if (hasCurrentBreakpoint(tid)) {
    CPUADDRESS breakpoint = currentBreakpoints[tid];
    if (!hasBreakpoint(breakpoint, BPX_simple)) {
      msglog->log(LOG_ALWAYS, "Error: Could not find breakpoint at address %X",
                  breakpoint);
      resumeProcess();
      return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
    }
    BREAKPOINT bp = getBreakpoint(breakpoint, BPX_simple);
    CPUADDRESS address;
    NaviError stepResult = doSingleStep(tid, address);
    if (stepResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't perform a single step");
      return stepResult;
    }
    currentBreakpoints.erase(tid);
    NaviError setResult = setBreakpoint(bp);
    if (setResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't set breakpoint");
      return setResult;
    }
    return resumeAfterStepping(tid, address);
  } else {
    unsigned resumeResult = resumeProcess();
    if (resumeResult) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't resume target thread",
                  resumeResult);
      return resumeResult;
    }
    return NaviErrors::SUCCESS;
  }
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi about a new thread that was created in the target process.
 *
 * @param tid The thread ID of the new thread.
 * @param state The state of the new thread.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::threadCreated(unsigned int tid, ThreadState state) {
  DBGEVT dbgevt;
  dbgevt.tid = tid;
  dbgevt.extra = state;
  dbgevt.type = dbgevt_thread_created;
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi that a thread of the target process was terminated.
 *
 * @param tid The thread ID of the terminated thread.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::threadExit(unsigned int tid) {
  DBGEVT dbgevt;
  dbgevt.tid = tid;
  dbgevt.type = dbgevt_thread_closed;
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

NaviError BaseSystem::processStart(const Module& module, const Thread& thread) {
  DBGEVT dbgevt;
  dbgevt.type = dbgevt_process_start;
  dbgevt.registerString = "<processStart>";
  dbgevt.registerString +=
      "<module name=\"" + module.name + "\" path=\"" + module.path +
      "\" address=\"" + zylib::zycon::toString(module.baseAddress) +
      "\" size=\"" + zylib::zycon::toString(module.size) +
      "\" /><thread threadId=\"" + zylib::zycon::toString(thread.tid) +
      "\" threadState=\"" + zylib::zycon::toString(thread.state) + "\" />";
  dbgevt.registerString += "</processStart>";
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi that a new module was loaded into the process space
 * of the target process.
 *
 * @param module Information about the loaded module.
 * @param threadId The id of the thread which loaded the module.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::moduleLoaded(const Module& module,
                                   unsigned int threadId) {
  DBGEVT dbgevt;
  dbgevt.type = dbgevt_module_loaded;
  dbgevt.registerString =
      "<module name=\"" + module.name + "\" path=\"" + module.path +
      "\" address=\"" + zylib::zycon::toString(module.baseAddress) +
      "\" size=\"" + zylib::zycon::toString(module.size) + "\" threadid=\"" +
      zylib::zycon::toString(threadId) + "\" />";
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi that a module was removed from the target process.
 *
 * @param module Information about the unloaded module.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::moduleUnloaded(const Module& module) {
  pruneBreakpointsByModule(module);
  DBGEVT dbgevt;
  dbgevt.type = dbgevt_module_unloaded;
  dbgevt.registerString =
      "<module name=\"" + module.name + "\" path=\"" + module.path +
      "\" address=\"" + zylib::zycon::toString(module.baseAddress) +
      "\" size=\"" + zylib::zycon::toString(module.size) + "\" />";
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi that the target process was terminated.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
void BaseSystem::processExit() {
  DBGEVT dbgevt;
  dbgevt.type = dbgevt_process_closed;
  addDebugEvent(dbgevt);
}

/**
 * Event handler that generates a debug event that can be sent to BinNavi
 * to notify BinNavi about exceptions in the target process.
 *
 * @param tid The thread ID of the thread that generated the exception.
 * @param address The address where the exception occurred.
 * @param code The exception code.
 * @param name The exception name.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::exceptionRaised(unsigned int tid, CPUADDRESS address,
                                      CPUADDRESS exc_code) {
  DBGEVT dbgevt;
  dbgevt.type = dbgevt_exception;
  dbgevt.registerString = buildExceptionRaisedString(tid, address, exc_code);
  addDebugEvent(dbgevt);
  return NaviErrors::SUCCESS;
}

NaviError BaseSystem::SetExceptionAction(CPUADDRESS exc_code,
                                    DebugExceptionHandlingAction action) {
  exceptionSettings[exc_code] = action;
  return NaviErrors::SUCCESS;
}

DebugExceptionHandlingAction BaseSystem::GetExceptionAction(
    CPUADDRESS exc_code) const {
  auto cit = exceptionSettings.find(exc_code);
  if (cit != exceptionSettings.end()) {
    return cit->second;
  }
  return HALT;
}

/**
 * Builds a string which contains the name of the occurred exception.
 *
 * @param exceptionCode The exception code for which the reply string is to be
 * generated.
 *
 * @return The exception string
 **/
std::string BaseSystem::buildExceptionRaisedString(
    unsigned int threadId, CPUADDRESS address, CPUADDRESS exceptionCode) const {
  return "<exception_raised threadId=\"" + zylib::zycon::toString(threadId) +
         "\" address=\"" + zylib::zycon::toString(address) + "\"" +
         " exceptionCode=\"" + zylib::zycon::toString(exceptionCode) +
         "\" exceptionName=\"" + getExceptionName(exceptionCode) + "\" />";
}

/**
 * Adds a debug event to the list of unprocessed debug events.
 *
 * @param evt The new unprocessed debug event.
 **/
void BaseSystem::addDebugEvent(const DBGEVT& evt) {
  dbgevts.push_back(evt);
}

/**
 * Starts to debug the target process.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::start() {
  if (!path.empty()) {
    msglog->log(LOG_VERBOSE, "Starting new process %s", path.string().c_str());
    return startProcess(path.string().c_str(), commands);
  } else {
    msglog->log(LOG_VERBOSE, "Attaching to existing process");
    return attachToProcess();
  }
}

/**
 * Processes incoming packets.
 *
 * @param p The incoming packet to process.
 * @param provider Information provider where information about the debug event
 * is stored.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 **/
NaviError BaseSystem::processPacket(const Packet* p,
                                    InformationProvider& provider) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  switch (p->hdr.command) {
    case cmd_setbp:
      return processSetBreakpoints(p, provider);
    case cmd_setbpe:
      return processSetEchoBreakpoints(p, provider);
    case cmd_setbps:
      return processSetSteppingBreakpoints(p, provider);
    case cmd_rembp:
      return processRemoveBreakpoints(p, provider);
    case cmd_rembpe:
      return processRemoveEchoBreakpoints(p, provider);
    case cmd_resume:
      return processResume(p, provider);
    case cmd_halt:
      return processHalt(p);
    case cmd_detach:
      return processDetach(p);
    case cmd_registers:
      return processRegisters(p, provider);
    case cmd_read_memory:
      return processMemoryRange(p, provider);
    case cmd_terminate:
      return processTerminate(p);
    case cmd_set_register:
      return processSetRegister(p);
    case cmd_single_step:
      return processSingleStep(p, provider);
    case cmd_validmem:
      return processValidMem(p, provider);
    case cmd_search:
      return processSearch(p, provider);
    case cmd_memmap:
      return processMemmap(p, provider);
    case cmd_list_processes:
      return processListProcesses(p, provider);
    case cmd_cancel_target_selection:
      return NaviErrors::SUCCESS;
    case cmd_select_process:
      return processSelectProcess(p);
    case cmd_list_files:
      return processListFiles(p, provider);
    case cmd_list_files_path:
      return processListFiles(p, provider);
    case cmd_select_file:
      return processSelectFile(p);
    case cmd_suspend_thread:
      return processSuspendThread(p);
    case cmd_resume_thread:
      return processResumeThread(p);
    case cmd_set_active_thread:
      return processSetActiveThread(p);
    case cmd_set_breakpoint_condition:
      return processSetBreakpointCondition(p);
    case cmd_write_memory:
      return processWriteMemory(p);
    case cmd_set_exceptions_options:
      return processSetExceptionSettings(p);
    case cmd_set_debugger_event_settings:
      return processSetDebuggerEventSettings(p);
    default:
      msglog->log(LOG_ALWAYS,
                  "Error: Couldn't process packet (Unknown command %d)",
                  p->hdr.command);
      return NaviErrors::UNKNOWN_COMMAND;
  }
}

/**
 * Determines whether a debug event is ready to be processed.
 *
 * @return True, if an unprocessed debug event exists. False, otherwise.
 **/
bool BaseSystem::isDebugEventAvailable() const { return dbgevts.size() != 0; }

/**
 * Returns the first unprocessed debug event.
 *
 * @param event Debug event structure that is filled with information
 * about the first unprocessed debug event.
 **/
unsigned int BaseSystem::getDebugEvent(DBGEVT* event) const {
  if (dbgevts.size() == 0) {
    msglog->log(LOG_ALWAYS, "Error: No debug event available");
    return 1;
  }
  *event = dbgevts.front();
  return 0;
}

/**
 * Removes the first unprocessed debug event from the list of
 * unprocessed debug events.
 **/
unsigned int BaseSystem::popDebugEvent() {
  if (dbgevts.size() == 0) {
    msglog->log(LOG_ALWAYS, "Error: No debug event available");
    return 1;
  }
  dbgevts.pop_front();
  return 0;
}

/**
 * Process the command used by binNaiv which specifies how the debugger handles
 * certain debug events.
 *
 * @param p The packet which has been sent by BinNavi
 **/
NaviError BaseSystem::processSetDebuggerEventSettings(const Packet* p) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 2) {
    msglog->log(LOG_ALWAYS, "Error: Set Event Settings packet has invalid "
                            "number of arguments - expected 2, found %u",
                p->ints.size());
    return NaviErrors::INVALID_PACKET;
  }
  debuggerEventSettings =
      DebuggerEventSettings(p->ints[0] != 0, p->ints[1] != 0);
  return NaviErrors::SUCCESS;
}

/**
 * Resolve a given exception code to the corresponding name.
 * If the exception code is unknown, an empty name is returned.
 *
 * @return The name of the exception.
 **/
std::string BaseSystem::getExceptionName(CPUADDRESS exceptionCode) const {
  DebugExceptionContainer exceptions = getPlatformExceptions();
  for (const DebugException& ex : exceptions) {
    if (ex.exceptionCode == exceptionCode) return ex.exceptionName;
  }
  return "";
}

/**
 * Enumerate all files in the file system roots and populate the
 * FileListContainer.
 **/
NaviError BaseSystem::readFiles(FileListContainer& fileList) const {
  boost::filesystem::path root;
  getSystemRoot(root);
  return readFiles(fileList, root);
}

/**
 * List all files and sub-directories in the given path.
 *
 * @param parentPath The path whose child elements should be determined.
 * @param files The out parameter which received all the files in parentPath.
 * @param dirs The out parameter which receives all the sub directories of
 * parentPath.
 **/
NaviError BaseSystem::walkDirectory(
    const boost::filesystem::path& parentPath,
    std::vector<boost::filesystem::path>& files,
    std::vector<boost::filesystem::path>& dirs) const {
  try {
    // normalize first; otherwise boost::filesystem::exists() fails on paths
    // like "\\\\"
    boost::filesystem::path p = parentPath;
    // nasty workaround for crappy java path handling
    if (p.string() == "\\\\") {
      p = "/";
    }
    if (exists(p)) {
      std::vector<boost::filesystem::path> v;
      std::copy(boost::filesystem::directory_iterator(p),
                boost::filesystem::directory_iterator(), std::back_inserter(v));
      std::sort(v.begin(), v.end());
      for (const boost::filesystem::path& path : v) {
        if (is_regular_file(path)) {
          files.push_back(path.filename());
        } else if (is_directory(path)) {
          dirs.push_back(path.filename());
        } else {
          msglog->log(LOG_ALL, "Item is neither a file nor a directory: %s\n",
                      path.string().c_str());
        }
      }
    } else {
      msglog->log(LOG_ALWAYS,
                  "Error: Unable to walk non-existing directory: '%s'",
                  p.string().c_str());
      return NaviErrors::COULDNT_GET_FILELIST;
    }
  }
  catch (const boost::filesystem::filesystem_error & exception) {
    msglog->log(LOG_ALWAYS, "Error: %s", exception.what());
    return NaviErrors::COULDNT_GET_FILELIST;
  }
  return NaviErrors::SUCCESS;
}

/**
 * Fill the file list container structure for the given path.
 **/
NaviError BaseSystem::readFiles(FileListContainer& fileList,
                                const boost::filesystem::path& path) const {
  std::vector<boost::filesystem::path> roots;
  if (getFileSystems(roots) != NaviErrors::SUCCESS) {
    msglog->log(LOG_ALWAYS,
                "Error: Unable to determine available file systems.");
    return NaviErrors::COULDNT_GET_FILELIST;
  }
  fileList.setDrives(roots);
  std::vector<boost::filesystem::path> files;
  std::vector<boost::filesystem::path> dirs;
  walkDirectory(path, files, dirs);
  fileList.setDirectories(dirs);
  fileList.setFiles(files);
  fileList.setDirectory(path);
  return NaviErrors::SUCCESS;
}

NaviError BaseSystem::processFakeRemoveBreakpoints(
    const Packet* p,
    std::vector<std::pair<CPUADDRESS, unsigned int> >& result) {
  if (!p) {
    msglog->log(LOG_ALWAYS, "Error: Invalid packet passed to function");
    return NaviErrors::INVALID_PACKET;
  }
  if (p->ints.size() != 1) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %d)",
                p->hdr.command);
    return NaviErrors::MALFORMED_PACKET;
  }
  unsigned int counter = p->ints[0];
  if (p->addresses.size() != counter) {
    msglog->log(LOG_ALWAYS,
                "Error: Malformed packet passed to function (Command %d)",
                p->hdr.command);
    return NaviErrors::MALFORMED_PACKET;
  }
  for (CPUADDRESS address : p->addresses) {
    result.emplace_back(address, NaviErrors::SUCCESS);
  }
  return NaviErrors::SUCCESS;
}

void BaseSystem::pruneByModule(std::set<CPUADDRESS>& bplist,
                               const Module& module) {
  std::set<CPUADDRESS> tmpBpxlist = bplist;
  for(CPUADDRESS bpAddress : tmpBpxlist) {
    if (bpAddress >= module.baseAddress &&
        bpAddress < module.baseAddress + module.size) {
      std::set<CPUADDRESS>::iterator it = bplist.find(bpAddress);
      if (it != bplist.end()) {
        bplist.erase(it);
      }
    }
  }
}

/**
 * Update the internal list of breakpoints for the given module after it was
 * unloaded by the target process.
 **/
void BaseSystem::pruneBreakpointsByModule(const Module& unloadedModule) {
  pruneByModule(bpxlist, unloadedModule);
  pruneByModule(ebpxlist, unloadedModule);
  pruneByModule(sbpxlist, unloadedModule);
}
