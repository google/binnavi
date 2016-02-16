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

#include "DebugClient.hpp"

#include "logger.hpp"

/**
 * Creates an XML string that describes the options supported by the debugger.
 *
 * @param options The options structure that describes the options supported
 * by the debugger.
 *
 * @return The XML string that describes the options.
 */
std::string getDebuggerOptionsString(const DebuggerOptions& options) {
  std::string optionsString = "<options>";

  optionsString += "<option name=\"attach\" value=\""
      + zylib::zycon::toBoolString(options.canAttach) + "\" />";
  optionsString += "<option name=\"detach\" value=\""
      + zylib::zycon::toBoolString(options.canDetach) + "\" />";
  optionsString += "<option name=\"terminate\" value=\""
      + zylib::zycon::toBoolString(options.canTerminate) + "\" />";
  optionsString += "<option name=\"memmap\" value=\""
      + zylib::zycon::toBoolString(options.canMemmap) + "\" />";
  optionsString += "<option name=\"multithread\" value=\""
      + zylib::zycon::toBoolString(options.canMultithread) + "\" />";
  optionsString += "<option name=\"validmemory\" value=\""
      + zylib::zycon::toBoolString(options.canValidMemory) + "\" />";
  optionsString += "<option name=\"softwareBreakpoints\" value=\""
      + zylib::zycon::toBoolString(options.canSoftwareBreakpoint) + "\" />";
  optionsString += "<option name=\"halt\" value=\""
      + zylib::zycon::toBoolString(options.canHalt) + "\" />";
  optionsString += "<option name=\"haltBeforeCommunicating\" value=\""
      + zylib::zycon::toBoolString(options.haltBeforeCommunicating) + "\" />";
  // TODO(mkow): Unless issue b/17100483 is fixed, we need this condition
  // otherwise BinNavi will crash
  if (options.breakpointCount != -1) {
    optionsString += "<option name=\"breakpointCount\" value=\""
       + zylib::zycon::toString(options.breakpointCount) + "\" />";
  }
  optionsString += "<option name=\"hasStack\" value=\""
      + zylib::zycon::toBoolString(options.hasStack) + "\" />";
  optionsString += "<option name=\"pageSize\" value=\""
     + zylib::zycon::toString(options.pageSize) + "\" />";
  optionsString += "<option name=\"canBreakOnModuleLoad\" value=\""
      + zylib::zycon::toBoolString(options.canBreakOnModuleLoad) + "\" />";
  optionsString += "<option name=\"canBreakOnModuleUnload\" value=\""
     + zylib::zycon::toBoolString(options.canBreakOnModuleUnload) + "\" />";
  optionsString += "<option name=\"canTraceCount\" value=\""
      + zylib::zycon::toBoolString(options.canTraceCount) + "\" />";
  // build exception list
  for (const DebugException& ex : options.exceptions) {
    optionsString += "<option name=\"exception\" exceptionName=\""
        + ex.exceptionName + "\" exceptionCode=\""
        + zylib::zycon::toString(ex.exceptionCode) + +"\" handlingAction=\""
        + zylib::zycon::toString(ex.handlingAction) + "\" />";
  }

  optionsString += "</options>";
  return optionsString;
}

/**
 * Creates a register description from a list of register description objects.
 *
 * @param registers The list of register description objects.
 *
 * @return The generated register description string.
 */
std::string getRegisterString(
    const std::vector<RegisterDescription>& registers) {
  std::string regString = "<registers>";

  for (const RegisterDescription& reg : registers) {
    regString += "<register ";
    regString += "name=\"" + reg.name + "\" ";
    regString += "size=\"" + zylib::zycon::toString(reg.size) + "\" ";
    regString += "editable=\"" + zylib::zycon::toString(reg.editable) + "\"";
    regString += "/>";
  }

  regString += "</registers>";
  msglog->log(LOG_ALL, "Register String created: %s", regString.c_str());
  return regString;
}

/**
 * Creates an XML string that describes the threads of the target process.
 *
 * @param tids The threads of the target process.
 *
 * @return An XML string that describes the threads of the target process.
 */
