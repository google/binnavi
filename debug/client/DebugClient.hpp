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

#ifndef DEBUGCLIENT_H
#define DEBUGCLIENT_H

#include <string>
#include <vector>
#include <cstdlib>

#include <zycon/src/zycon.h>

#include "errors.hpp"
#include "defs.hpp"
#include "logger.hpp"
#include "InformationProvider.hpp"
#include "DebuggerOptions.hpp"
#include "BaseConnection.hpp"
#include "BaseSystem.hpp"

// Creates the debugger options XML string that is sent to BinNavi
std::string getDebuggerOptionsString(const DebuggerOptions& options);

// Creates the register values string that is sent to BinNavi
std::string getRegisterString(
    const std::vector<RegisterDescription>& registers);

// Creates the thread information string that is sent to BinNavi
std::string getThreadIdString(const std::vector<Thread>& tids);

// Creates the address size string that is sent to BinNavi
std::string getAddressSizeString(unsigned int size);

// Creates the information string that is sent to BinNavi
std::string getInformationString(
    const DebuggerOptions& options, unsigned int addressSize,
    const std::vector<RegisterDescription>& registerNames);

// DebugClient objects are used to communicate with BinNavi.
class DebugClient {
 private:

  // Connection policy that is used to connect the debug client with BinNavi.
  BaseConnection* binnavi_connection;

  // System policy that implements the platform dependent debugging code.
  BaseSystem* process;

  // Generates and sends the target information string to BinNavi.
  NaviError sendInformationString();

  // Retrieves the debugger event settings packet from BinNavi.
  NaviError handleEventSettingsPacket() const;

 public:

  // Creates a new debug client object
  // Takes ownership of the system and process instances. This instance has the
  // same lifetime as the containing DebugClient instance.
  // TODO(mkow): change pointers to shared_ptr
  DebugClient(BaseConnection *binnavi_connection, BaseSystem *process);

  BaseSystem* getSystemPolicy() const {
    return process;
  }

  // Initializes the connection between the debug client and BinNavi.
  NaviError initializeConnection();

  // Closes the connection to BinNavi.
  NaviError closeConnection();

  // Waits for a connection from BinNavi.
  NaviError waitForConnection();

  // Tries to start/attach to a process
  NaviError attachToProcess();

  // Sends BinNavi a message that a target was not specified when the debug
  NaviError requestTarget();

  // Processes incoming packets from BinNavi.
  NaviError processPackets();

  virtual ~DebugClient() {
    delete binnavi_connection;
    delete process;
  }
};

#endif
