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
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser responsible for parsing replies to Map Memory requests.
 */
public final class MemoryMapParser extends AbstractReplyParser<MemoryMapReply> {
  /**
   * Creates a new Map Memory reply parser.
   *
   * @param clientReader Used to read messages sent by the debug client.
   */
  public MemoryMapParser(final ClientReader clientReader) {
    super(clientReader, DebugCommandType.RESP_MEMMAP_SUCCESS);
  }

  @Override
  protected MemoryMapReply parseError(final int packetId) throws IOException {
    final int errorCode = parseInteger();
    return new MemoryMapReply(packetId, errorCode, null);
  }

  @Override
  public MemoryMapReply parseSuccess(final int packetId, final int argumentCount)
      throws IOException {
    final IAddress addresses[] = new IAddress[argumentCount];
    for (int i = 0; i < argumentCount; i++) {
      addresses[i] = parseAddress();
    }
    final List<MemorySection> map = new ArrayList<>();
    for (int i = 0; i < addresses.length / 2; i++) {
      final IAddress startAddress = addresses[2 * i];
      final IAddress endAddress = addresses[2 * i + 1];
      map.add(new MemorySection(startAddress, endAddress));
    }
    return new MemoryMapReply(packetId, 0, new MemoryMap(map));
  }
}
