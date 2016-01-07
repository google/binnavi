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

import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

/**
 * Represents the reply that is sent by the debug client after a breakpoint removal request was
 * processed.
 */
public final class BreakpointsRemovedReply extends AnyBreakpointRemovedReply {
  /**
   * Creates a new breakpoint removed reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param addresses Addresses the breakpoint were removed from.
   */
  public BreakpointsRemovedReply(final int packetId, final int errorCode,
      final List<Pair<RelocatedAddress, Integer>> addresses) {
    super(packetId, errorCode, addresses);
  }
}
