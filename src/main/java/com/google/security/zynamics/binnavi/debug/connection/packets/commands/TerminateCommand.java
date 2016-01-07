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

/**
 * Command class that is used for terminate commands. This command is sent to tell the debug client
 * to terminate the target process.
 */
public final class TerminateCommand extends DebugCommand {
  /**
   * Creates a new terminate command.
   *
   * @param packetId Packet ID of the command.
   */
  public TerminateCommand(final int packetId) {
    super(DebugCommandType.CMD_TERMINATE, packetId);
  }
}
