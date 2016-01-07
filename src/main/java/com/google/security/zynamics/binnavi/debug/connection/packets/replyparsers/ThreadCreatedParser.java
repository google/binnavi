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
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadCreatedReply;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

import java.io.IOException;

/**
 * Parser responsible for parsing Thread Created replies.
 */
public final class ThreadCreatedParser extends AbstractReplyParser<ThreadCreatedReply> {
  /**
   * Creates a new Thread Created reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public ThreadCreatedParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_THREAD_CREATED);
  }

  /**
   * Converts the numerical value of a thread state into the proper enumeration value.
   *
   * @param value The numerical value of the thread state.
   *
   * @return The enumeration value of the thread state.
   */
  private ThreadState convertThreadState(final int value) {
    switch (value) {
      case 0:
        return ThreadState.RUNNING;
      case 1:
        return ThreadState.SUSPENDED;
      default:
        throw new IllegalArgumentException(String.format(
            "Received invalid thread state %d", value));
    }
  }

  @Override
  protected ThreadCreatedReply parseError(final int packetId) {
    // TODO: There is no proper handling of errors on the side of the
    // client yet.
    throw new IllegalStateException("IE01092: Received invalid reply from the debug client");
  }

  @Override
  public ThreadCreatedReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final long thread_id = parseThreadId();
    final ThreadState threadState = convertThreadState(parseInteger());
    return new ThreadCreatedReply(packetId, 0, thread_id, threadState);
  }
}
