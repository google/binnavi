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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SuspendThreadReply;

import java.io.IOException;

/**
 * Parser responsible for parsing replies to Resume requests.
 */
public final class SuspendThreadParser extends AbstractReplyParser<SuspendThreadReply> {
  /**
   * Creates a new Resume reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public SuspendThreadParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_SUSPEND_THREAD_SUCC);
  }

  @Override
  protected SuspendThreadReply parseError(final int packetId) throws IOException {
    final int errorCode = parseInteger();
    final long threadId = parseThreadId();
    return new SuspendThreadReply(packetId, errorCode, threadId);
  }

  @Override
  public SuspendThreadReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    return new SuspendThreadReply(packetId, 0, parseInteger());
  }
}
