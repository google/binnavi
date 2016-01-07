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
package com.google.security.zynamics.binnavi.debug.connection.helpers;

import java.util.concurrent.Semaphore;

/**
 * Packet ID generator that guarantees unique packet ID generation. The first generated packet ID is
 * 0. Calls to next automatically increase the packet ID counter.
 */
public final class PacketIdGenerator {
  /**
   * This counter is used to create unique packet IDs.
   *
   */
  private static int packetIds = 0;

  /**
   * Semaphore that is used to synchronize access to the packet ID counter.
   */
  private final Semaphore packetSemaphore = new Semaphore(1, true);

  /**
   * Returns the next packet ID.
   *
   * @return The next packet ID.
   */
  public int next() {
    packetSemaphore.acquireUninterruptibly();
    final int packet = packetIds++;
    packetSemaphore.release();
    return packet;
  }
}
