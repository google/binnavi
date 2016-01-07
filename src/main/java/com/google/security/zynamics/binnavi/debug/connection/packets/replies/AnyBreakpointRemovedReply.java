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
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for any kind of breakpoint removal replies.
 */
public abstract class AnyBreakpointRemovedReply extends DebuggerReply {
  /**
   * The address of the removed breakpoint that was parsed from the packet.
   */
  private final List<Pair<RelocatedAddress, Integer>> addresses;

  /**
   * Creates a new breakpoint removed reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param addresses Addresses the breakpoints were removed from.
   */
  protected AnyBreakpointRemovedReply(final int packetId, final int errorCode,
      final List<Pair<RelocatedAddress, Integer>> addresses) {
    super(packetId, errorCode);
    this.addresses =
        Preconditions.checkNotNull(addresses, "IE01056: Addresses argument can not be null");
  }

  /**
   * Returns the address of the breakpoint that was removed.
   *
   * @return The address of the breakpoint that was removed.
   */
  public List<Pair<RelocatedAddress, Integer>> getAddresses() {
    return new ArrayList<Pair<RelocatedAddress, Integer>>(addresses);
  }
}
