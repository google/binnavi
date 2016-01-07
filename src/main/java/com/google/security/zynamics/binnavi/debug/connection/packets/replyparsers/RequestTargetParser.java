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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RequestTargetReply;

/**
 * Parser responsible for parsing replies for Request Target replies. This message is sent by the
 * debug client if no target was specified on the debug client side. When such a message is received
 * it is the job of BinNavi to specify a target file or target process to debug.
 */
public final class RequestTargetParser extends AbstractReplyParser<RequestTargetReply> {
  /**
   * Creates a new Request Target reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public RequestTargetParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_REQUEST_TARGET);
  }

  @Override
  protected RequestTargetReply parseError(final int packetId) {
    // TODO: There is no proper handling of errors on the side of the
    // client yet.

    throw new IllegalStateException("IE01088: Received invalid reply from the debug client");
  }

  @Override
  public RequestTargetReply parseSuccess(final int packetId, final int argumentCount) {
    return new RequestTargetReply(packetId, 0);
  }
}
