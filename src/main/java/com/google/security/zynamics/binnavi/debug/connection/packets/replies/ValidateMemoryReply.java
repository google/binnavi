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
 * Reply sent by the debug client in response to Validate Memory requests.
 */
public final class ValidateMemoryReply extends DebuggerReply {
  /**
   * Start address of the discovered memory range.
   */
  private final IAddress memoryRangeStart;

  /**
   * End address of the discovered memory range.
   */
  private final IAddress memoryRangeEnd;

  /**
   * Creates a new Validate Memory reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param start Start address of the discovered memory range.
   * @param end End address of the discovered memory range.
   */
  public ValidateMemoryReply(
      final int packetId, final int errorCode, final IAddress start, final IAddress end) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(start, "IE01057: Start argument can not be null");

      Preconditions.checkNotNull(end, "IE01077: End argument can not be null");
    } else {
      if (start != null) {
        throw new IllegalArgumentException("IE01078: Start argument must be null");
      }

      if (end != null) {
        throw new IllegalArgumentException("IE01079: End argument must be null");
      }
    }

    memoryRangeStart = start;
    memoryRangeEnd = end;
  }

  /**
   * Returns the end address of the discovered range. In case of errors this method returns null.
   *
   * @return The end address of the discovered range or null.
   */
  public IAddress getEnd() {
    return memoryRangeEnd;
  }

  /**
   * Returns the start address of the discovered range. In case of errors this method returns null.
   *
   * @return The start address of the discovered range or null.
   */
  public IAddress getStart() {
    return memoryRangeStart;
  }
}
