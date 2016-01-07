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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.HaltReply;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to Halt requests.
 */
public final class HaltParser extends AbstractReplyParser<HaltReply> {
  /**
   * Creates a new Halt reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public HaltParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_HALTED_SUCCESS);
  }

  @Override
  protected HaltReply parseError(final int packetId) throws IOException {
    return new HaltReply(packetId, 0, parseInteger());
  }

  @Override
  public HaltReply parseSuccess(final int packetId, final int argumentCount) throws IOException {
    final int tid = parseInteger();
    return new HaltReply(packetId, tid, 0);
  }
}
