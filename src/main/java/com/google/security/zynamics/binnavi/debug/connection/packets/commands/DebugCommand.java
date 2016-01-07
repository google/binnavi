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

import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of all command classes that represent the commands that BinNavi can send to the debug
 * client.
 *
 *  Every message contains a command type identifier, a packet ID, and an optional list of arguments
 * that depend on the exact command type of the command.
 */
public class DebugCommand {
  /**
   * Type of the message that is sent to the debug client.
   */
  private final int type;

  /**
   * Packet ID of the message.
   */
  private final int packetId;

  /**
   * Message arguments that are attached to the message.
   */
  private final List<DebugMessageArgument> arguments;

  /**
   * Creates a new debug message.
   *
   * @param type Identifies the message type.
   * @param packetId Packet ID of the message.
   */
  public DebugCommand(final int type, final int packetId) {
    this.arguments = new ArrayList<>();
    this.type = type;
    this.packetId = packetId;
  }

  /**
   * Adds a new argument to the debug message.
   *
   * @param argument The argument to add.
   */
  public final void addArgument(final DebugMessageArgument argument) {
    arguments.add(argument);
  }

  /**
   * Returns the packet ID of the message.
   *
   * @return The packet ID of the message.
   */
  public final int getPacketId() {
    return packetId;
  }

  /**
   * Converts the debug message into a byte array that can be sent to the debug client.
   *
   * @return The byte representation of the message.
   */
  public final byte[] toByteArray() {
    final List<Byte> argchars = new ArrayList<>();

    // Create the packet header.
    argchars.add((byte) ((type >> 24) & 0xFF));
    argchars.add((byte) ((type >> 16) & 0xFF));
    argchars.add((byte) ((type >> 8) & 0xFF));
    argchars.add((byte) ((type) & 0xFF));

    argchars.add((byte) ((packetId >> 24) & 0xFF));
    argchars.add((byte) ((packetId >> 16) & 0xFF));
    argchars.add((byte) ((packetId >> 8) & 0xFF));
    argchars.add((byte) ((packetId) & 0xFF));

    argchars.add((byte) ((arguments.size() >> 24) & 0xFF));
    argchars.add((byte) ((arguments.size() >> 16) & 0xFF));
    argchars.add((byte) ((arguments.size() >> 8) & 0xFF));
    argchars.add((byte) ((arguments.size()) & 0xFF));

    // Add the arguments to the packet.
    for (final DebugMessageArgument argument : arguments) {
      argchars.addAll(argument.getBytes());
    }

    // Convert the packet to a byte array.
    final byte[] ret = new byte[argchars.size()];

    for (int i = 0; i < argchars.size(); i++) {
      ret[i] = argchars.get(i);
    }

    return ret;
  }
}
