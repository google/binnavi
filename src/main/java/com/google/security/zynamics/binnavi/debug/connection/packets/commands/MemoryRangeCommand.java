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
 * Command class of memory range commands. This command should be sent when the memory range
 * surrounding a given address should be determined in the target process.
 */
public final class MemoryRangeCommand extends DebugCommand {
  /**
   * Creates a memory range command.
   *
   * @param packetId Packet ID of the command.
   * @param address Address inside the memory region to validate.
   *
   * @throws IllegalArgumentException Thrown if the address argument is null.
   */
  public MemoryRangeCommand(final int packetId, final IAddress address) {
    super(DebugCommandType.CMD_VALID_MEMORY, packetId);
    Preconditions.checkNotNull(address, "IE00737: Address argument can not be null");
    addArgument(new DebugMessageAddressArgument(address));
  }
}
