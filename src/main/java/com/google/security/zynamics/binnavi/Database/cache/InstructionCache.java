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
package com.google.security.zynamics.binnavi.Database.cache;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.HashMap;
import java.util.Map;

public class InstructionCache {

  private static Map<SQLProvider, InstructionCache> caches =
      new HashMap<SQLProvider, InstructionCache>();

  Cache<Pair<IAddress, Integer>, INaviInstruction> instructionByAddressCache =
      CacheBuilder.newBuilder().weakValues().build();

  private SQLProvider provider;

  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  private InstructionCache(final SQLProvider provider) {
    this.provider = provider;
    this.provider.addListener(providerListener);
  }

  public static synchronized InstructionCache get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE01239: Provider argument can not be null");
    if (!caches.containsKey(provider)) {
      caches.put(provider, new InstructionCache(provider));
    }
    return caches.get(provider);
  }

  private void close() {
    caches.remove(provider);
    provider.removeListener(providerListener);
  }

  public void addInstructions(final Iterable<INaviInstruction> instructions) {
    instructionByAddressCache.putAll(
        Maps.uniqueIndex(instructions, new Function<INaviInstruction, Pair<IAddress, Integer>>() {
          @Override
          public Pair<IAddress, Integer> apply(final INaviInstruction instruction) {
            return new Pair<IAddress, Integer>(
                instruction.getAddress(), instruction.getModule().getConfiguration().getId());
          }
        }));
  }

  public INaviInstruction getInstructionByAddress(final IAddress address, final Integer moduleId) {
    return instructionByAddressCache.getIfPresent(new Pair<IAddress, Integer>(address, moduleId));
  }

  public void addInstruction(final INaviInstruction instruction) {
    instructionByAddressCache.put(new Pair<IAddress, Integer>(
        instruction.getAddress(), instruction.getModule().getConfiguration().getId()), instruction);
  }

  /**
   * Internal listener class to keep informed about changes in the {@link SQLProvider provider}.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (InstructionCache.this.provider.equals(provider)) {
        InstructionCache.this.close();
      }
    }
  }
}
