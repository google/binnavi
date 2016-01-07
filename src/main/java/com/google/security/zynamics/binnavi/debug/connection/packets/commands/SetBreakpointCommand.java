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
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageIntegerArgument;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

import java.util.Set;

/**
 * Command class for the set breakpoint command. This command should be sent to set breakpoints of
 * any type in the target process.
 */
public final class SetBreakpointCommand extends DebugCommand {
  /**
   * Creates a new set breakpoint command.
   *
   * @param packetId Packet ID of the command.
   * @param addresses Addresses where the breakpoints should be set.
   * @param type Type of the new breakpoint.
   *
   * @throws IllegalArgumentException Thrown if the address or type arguments are null.
   */
  public SetBreakpointCommand(final int packetId, final Set<RelocatedAddress> addresses,
      final BreakpointType type) {
    super(getCommandForType(type), packetId);
    Preconditions.checkNotNull(addresses, "IE01094: Addresses argument can not be null");
    Preconditions.checkNotNull(type, "IE00959: Type argument can not be null");
    addArgument(new DebugMessageIntegerArgument(addresses.size()));
    for (final RelocatedAddress address : addresses) {
      addArgument(new DebugMessageAddressArgument(address.getAddress()));
    }
  }

  /**
   * Returns the set breakpoint command for a given breakpoint type.
   *
   * @param type The type of the breakpoint.
   *
   * @return The command that is used to set the breakpoint.
   */
  private static int getCommandForType(final BreakpointType type) {
    switch (type) {
      case REGULAR:
        return DebugCommandType.CMD_SETBP;
      case ECHO:
        return DebugCommandType.CMD_SETBPE;
      case STEP:
        return DebugCommandType.CMD_SETBPS;
      default:
        throw new IllegalArgumentException(
            String.format("IE00734: Unknown breakpoint type '%s'", type));
    }
  }
}
