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
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RegistersReply;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to Read Registers requests.
 */
public final class RegistersParser extends AbstractReplyParser<RegistersReply> {
  /**
   * Creates a new Read Registers reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public RegistersParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_REGISTERS_SUCCESS);
  }

  @Override
  protected RegistersReply parseError(final int packetId) throws IOException {
    return new RegistersReply(packetId, parseInteger(), null);
  }

  @Override
  public RegistersReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final byte[] data = parseData();
    try {
      final RegisterValues registerValues = RegisterValuesParser.parse(data);
      return new RegistersReply(packetId, 0, registerValues);
    } catch (final MessageParserException exception) {
      CUtilityFunctions.logException(exception);
      return new RegistersReply(packetId, PARSER_ERROR, null);
    }
  }
}
