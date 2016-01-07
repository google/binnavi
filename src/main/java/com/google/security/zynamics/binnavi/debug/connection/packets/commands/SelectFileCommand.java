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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageRawArgument;

/**
 * Command class for the select file command. This command is sent to the debug client to tell it
 * that the user selected a file he wants to debug.
 */
public final class SelectFileCommand extends DebugCommand {
  /**
   * Creates a select file command.
   *
   * @param packetId Packet ID of the command.
   * @param filename Name of the file to select for debugging.
   *
   * @throws IllegalArgumentException Thrown if the filename argument is null.
   */
  public SelectFileCommand(final int packetId, final String filename) {
    super(DebugCommandType.CMD_SELECT_FILE, packetId);
    Preconditions.checkNotNull(filename, "IE01026: Filename argument can not be null");
    addArgument(new DebugMessageRawArgument(filename.getBytes()));
  }
}
