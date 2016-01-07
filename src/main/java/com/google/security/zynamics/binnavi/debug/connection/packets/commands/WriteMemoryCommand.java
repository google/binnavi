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
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageAddressArgument;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageRawArgument;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Command class for write memory commands. This debug message should be sent when the user wants to
 * change the memory of the target process.
 */
public final class WriteMemoryCommand extends DebugCommand {
  /**
   * Creates a new write memory command.
   *
   * @param packetId Packet ID of the command.
   * @param address Start address of the Write Memory operation.
   * @param data Data to write.
   */
  public WriteMemoryCommand(final int packetId, final IAddress address, final byte[] data) {
    super(DebugCommandType.CMD_WRITE_MEMORY, packetId);
    addArgument(new DebugMessageAddressArgument(address));
    addArgument(new DebugMessageRawArgument(data));
  }
}
