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
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MemoryModuleParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;

import java.io.IOException;

/**
 * Parser responsible for parsing Module Unloaded replies.
 */
public final class ModuleUnloadedParser extends AbstractReplyParser<ModuleUnloadedReply> {
  /**
   * Creates a new Module Unloaded reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ModuleUnloadedParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_MODULE_UNLOADED);
  }

  @Override
  protected ModuleUnloadedReply parseError(final int packetId) {
    // TODO: There is no proper handling of errors on the side of the
    // client yet.

    throw new IllegalStateException("IE01084: Received invalid reply from the debug client");
  }

  @Override
  public ModuleUnloadedReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    try {
      return new ModuleUnloadedReply(packetId, 0, MemoryModuleParser.parseModule(parseData()));
    } catch (final MessageParserException exception) {
      CUtilityFunctions.logException(exception);
      return new ModuleUnloadedReply(0, PARSER_ERROR, null);
    }
  }
}
