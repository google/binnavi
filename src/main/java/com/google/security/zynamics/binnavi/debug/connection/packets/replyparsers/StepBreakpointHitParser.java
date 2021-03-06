// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;

import java.io.IOException;

/**
 * Parser responsible for parsing Step Breakpoint hit replies.
 */
public final class StepBreakpointHitParser extends AbstractReplyParser<StepBreakpointHitReply> {
  /**
   * Creates a new Step Breakpoint hit reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public StepBreakpointHitParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_BPS_HIT);
  }

  @Override
  protected StepBreakpointHitReply parseError(final int packetId) {
    // TODO: There is no proper handling of errors on the side of the
    // client yet.

    throw new IllegalStateException("IE01089: Received invalid reply from the debug client");
  }

  @Override
  public StepBreakpointHitReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final long tid = parseThreadId();
    final byte[] data = parseData();

    try {
      final RegisterValues registerValues = RegisterValuesParser.parse(data);
      return new StepBreakpointHitReply(packetId, 0, tid, registerValues);
    } catch (final MessageParserException e) {
      return new StepBreakpointHitReply(packetId, PARSER_ERROR, tid, null);
    }
  }
}
