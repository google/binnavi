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
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;

/**
 * Represents the reply sent by the debug client after a Memory Map request.
 */
public final class MemoryMapReply extends DebuggerReply {
  /**
   * Memory map returned in the reply.
   */
  private final MemoryMap memoryMap;

  /**
   * Creates a new Memory Map reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param memoryMap Memory map sent by the debug client. This argument must be null in case of
   *        errors.
   */
  public MemoryMapReply(final int packetId, final int errorCode, final MemoryMap memoryMap) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(memoryMap, "IE01064: Memory map argument can not be null");
    } else {
      if (memoryMap != null) {
        throw new IllegalArgumentException("IE01065: Memory map argument must be null");
      }
    }
    this.memoryMap = memoryMap;
  }

  /**
   * Returns the memory map information sent by the debug client. In case of errors this method
   * returns null.
   *
   * @return The memory map information sent by the debug client or null.
   */
  public MemoryMap getMemoryMap() {
    return memoryMap;
  }
}
