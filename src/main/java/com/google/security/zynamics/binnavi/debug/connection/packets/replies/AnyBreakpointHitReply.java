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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;

/**
 * Abstract base class for all kinds of breakpoint hit replies.
 */
public abstract class AnyBreakpointHitReply extends DebuggerReply {
  /**
   * Thread ID of the thread that hit the breakpoint.
   */
  private final long threadId;

  /**
   * Values of all registers when the breakpoints was hit.
   */
  private final RegisterValues registerValues;

  /**
   * Creates a new breakpoint hit reply object.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param tid Thread ID of the thread that hit the breakpoint.
   * @param registerValues Values of all registers when the breakpoints was hit. In case of an
   *        error, this argument is null.
   */
  protected AnyBreakpointHitReply(final int packetId, final int errorCode, final long tid,
      final RegisterValues registerValues) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(
          registerValues, "IE01054: Register values argument can not be null");

      boolean foundThread = false;

      for (final ThreadRegisters threadRegisters : registerValues) {
        if (threadRegisters.getTid() == tid) {
          foundThread = true;
        }
      }

      if (!foundThread) {
        throw new IllegalArgumentException("IE01301: Invalid thread ID");
      }
    } else {
      if (registerValues != null) {
        throw new IllegalArgumentException("IE01055: Register values argument must be null");
      }
    }

    this.threadId = tid;
    this.registerValues = registerValues;
  }

  /**
   * Returns the register values of all registers when the breakpoint was hit.
   *
   * @return All current register values.
   */
  public RegisterValues getRegisterValues() {
    return registerValues;
  }

  /**
   * Returns the Thread ID of the thread that hit the breakpoint.
   *
   * @return The Thread ID of the thread that hit the breakpoint.
   */
  public long getThreadId() {
    return threadId;
  }
}
