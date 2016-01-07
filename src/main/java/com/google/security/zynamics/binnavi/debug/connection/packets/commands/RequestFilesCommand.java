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
 * Command class for the request files command. This command should be sent whenever the content of
 * the default directory of the machine where the debug client is located is required.
 */
public final class RequestFilesCommand extends DebugCommand {
  /**
   * Creates a new list files command.
   *
   * @param packetId Packet ID of the command.
   */
  public RequestFilesCommand(final int packetId) {
    super(DebugCommandType.CMD_LIST_FILES, packetId);
  }
}
