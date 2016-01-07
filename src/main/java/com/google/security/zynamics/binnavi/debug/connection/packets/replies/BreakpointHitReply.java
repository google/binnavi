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

/**
 * Represents the reply that is sent by the debug client whenever a regular breakpoint was hit in
 * the target process.
 */
public final class BreakpointHitReply extends AnyBreakpointHitReply {
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
  public BreakpointHitReply(final int packetId, final int errorCode, final long tid,
      final RegisterValues registerValues) {
    super(packetId, errorCode, tid, registerValues);
  }
}
