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
package com.google.security.zynamics.binnavi.disassembly.functions;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class which keeps track of loaded {@link INaviFunction} in BinNavi. This class is here
 * because when synchronizing information between different instances of BinNavi over the database a
 * quick lookup must be available to access functions that have changed. This class is supposed to
 * solve this problem.
 *
 * @author (timkornau)
 *
 */
public class FunctionManager {
  /**
   * Keeps track of the functions managers for the individual databases.
   */
  private static Map<SQLProvider, FunctionManager> managers =
      new HashMap<SQLProvider, FunctionManager>();

  /**
   * The {@link SQLProvider provider} used to communicate with the database. Used here to make sure
   * only one {@link FunctionManager manager} exists per database.
   */
  private final SQLProvider provider;

  /**
   * Listener which gets informed about changes in the {@link SQLProvider provider}. Used here to
   * make sure we clean up the static references to the {@link SQLProvider provider} on close.
   */
  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  /**
   * The BiMap to map functions to its keys and reverse.
   */
  private final HashBiMap<INaviFunction, Pair<IAddress, Integer>> functionsToAddressModule =
      HashBiMap.create();

  /**
   * private constructor to force static access.
   */
  private FunctionManager(final SQLProvider provider) {
    this.provider = provider;
    this.provider.addListener(providerListener);
  }

  /**
   * Method the clears the current {@link FunctionManager mamager} from the static container of all
   * {@link FunctionManager managers}.
   */
  private void close() {
    managers.remove(provider);
    provider.removeListener(providerListener);
  }

  /**
   * Method to retrieve the right {@link FunctionManager} for a given {@link SQLProvider}.
   *
   * @param provider The {@link SQLProvider} to get the {@link FunctionManager} for.
   * @return The {@link FunctionManager} for the given {@link SQLProvider}.
   */
  public static synchronized FunctionManager get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE02755: provider argument can not be null");
    if (!managers.containsKey(provider)) {
      managers.put(provider, new FunctionManager(provider));
    }
    return managers.get(provider);
  }

  /**
   * Returns a {@link INaviFunction} given a function {@link IAddress address} and a module id.
   *
   * @param address The {@link IAddress} of the function {@link IAddress}.
   * @param moduleId The database id of the module that contains the address.
   * @return The {@link INaviFunction} or null if no corresponding function is found.
   */
  public synchronized INaviFunction getFunction(final IAddress address, final int moduleId) {
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    return functionsToAddressModule.inverse().get(new Pair<IAddress, Integer>(address, moduleId));
  }

  /**
   * Adds a {@link INaviFunction} to the {@link FunctionManager}.
   *
   * @param function The {@link INaviFunction} to add.
   */
  public synchronized void putFunction(final INaviFunction function) {
    functionsToAddressModule.forcePut(function, new Pair<IAddress, Integer>(
        function.getAddress(), function.getModule().getConfiguration().getId()));
  }

  /**
   * Internal listener class to get informed about changes in the {@link SQLProvider provider}.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (FunctionManager.this.provider.equals(provider)) {
        FunctionManager.this.close();
      }
    }
  }
}
