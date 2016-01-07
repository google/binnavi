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

/**
 * Command class for canceling target selection. This debug message should be sent when the user
 * canceled the selection of a target file or a target process.
 */
public final class CancelTargetSelectionCommand extends DebugCommand {
  /**
   * Creates a new cancel target selection command.
   *
   * @param packetId Packet ID of the command.
   */
  public CancelTargetSelectionCommand(final int packetId) {
    super(DebugCommandType.CMD_CANCEL_TARGET_SELECTION, packetId);
  }
}
