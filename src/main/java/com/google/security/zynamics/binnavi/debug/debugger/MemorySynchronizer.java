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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IMemoryProvider;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.memmanager.IMemoryListener;

import java.math.BigInteger;

/**
 * The memory synchronizer class is used to synchronize memory requests from BinNavi with the
 * attached debugger. The primary purpose of this class is to cache memory requests to avoid lags
 * when scrolling through the memory in the debugger panel.
 */
public final class MemorySynchronizer {
  /**
   * The debugger that is synchronized.
   */
  private final IDebugger debugger;

  /**
   * Used to load memory from the target process.
   */
  private final MemoryLoader memoryLoader;

  /**
   * Internal memory provider class that provides target process memory.
   */
  private final InternalMemoryProvider memoryProvider = new InternalMemoryProvider();

  /**
   * Used to synchronize the simulated process memory with the target process memory loader.
   */
  private final InternalMemoryListener memoryListener = new InternalMemoryListener();

  /**
   * Updates the loader on relevant changes in the debugger.
   */
  private final InternalDebugListener debuggerListener = new InternalDebugListener();

  /**
   * Creates a new memory synchronizer object.
   *
   * @param debugger The debugger that is synchronized.
   */
  public MemorySynchronizer(final IDebugger debugger) {
    this.debugger =
        Preconditions.checkNotNull(debugger, "IE00816: Debugger argument can not be null");
    this.debugger.addListener(debuggerListener);
    this.debugger.getProcessManager().getMemory().addMemoryListener(memoryListener);
    memoryLoader = new MemoryLoader(debugger);
  }

  /**
   * Returns the memory provider object.
   *
   * @return The memory provider object.
   */
  public IMemoryProvider getMemoryProvider() {
    return memoryProvider;
  }

  /**
   * Updates the loader on relevant changes in the debugger.
   */
  private class InternalDebugListener extends DebugEventListenerAdapter {
    @Override
    public void debuggerClosed(final int errorCode) {
      memoryLoader.reset();
    }

    @Override
    public void receivedReply(final ProcessClosedReply reply) {
      memoryLoader.reset();
    }
  }

  /**
   * Used to synchronize the simulated process memory with the target process memory loader.
   */
  private class InternalMemoryListener implements IMemoryListener {
    @Override
    public void memoryChanged(final long address, final int size) {
      memoryLoader.received(address, size);
    }

    @Override
    public void memoryCleared() {
      memoryLoader.reset();
    }
  }

  /**
   * Internal memory provider class that provides target process memory.
   */
  private class InternalMemoryProvider implements IMemoryProvider {
    @Override
    public boolean hasData(final BigInteger offset, final int size) {
      // For optimization reasons we do not simply load the requested
      // range but also some bytes before and after the range.
      final Pair<IAddress, Integer> realRange =
          MemoryRangeCalculator.calculateRequestRange(debugger, offset, size);

      try {
        memoryLoader.requestMemory(realRange.first(), realRange.second());
      } catch (final DebugExceptionWrapper e) {
        CUtilityFunctions.logException(e);
      }

      return memoryLoader.hasData(offset, size);
    }

    @Override
    public boolean hasData(final BigInteger cacheOffset, final int cacheSize,
        final BigInteger offset, final int size) {
      try {
        memoryLoader.requestMemory(new CAddress(offset), size);
      } catch (final DebugExceptionWrapper e) {
        CUtilityFunctions.logException(e);
      }

      return memoryLoader.hasData(cacheOffset, cacheSize);
    }
  }
}
