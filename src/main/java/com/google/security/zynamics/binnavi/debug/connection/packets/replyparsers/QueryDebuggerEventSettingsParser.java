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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.QueryDebuggerEventSettingsReply;

/**
 * Parser used to parse the Debugger Event Settings query which is sent from the debug client to
 * request a Debugger Event Settings packet so the debugger knows how to handle certain debug
 * events.
 */
public class QueryDebuggerEventSettingsParser extends
    AbstractReplyParser<QueryDebuggerEventSettingsReply> {
  public QueryDebuggerEventSettingsParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_QUERY_DEBUGGER_EVENT_SETTINGS);
  }

  @Override
  protected QueryDebuggerEventSettingsReply parseError(final int packetId) {
    // Can never happen since this is always the result of a message which is initially sent by the
    // debug client.
    throw new IllegalStateException(
        "IE00153: Received an error packet in CQueryDebuggerEventSettingsReply");
  }

  @Override
  protected QueryDebuggerEventSettingsReply parseSuccess(final int packetId,
      final int argumentCount) {
    return new QueryDebuggerEventSettingsReply(packetId, 0);
  }
}
