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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to Read Memory requests.
 */
public final class ReadMemoryParser extends AbstractReplyParser<ReadMemoryReply> {
  /**
   * Creates a new Read Memory reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ReadMemoryParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_READ_MEMORY_SUCCESS);
  }

  @Override
  protected ReadMemoryReply parseError(final int packetId) throws IOException {
    return new ReadMemoryReply(packetId, parseInteger(), null, null);
  }

  @Override
  public ReadMemoryReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final IAddress address = parseAddress();
    final byte[] data = parseData();
    return new ReadMemoryReply(packetId, 0, address, data);
  }
}
