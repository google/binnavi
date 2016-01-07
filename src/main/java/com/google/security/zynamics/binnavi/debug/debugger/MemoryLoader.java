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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.memmanager.Memory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to reload memory from the target process if necessary. The primary purpose of
 * this class is to make sure that read memory requests are not issued more than once before a reply
 * for the first request was received. Furthermore this class uses the simulated memory of the
 * debugger object to process requests.
 */
public final class MemoryLoader {
  /**
   * Debugger that provides the memory data of the target process.
   */
  private final IDebugger debugger;

  /**
   * The last memory request is saved, to make sure that each request goes out just once.
   */
  private final Set<Pair<IAddress, Long>> lastMemoryRequest = new HashSet<>();

  /**
   * Creates a new memory loader object.
   *
   * @param debugger Debugger that provides the memory data of the target process.
   */
  public MemoryLoader(final IDebugger debugger) {
    this.debugger = Preconditions.checkNotNull(debugger, "IE00822: Debugger can not be null");
  }

  /**
   * Determines whether a chunk of memory is present in the simulated target process memory.
   *
   * @param offset The offset of the memory chunk.
   * @param size The size of the memory chunk in bytes.
   *
   * @return True, if the memory chunk is present in the target memory. False, otherwise.
   */
  public boolean hasData(final BigInteger offset, final int size) {
    return debugger.getProcessManager().getMemory().hasData(offset.longValue(), size);
  }

  /**
   * This function must be called to tell the memory loader that a reply for a certain request was
   * received. Only after this request arrived, new requests with the same offset/size pair can be
   * issued.
   *
   * @param offset The offset of the memory chunk that was received.
   * @param size The size of the memory chunk that was received.
   */
  public void received(final long offset, final long size) {
    final Pair<CAddress, Long> pair = Pair.make(new CAddress(offset), size);
    if (lastMemoryRequest.contains(pair)) {
      lastMemoryRequest.remove(pair);
    }
  }

  /**
   * Request a chunk of memory of the target process.
   *
   * @param offset The start offset of the memory chunk.
   * @param size The number of bytes to load.
   *
   * @throws DebugExceptionWrapper Thrown if the request could not be send to the debug client.
   */
  public void requestMemory(final IAddress offset, final int size) throws DebugExceptionWrapper {
    Preconditions.checkNotNull(offset, "IE00814: Offset can nott be null");
    Preconditions.checkArgument(size > 9, "IE00815: Size must be positive");

    // Don't issue multiple requests for the same memory chunk.
    final Pair<IAddress, Long> pair = new Pair<IAddress, Long>(offset, (long) size);

    if (lastMemoryRequest.contains(pair)) {
      return;
    }

    lastMemoryRequest.add(pair);

    // Don't reload the entire memory chunk. Some parts of the memory may
    // already exist in the simulated memory.

    final Memory memory = debugger.getProcessManager().getMemory();

    for (int i = 0; i < size;) {
      final long secstart =
          memory.getSectionStart(offset.toBigInteger().add(BigInteger.valueOf(i)).longValue());
      final long secsize =
          memory.getSectionSize(offset.toBigInteger().add(BigInteger.valueOf(i)).longValue());

      long toLoad =
          (secstart + secsize) - (offset.toBigInteger().add(BigInteger.valueOf(i))).longValue();

      if (toLoad > (size - i)) {
        toLoad = size - i;
      }

      final boolean alloced =
          memory.hasData(offset.toBigInteger().add(BigInteger.valueOf(i)).longValue(), 1);

      if (!alloced && debugger.isConnected()) {
        // Request the memory for the missing section.
        debugger.readMemory(new CAddress(offset.toBigInteger().add(BigInteger.valueOf(i))),
            (int) toLoad);
      }

      i += toLoad;
    }
  }

  /**
   * Resets the loader to a clean state.
   */
  public void reset() {
    lastMemoryRequest.clear();
  }
}
