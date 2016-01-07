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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.ProcessStartParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;

import java.io.IOException;

public class ProcessStartReplyParser extends AbstractReplyParser<ProcessStartReply> {
  /**
   * Creates a new Process Start reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ProcessStartReplyParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_PROCESS_START);
  }

  @Override
  protected ProcessStartReply parseError(final int packetId) {
    // can not happen since this packet is never the result of a query but is always sent by the
    // debug client directly.
    throw new IllegalStateException("IE00099: Unexpected error in process start reply packet");
  }

  @Override
  public ProcessStartReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    Preconditions.checkArgument(argumentCount == 1,
        "IE00101: Unexpected number of argument while parsing process start packet");
    try {
      return new ProcessStartReply(packetId, 0, ProcessStartParser.parse(parseData()));
    } catch (final MessageParserException exception) {
      CUtilityFunctions.logException(exception);
      throw new IllegalStateException(
          "IE00131: Unexpected error while parsing process start packet");
    }
  }
}
