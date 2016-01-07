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
 * Command class for remove breakpoint commands. This debug command can be sent to remove
 * breakpoints of any type from the target process.
 */
public final class RemoveBreakpointsCommand extends DebugCommand {
  /**
   * Creates a new remove breakpoint command.
   *
   * @param packetId Packet ID of the command.
   * @param addresses Addresses from which the breakpoints are removed.
   * @param type Type of the breakpoint to remove.
   *
   * @throws IllegalArgumentException Thrown if the address or type arguments are null.
   */
  public RemoveBreakpointsCommand(final int packetId, final Set<RelocatedAddress> addresses,
      final BreakpointType type) {
    super(getCommandForType(type), packetId);
    Preconditions.checkNotNull(addresses, "IE00824: Address argument can not be null");
    addArgument(new DebugMessageIntegerArgument(addresses.size()));
    for (final RelocatedAddress address : addresses) {
      addArgument(new DebugMessageAddressArgument(address.getAddress()));
    }
  }

  /**
   * Returns the remove breakpoint command for a given breakpoint type.
   *
   * @param type The type of the breakpoint.
   *
   * @return The command that is used to remove the breakpoint.
   *
   * @throws IllegalArgumentException Thrown if the type argument is null.
   */
  private static int getCommandForType(final BreakpointType type) {
    Preconditions.checkNotNull(type, "IE01009: Type argument can not be null");
    switch (type) {
      case REGULAR:
        return DebugCommandType.CMD_REMBP;
      case ECHO:
        return DebugCommandType.CMD_REMBPE;
      case STEP:
        return DebugCommandType.CMD_REMBPS;
      default:
        throw new IllegalStateException(
            String.format("IE00741: Unknown breakpoint type '%s'", type));
    }
  }
}
