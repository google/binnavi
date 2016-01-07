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

/**
 * Represents the reply to a Halt request sent to the debug client.
 */
public final class HaltReply extends DebuggerReply {
  /**
   * Thread ID of the new active thread.
   */
  private final int activeThreadId;

  /**
   * Creates a new Halt reply object.
   *
   * @param packetId Packet ID of the reply.
   * @param tid Thread ID of the new active thread.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   */
  public HaltReply(final int packetId, final int tid, final int errorCode) {
    super(packetId, errorCode);
    activeThreadId = tid;
  }

  /**
   * Returns the thread ID of the new active thread.
   *
   * @return The thread ID of the new active thread.
   */
  public int getTid() {
    return activeThreadId;
  }
}
