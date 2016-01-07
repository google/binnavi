/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.debug.connection.packets.commands;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageIntegerArgument;

/**
 * Command class for the select process command. This command is sent to the debug client to tell it
 * that the user selected a process he wants to debug.
 */
public final class SelectProcessCommand extends DebugCommand {
  /**
   * Creates a new select process command.
   *
   * @param packetId Packet ID of the command.
   * @param pid Process ID of the process to select for debugging.
   */
  public SelectProcessCommand(final int packetId, final int pid) {
    super(DebugCommandType.CMD_SELECT_PROCESS, packetId);
    addArgument(new DebugMessageIntegerArgument(pid));
  }
}
