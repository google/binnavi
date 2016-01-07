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
 * Represents the reply sent by the debug client when memory data of the target process was
 * requested.
 */
public final class ReadMemoryReply extends DebuggerReply {
  /**
   * Start address from where the data is read.
   */
  private final IAddress startAddress;

  /**
   * Data read from the memory of the target process.
   */
  private final byte[] memoryData;

  /**
   * Creates a new Read Memory reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param address Start address from where the data is read. In case of errors, this argument must
   *        be null.
   * @param data Data read from the memory of the target process. In case of errors, this argument
   *        must be null.
   */
  public ReadMemoryReply(final int packetId, final int errorCode, final IAddress address,
      final byte[] data) {
    super(packetId, errorCode);

    if (success()) {
      Preconditions.checkNotNull(address, "IE01066: Address argument can not be null");
      Preconditions.checkNotNull(data, "IE01067: Data argument can not be null");
    } else {
      if (address != null) {
        throw new IllegalArgumentException("IE01068: Address argument must be null");
      }

      if (data != null) {
        throw new IllegalArgumentException("IE01069: Data argument must be null");
      }
    }

    startAddress = address;

    memoryData = data == null ? null : data.clone();
  }

  /**
   * Returns the start address from where the memory was read. This method returns null in case of
   * errors.
   *
   * @return The start address from where the memory was read or null.
   */
  public IAddress getAddress() {
    return startAddress;
  }

  /**
   * Returns the data read from the memory of the target process.
   *
   * @return The data read from the memory or null.
   */
  public byte[] getData() {
    return memoryData == null ? null : memoryData.clone();
  }
}
