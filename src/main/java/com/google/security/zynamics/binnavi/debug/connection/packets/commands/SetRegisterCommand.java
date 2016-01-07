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
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Command class for set register commands. This command should be sent when the user wants to
 * modify the value of a register.
 */
public final class SetRegisterCommand extends DebugCommand {
  /**
   * Creates a new set register command.
   *
   * @param packetId Packet ID of the command.
   * @param tid Thread ID of the thread whose register is changed.
   * @param index Index of the register to be changed. This index is defined in the information
   *        string sent from the debug client.
   * @param value New value of the register.
   *
   * @throws IllegalArgumentException Thrown if the register index is negative or the value argument
   *         is null.
   */
  public SetRegisterCommand(final int packetId, final long tid, final int index,
      final IAddress value) {
    super(DebugCommandType.CMD_SET_REGISTER, packetId);
    Preconditions.checkNotNull(value, "IE01029: Value argument can not be null");
    Preconditions.checkArgument(index >= 0, "IE01030: Register index can not be negative");
    addArgument(new DebugMessageIntegerArgument((int) tid));
    addArgument(new DebugMessageIntegerArgument(index));
    addArgument(new DebugMessageAddressArgument(value));
  }
}
