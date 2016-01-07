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
 * Command class for the request files in a given path command. This command should be sent to
 * request the content of a specified directory of the machine where the debug client resides.
 */
public final class RequestFilesPathCommand extends DebugCommand {
  /**
   * Creates a new request files path command.
   *
   * @param packetId Packet ID of the command.
   * @param path Path whose content should be determined.
   *
   * @throws IllegalArgumentException Thrown if the path argument is null.
   */
  public RequestFilesPathCommand(final int packetId, final String path) {
    super(DebugCommandType.CMD_LIST_FILES_PATH, packetId);
    Preconditions.checkNotNull(path, "IE01010: Path argument can not be null");
    addArgument(new DebugMessageRawArgument(path.getBytes()));
  }
}
