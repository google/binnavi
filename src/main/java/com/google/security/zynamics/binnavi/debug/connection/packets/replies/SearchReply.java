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
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Represents the reply that is sent by the debug client in response to Search Memory operations.
 */
public final class SearchReply extends DebuggerReply {
  /**
   * The address where the requested search string was found.
   */
  private final IAddress address;

  /**
   * Creates a new Search reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param address The address where the requested search string was found. In case of errors, this
   *        argument must be null.
   */
  public SearchReply(final int packetId, final int errorCode, final IAddress address) {
    super(packetId, errorCode);
    if (success()) {
      Preconditions.checkNotNull(address, "IE01072: Address argument can not be null");
    } else {
      if (address != null) {
        throw new IllegalArgumentException("IE01073: Address argument must be null");
      }
    }
    this.address = address;
  }

  /**
   * Returns the address where the requested search string was found. In case of errors this method
   * returns null.
   *
   * @return The address where the requested search string was found.
   */
  public IAddress getAddress() {
    return address;
  }
}
