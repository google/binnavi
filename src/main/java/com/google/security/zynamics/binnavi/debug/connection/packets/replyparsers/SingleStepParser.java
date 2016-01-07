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
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to Single Step requests.
 */
public final class SingleStepParser extends AbstractReplyParser<SingleStepReply> {
  /**
   * Creates a new Single Step reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public SingleStepParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_SINGLE_STEP_SUCCESS);
  }

  @Override
  protected SingleStepReply parseError(final int packetId) throws IOException {
    return new SingleStepReply(packetId, parseInteger(), 0, null, null);
  }

  @Override
  public SingleStepReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final long tid = parseThreadId();
    final RelocatedAddress address = new RelocatedAddress(parseAddress());
    try {
      final RegisterValues registerValues = RegisterValuesParser.parse(parseData());
      return new SingleStepReply(packetId, 0, tid, address, registerValues);
    } catch (final MessageParserException e) {
      return new SingleStepReply(packetId, PARSER_ERROR, tid, address, null);
    }
  }
}
