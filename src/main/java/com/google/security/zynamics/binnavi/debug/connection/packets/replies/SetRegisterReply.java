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
 * Represents the reply sent by the debug client when registers of the target process were changed.
 */
public final class SetRegisterReply extends DebuggerReply {
  /**
   * Thread ID whose registers were changed.
   */
  private final long threadId;

  /**
   * Index of the register that was changed.
   */
  private final int registerIndex;

  /**
   * Creates a new reply object.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply.
   * @param tid Thread ID whose registers were changed.
   * @param index Index of the register that was changed.
   */
  public SetRegisterReply(final int packetId, final int errorCode, final long tid,
      final int index) {
    super(packetId, errorCode);
    threadId = tid;
    registerIndex = index;
  }

  /**
   * Returns the index of the register that was modified.
   *
   * @return The index of the register that was modified.
   */
  public int getIndex() {
    return registerIndex;
  }

  /**
   * Returns the thread ID of the thread whose register was modified.
   *
   * @return The thread ID of the thread whose register was modified.
   */
  public long getThread() {
    return threadId;
  }
}
