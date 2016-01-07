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
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageAddressArgument;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageRawArgument;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Command class for search commands. This command should be sent to search for a value in the
 * memory of the target process.
 */
public final class SearchCommand extends DebugCommand {
  /**
   * Creates a new search command.
   *
   * @param packetId Packet ID of the command.
   * @param start Start address where the search operation begins.
   * @param size Maximum number of bytes to search through.
   * @param data Binary data to search for.
   *
   * @throws IllegalArgumentException Thrown if the start, size, or data arguments are null.
   */
  public SearchCommand(final int packetId, final IAddress start, final IAddress size,
      final byte[] data) {
    super(DebugCommandType.CMD_SEARCH, packetId);
    Preconditions.checkNotNull(start, "IE01023: Start argument can not be null");
    Preconditions.checkNotNull(size, "IE01024: Size argument can not be null");
    Preconditions.checkNotNull(data, "IE01025: Data argument can not be null");
    addArgument(new DebugMessageAddressArgument(start));
    addArgument(new DebugMessageAddressArgument(size));
    addArgument(new DebugMessageRawArgument(data));
  }
}
