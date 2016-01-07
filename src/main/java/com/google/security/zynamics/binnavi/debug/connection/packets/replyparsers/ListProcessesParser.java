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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListProcessesReply;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to List Processes requests.
 */
public final class ListProcessesParser extends AbstractReplyParser<ListProcessesReply> {
  /**
   * Creates a new List Processes reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ListProcessesParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_LIST_PROCESSES_SUCCESS);
  }

  @Override
  protected ListProcessesReply parseError(final int packetId) throws IOException {
    return new ListProcessesReply(packetId, parseInteger(), null);
  }

  @Override
  public ListProcessesReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    try {
      final ProcessList processes = ProcessList.parse(parseData());
      return new ListProcessesReply(packetId, 0, processes);
    } catch (final Exception exception) {
      CUtilityFunctions.logException(exception);

      return new ListProcessesReply(packetId, PARSER_ERROR, null);
    }
  }
}
