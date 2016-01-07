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
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageLongArgument;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;

import java.util.Collection;

/**
 * The command class used to inform the debugger about exceptions which need special handling.
 */
public class SetExceptionSettingsCommand extends DebugCommand {
  /**
   * Creates the new command class to specify exception settings. The command consists of 2-tuples
   * for each exception: <code, action> @param packetId Packet Id of the command. * @param *
   * exceptions The list of exceptions whose settings are to be transmitted
   */
  public SetExceptionSettingsCommand(final int packetId,
      final Collection<DebuggerException> exceptions) {
    super(DebugCommandType.CMD_SET_EXCEPTIONS, packetId);
    Preconditions.checkNotNull(exceptions, "IE00055: Exceptions argument can not be null");
    for (final DebuggerException pe : exceptions) {
      addArgument(new DebugMessageLongArgument(pe.getExceptionCode()));
      addArgument(new DebugMessageIntegerArgument(pe.getExceptionAction().getValue()));
    }
  }
}
