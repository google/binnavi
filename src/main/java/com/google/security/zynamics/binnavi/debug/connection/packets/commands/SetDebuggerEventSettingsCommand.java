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
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageIntegerArgument;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;

/**
 * The command class used to tell the debugger how to respond to the certain debug events.
 */
public class SetDebuggerEventSettingsCommand extends DebugCommand {

  /**
   * Creates a new instance of the debug event settings command.
   *
   * @param packetId Packet Id of the command.
   * @param eventSettings The debugger event setting to be sent to the debugger.
   */
  public SetDebuggerEventSettingsCommand(final int packetId,
      final DebuggerEventSettings eventSettings) {
    super(DebugCommandType.CMD_SET_DEBUGGER_EVENT_SETTINGS, packetId);
    Preconditions.checkNotNull(eventSettings, "IE00054: Event settings arugment can not be null");
    // Warning: NEVER change the order of the parameters here, since this would break the protocol
    // also see case 2615
    addArgument(new DebugMessageIntegerArgument(eventSettings.getBreakOnDllLoad() ? 1 : 0));
    addArgument(new DebugMessageIntegerArgument(eventSettings.getBreakOnDllUnload() ? 1 : 0));
  }
}
