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
package com.google.security.zynamics.binnavi.debug.connection.packets.replies;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

/**
 * Reply that is sent to BinNavi whenever a new thread is created in the target process.
 */
public final class ThreadCreatedReply extends DebuggerReply {
  /**
   * The thread ID of the new thread.
   */
  private final long threadId;

  /**
   * The state of the new thread.
   */
  private final ThreadState threadState;

  /**
   * Creates a new Thread Created reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param tid Thread ID of the new thread.
   * @param threadState The thread state of the new thread. This argument must be null in case of
   *        errors.
   */
  public ThreadCreatedReply(final int packetId, final int errorCode, final long tid,
      final ThreadState threadState) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(threadState, "IE01075: Thread state argument can not be null");
    } else {
      if (threadState != null) {
        throw new IllegalArgumentException("IE01076: Thread state argument must be null");
      }
    }

    threadId = tid;
    this.threadState = threadState;
  }

  /**
   * Returns the thread ID of the new thread.
   *
   * @return The thread ID of the new thread.
   */
  public long getThreadId() {
    return threadId;
  }

  /**
   * Returns the thread state of the new thread. In case of errors this method returns null.
   *
   * @return The thread state of the new thread or null.
   */
  public ThreadState getThreadState() {
    return threadState;
  }
}