std::string getThreadIdString(unsigned int activeThread,
                              const std::vector<Thread>& tids) {
  const char* STATE[] = { "Running", "Suspended" };
  std::string tidString = "<threads>";

  for (const Thread& thread : tids) {
    tidString += "<thread tid=\"" + zylib::zycon::toString(thread.tid)
       + "\" state=\"" + STATE[thread.state] + "\"";

    if (thread.tid == activeThread) {
      tidString += " active=\"" + zylib::zycon::toBoolString(true) + "\" ";
    }

    tidString += "/>";
  }

  tidString += "</threads>";
  msglog->log(LOG_ALL, "Thread String created: %s", tidString.c_str());
  return tidString;
}

std::string getModulesString(const std::vector<Module>& modules) {
  std::string modulesString = "<modules>";

  for (const Module& module : modules) {
    modulesString += "<module name=\"" + module.name + "\" path=\"" + module.path
        + "\" address=\"" + zylib::zycon::toString(module.baseAddress)
        + "\" size=\"" + zylib::zycon::toString(module.size) + "\" />";
  }

  modulesString += "</modules>";
  msglog->log(LOG_ALL, "Modules String created: %s", modulesString.c_str());
  return modulesString;
}

/**
 * Creates an XML string that describes the architecture size of the target CPU.
 *
 * @param tids The size of the target memory.
 *
 * @return An XML string that describes the target architecture size.
 */
std::string getAddressSizeString(unsigned int size) {
  std::string addressSizeString = "<size>" + zylib::zycon::toString(size)
      + "</size>";
  msglog->log(LOG_ALL, "Address Size String created: %s",
              addressSizeString.c_str());
  return addressSizeString;
}

/**
 * Creates an XML string that gives information about the target process and the
 * debug client.
 *
 * @param options The debugger options that are available in the debug client.
 * @param addressSize The size of the architecture.
 * @param registerNames The names of the registers of the target platform.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
std::string getInformationString(
    const DebuggerOptions& options, unsigned int addressSize,
    const std::vector<RegisterDescription>& registerNames) {
  std::string infoString = "<info>";
  infoString += getDebuggerOptionsString(options);
  infoString += getRegisterString(registerNames);
  infoString += getAddressSizeString(addressSize);
  infoString += "</info>";

  msglog->log(LOG_ALL, "Created Info String %s", infoString.c_str());
  return infoString;
}

/**
* Generates and sends the target information string to BinNavi.
*
* @return A NaviError code that describes whether the operation was successful
* or not.
*/
NaviError DebugClient::sendInformationString() {
  std::vector<RegisterDescription> registerNames = process->getRegisterNames();

  DebuggerOptions options = process->getDebuggerOptions();
  unsigned int addressSize = process->getAddressSize();
  std::string infoString = getInformationString(options, addressSize,
                                                registerNames);
  unsigned int infoResult = binnavi_connection->sendInfoString(infoString);

  if (infoResult) {
    msglog->log(LOG_ALWAYS,
                "Error: Couldn't send target information to BinNavi");
    return infoResult;
  }
  msglog->log(LOG_VERBOSE,
              "Successfully sent the Information String to BinNavi");
  return NaviErrors::SUCCESS;
}

/**
 * Retrieves and handles the debugger event settings packet from BinNavi. This
 * needs to be done before the debuggee is started, otherwise the debugger is
 * unable to react to certain events accordingly.
 */
NaviError DebugClient::handleEventSettingsPacket() const {
  Packet settingsPacket;
  NaviError error = binnavi_connection->readPacket(&settingsPacket);

  if (error) {
    msglog->log(LOG_ALWAYS,
                "Failed to retrieve debugger settings packet from BinNavi");
    return error;
  }
  if (settingsPacket.hdr.command != cmd_set_debugger_event_settings) {
    msglog->log(LOG_ALWAYS, "Expected Debugger Event Settings packet, but "
                "received packet with command id: %d\n",
                settingsPacket.hdr.command);
    return NaviErrors::INVALID_PACKET;
  }

  InformationProvider provider;
  return process->processPacket(&settingsPacket, provider);
}

