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
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageAddressArgument;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageRawArgument;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.conditions.ConditionTreeFlattener;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

/**
 * Command class for the set breakpoint condition command. This command should be sent to set
 * breakpoint conditions of any type in the target process.
 */
public final class SetBreakpointConditionCommand extends DebugCommand {
  /**
   * Creates a new set breakpoint command.
   *
   * @param packetId Packet ID of the command.
   * @param address The address of the breakpoint.
   * @param condition The new condition of the breakpoint. This argument can be null.
   *
   * @throws IllegalArgumentException Thrown if the address or type arguments are null.
   */
  public SetBreakpointConditionCommand(final int packetId, final RelocatedAddress address,
      final Condition condition) {
    super(DebugCommandType.CMD_SET_BREAKPOINT_CONDITION, packetId);
    Preconditions.checkNotNull(address, "IE01027: Address argument can not be null");
    Preconditions.checkNotNull(condition, "IE01028: Condition argument can not be null");
    addArgument(new DebugMessageAddressArgument(address.getAddress()));
    try {
      addArgument(new DebugMessageRawArgument(ConditionTreeFlattener.flatten(condition.getRoot())));
    } catch (final MaybeNullException e) {
      addArgument(new DebugMessageRawArgument(new byte[0]));
    }
  }
}
