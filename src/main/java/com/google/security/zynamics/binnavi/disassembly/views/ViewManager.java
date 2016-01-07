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
package com.google.security.zynamics.binnavi.disassembly.views;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The ViewManager class is a central instance for a view to view id mapping. Both server as either
 * key or value in the BiMap used as storage. The ViewManager is unique per database connection.
 */
public class ViewManager {

  /**
   * Keeps track of the view managers for the individual databases.
   */
  private static Map<SQLProvider, ViewManager> managers = new HashMap<SQLProvider, ViewManager>();

  /**
   * The {@link SQLProvider provider} used to talk to the database.
   */
  private final SQLProvider provider;

  /**
   * Internal listener to get informed about changes in the {@link SQLProvider provider}.
   */
  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  /**
   * Objects that want to be notified about changes in global comments.
   */
  private final ListenerProvider<ViewManagerListener> listeners =
      new ListenerProvider<ViewManagerListener>();

  /**
   * The BiMap to store the view to view id mapping.
   */
  private final HashBiMap<INaviView, Integer> viewsToId = HashBiMap.create();

  /**
   * Creates a new view manager object.
   */
  private ViewManager(final SQLProvider provider) {
    this.provider = provider;
    this.provider.addListener(providerListener);
  }

  /**
   * Adds a listener object that is notified about changes in global comments.
   *
   * @param listener The listener object to add.
   */
  public synchronized void addListener(final ViewManagerListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Static method to retrieve a ViewManager object by {@link SQLProvider} argument.
   *
   * @param provider The provider to access the database.
   * @return The instance of the ViewManager for the given provider.
   */
  public static synchronized ViewManager get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE02807: provider argument can not be null");
    if (!managers.containsKey(provider)) {
      managers.put(provider, new ViewManager(provider));
    }
    return managers.get(provider);
  }

  /**
   * Removes this {@link ViewManager instance} from the static list of {@link ViewManager view
   * managers}.
   */
  private void close() {
    managers.remove(provider);
    provider.removeListener(providerListener);
  }

  /**
   * Returns a view by view id.
   *
   * @param viewId The view id for the view to look up.
   * @return An {@link INaviView} if its in the BiMap else null.
   */
  public synchronized INaviView getView(final int viewId) {
    return viewsToId.inverse().get(viewId);
  }

  /**
   * Store a view.
   *
   * @param view The {@link INaviView} to store.
   */
  public synchronized void putView(final INaviView view) {
    viewsToId.forcePut(view, view.getConfiguration().getId());
  }

  /**
   * Internal listener class to be informed about provider changes.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (ViewManager.this.provider.equals(provider)) {
        ViewManager.this.close();
      }
    }
  }
}