/**
 * Creates a new debug client object
 * The argument ConnectionPolicy specifies the kind of connection
 * that is used to communicate between BinNavi and the debug client.
 * The constructor SystemPolicy specifies the platform on which
 * the target process is executed.
 */
DebugClient::DebugClient(BaseConnection* binnavi_connection,
                         BaseSystem* process)
    : binnavi_connection(binnavi_connection), process(process) {}

/**
 * Initializes the connection between the debug client and BinNavi.
 *
 * @return A NaviError code that describes whether the operation was successful
 *   or not.
 */
NaviError DebugClient::initializeConnection() {
  binnavi_connection->printConnectionInfo();
  return binnavi_connection->initializeConnection();
}

/**
* Closes the connection to BinNavi.
*
* @return A NaviError code that describes whether the operation was successful
* or not.
*/
NaviError DebugClient::closeConnection() {
  return binnavi_connection->closeConnection();
}

/**
* Waits for a connection from BinNavi.
*
* @return A NaviError code that describes whether the operation was successful
or not.
*/
NaviError DebugClient::waitForConnection() {
  NaviError connectionResult = binnavi_connection->waitForConnection();

  if (!connectionResult) {
    connectionResult = binnavi_connection->sendAuthentication();
  }

  return connectionResult;
}

/**
* Tries to start/attach to a process
*
* @return A NaviError code that describes whether the operation was successful
or not.
*/
NaviError DebugClient::attachToProcess() {
  // send debugger_event_settings_reply
  binnavi_connection->sendSimpleReply(resp_query_debugger_event_settings, 0);
  handleEventSettingsPacket();

  NaviError infoResult = sendInformationString();
  if (infoResult) {
    msglog->log(LOG_ALWAYS,
                "Error: Couldn't send message information string (Code %d)",
                infoResult);
    return infoResult;
  }

  // Try to attach to the process.
  NaviError attachResult = process->start();
  // Send the appropriate message to BinNavi
  commandtype_t reply =
    attachResult ? resp_attach_error : resp_attach_success;
  NaviError sendResult = binnavi_connection->sendSimpleReply(reply, 0);

  if (sendResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't send message (Code %d)",
                sendResult);
    return sendResult;
  }
  if (attachResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't attach to target process");
    return attachResult;
  }

  // Attach operation was successful
  msglog->log(LOG_VERBOSE, "Attaching to the target process succeeded");

  return NaviErrors::SUCCESS;
}

/**
* Sends BinNavi a message that a target was not specified when the debug
* client was
* started and therefore must be selected manually now.
*/
NaviError DebugClient::requestTarget() {
  NaviError sendResult =
      binnavi_connection->sendSimpleReply(resp_request_target, 0);
  if (sendResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't send message (Code %d)",
                sendResult);
  }
  return sendResult;
}

