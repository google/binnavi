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
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetDebuggerEventSettingsReply;

import java.io.IOException;

/**
 * Parser used to parse replies to the Set Debug Event Settings command.
 */
public class SetDebuggerEventSettingsParser extends
    AbstractReplyParser<SetDebuggerEventSettingsReply> {
  protected SetDebuggerEventSettingsParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_SET_DEBUGGER_EVENT_SETTINGS_SUCC);
  }

  @Override
  protected SetDebuggerEventSettingsReply parseError(final int packetId) throws IOException {
    return new SetDebuggerEventSettingsReply(packetId, parseInteger());
  }

  @Override
  public SetDebuggerEventSettingsReply parseSuccess(final int packetId, final int argumentCount) {
    return new SetDebuggerEventSettingsReply(packetId, 0);
  }
}
