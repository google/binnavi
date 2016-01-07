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
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Command class for memory read operations. This command is used to ask the debug client to read
 * and send a given memory range from the memory of the target process.
 */
public final class RequestMemoryCommand extends DebugCommand {
  /**
   * Creates a new memory request command.
   *
   * @param packetId Packet ID of the command.
   * @param address Start address of the memory range to be read.
   * @param length Length of the memory range in bytes.
   *
   * @throws IllegalArgumentException Thrown if the address or length arguments are null.
   */
  public RequestMemoryCommand(final int packetId, final IAddress address, final IAddress length) {
    super(DebugCommandType.CMD_READ_MEMORY, packetId);
    Preconditions.checkNotNull(address, "IE01015: Address argument can not be null");
    Preconditions.checkNotNull(length, "IE01021: Length argument can not be null");
    addArgument(new DebugMessageAddressArgument(address));
    addArgument(new DebugMessageAddressArgument(length));
  }
}