/**
* Processes incoming packets from BinNavi.
*/
NaviError DebugClient::processPackets() {
  bool loop = true;
  NaviError ret = NaviErrors::SUCCESS;

  do {
    // There are two important race conditions here that must be handled.
    //
    // Race Condition I:
    //   0. Precondition: The target process is running (not suspended)
    //   1. RemoveBreakpoint at offset X packet has arrived =>
    // binnavi_connection->hasData() == true
    //   2. The breakpoint at offset X is hit while processing the packet
    //   3. The breakpoint handler is invoked after the packet is processed (=
    // the breakpoint was removed)
    //   4. The breakpoint handler believes that the breakpoint wasn't set by
    // the debug client => Error
    //
    // Race Condition II:
    //   0. Precondition: The target process is running (not suspended)
    //   1. Breakpoint at offset X is hit
    //   2. RemoveBreakpoint at offset X packet arrives
    //   3. Bad things might happen; to be investigated

    // TODO: All other debug commands must be checked for race conditions
    // All commands which modify the process (have side effects) might lead
    // to race conditions.

    if (binnavi_connection->hasData()) {
      // Packet from BinNavi arrived
      Packet* p = new Packet;
      NaviError error = binnavi_connection->readPacket(p);
      msglog->log(LOG_VERBOSE, "Processing command %s",
                  commandToString(p->hdr.command));

      if (!error) {
        msglog->log(LOG_VERBOSE, "Received packet with command %s from BinNavi",
                    commandToString(p->hdr.command));
        InformationProvider provider;
        // trying to work on the debug events which just came in before we
        // introduce any other things.
        process->readDebugEvents();
        NaviError procResult = process->processPacket(p, provider);
        if (procResult) {
          msglog->log(LOG_VERBOSE, "Couldn't process packet (Code %d)",
                      procResult);
          binnavi_connection->sendErrorReply(p, procResult);
        } else {
          msglog->log(LOG_VERBOSE,
                      "Packet successfully processed. Sending "
                      "reply to BinNavi now. ...");
          binnavi_connection->sendSuccessReply(p, provider);
        }

        if (p->hdr.command == cmd_terminate) {
          delete p;
          return NaviErrors::SUCCESS;
        } else if (p->hdr.command == cmd_detach) {
          delete p;
          return NaviErrors::SUCCESS;
        } else if (p->hdr.command == cmd_cancel_target_selection) {
          msglog->log(LOG_VERBOSE, "Canceling target selection");
          delete p;
          return NaviErrors::SUCCESS;
        } else if (p->hdr.command == cmd_select_process ||
                   p->hdr.command == cmd_select_file) {
          msglog->log(LOG_VERBOSE, "Selecting target process");

          NaviError attachResult = attachToProcess();
          if (attachResult) {
            return attachResult;
          }
        }
      } else if (error == NaviErrors::CONNECTION_CLOSED ||
                 error == NaviErrors::CONNECTION_ERROR) {
        ret = error;
        loop = false;
      } else {
        msglog->log(LOG_ALWAYS, "Error: Reading packet failed (%s)",
                    commandToString(p->hdr.command));
      }
      delete p;
    } else if (process->hasTarget()) {
      // No packets from BinNavi, process debug events.
      if (!process->isDebugEventAvailable()) {
        // No debug events in queue, try to read them and insert into event
        // queue.
        if (process->readDebugEvents() == NaviErrors::CONNECTION_CLOSED) {
          msglog->log(LOG_ALWAYS,
                      "Connection to debuggee closed, debugging finished.");
          return NaviErrors::SUCCESS;
        }
      }
      while (process->isDebugEventAvailable()) {
        DBGEVT* evt = new DBGEVT;

        NaviError dbgResult = process->getDebugEvent(evt);

        if (dbgResult) {
          msglog->log(LOG_ALWAYS,
                      "Error: Couldn't retrieve debug event (Code %d)",
                      dbgResult);
        }

        msglog->log(LOG_VERBOSE, "Sending debug event to BinNavi (%s)",
                    debugEventToString(evt->type));

        NaviError sendResult = binnavi_connection->sendDebugEvent(evt);

        if (sendResult) {
          msglog->log(LOG_ALWAYS,
                      "Error: Couldn't send debug event (Code %d)",
                      sendResult);
        } else {
          // If BinNavi was notified of the debug event, the debug
          // event can now be removed from the list of debug events.
          unsigned int popResult = process->popDebugEvent();

          if (popResult) {
            msglog->log(LOG_ALWAYS,
                        "Error: Couldn't remove debug event (Code %d)",
                        popResult);
          }

          if (evt->type == dbgevt_process_closed) {
            delete evt;

            return NaviErrors::SUCCESS;
          }
        }
        delete evt;
      }
// facilitates debugging - less noise
#ifdef _DEBUG
      continue;
#endif

      InformationProvider provider;
      Packet* p = new Packet();
      p->hdr.command = cmd_read_memory;
      NaviError procResult = process->reloadMemory(p, provider);

      if (procResult) {
        if (procResult != NaviErrors::NOTHING_TO_REFRESH) {
          msglog->log(LOG_VERBOSE, "Could not update memory (Code %d)",
                      procResult);
        }
      } else {
        msglog->log(LOG_VERBOSE,
                    "Packet successfully processed. Sending "
                    "reply to BinNavi now. ...");
        binnavi_connection->sendSuccessReply(p, provider);
      }
      delete p;
    }
  } while (loop);

  return ret;
}
