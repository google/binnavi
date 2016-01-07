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

import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

/**
 * Represents the reply sent by the debug client after a Single Step command was sent.
 */
public final class SingleStepReply extends DebuggerReply {
  /**
   * Thread that was single stepped.
   */
  private final long threadId;

  /**
   * New PC value of the stepped thread.
   */
  private final RelocatedAddress address;

  /**
   * All register values of the debugged process.
   */
  private final RegisterValues registerValues;

  /**
   * Creates a new reply object.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply.
   * @param threadId Thread that was single stepped.
   * @param address New PC value of the stepped thread.
   * @param registerValues All register values of the debugged process.
   */
  public SingleStepReply(final int packetId, final int errorCode, final long threadId,
      final RelocatedAddress address, final RegisterValues registerValues) {
    super(packetId, errorCode);

    this.threadId = threadId;
    this.address = address;
    this.registerValues = registerValues;
  }

  /**
   * Returns the new PC value of the stepped thread.
   *
   * @return The new PC value of the stepped thread.
   */
  public RelocatedAddress getAddress() {
    return address;
  }

  /**
   * Returns the new register values of the target process.
   *
   * @return The new register values of the target process.
   */
  public RegisterValues getRegisterValues() {
    return registerValues;
  }

  /**
   * Returns the thread ID of the stepped thread.
   *
   * @return The thread ID of the stepped thread.
   */
  public long getThreadId() {
    return threadId;
  }
}
