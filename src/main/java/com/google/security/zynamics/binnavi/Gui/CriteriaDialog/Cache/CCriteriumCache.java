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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;



/**
 * Keeps track of previously executed Select by Criteria trees and stores them for later reuse.
 */
public final class CCriteriumCache {
  /**
   * Maximum number of items in the cache.
   */
  private static final int MAXIMUM_CACHE_SIZE = 10;

  /**
   * Cached trees for later reuse.
   */
  private final IFilledList<CCachedExpressionTree> m_trees =
      new FilledList<CCachedExpressionTree>();

  /**
   * Listeners that are notified about changed in the criterium cache.
   */
  private final ListenerProvider<ICriteriumCacheListener> m_listeners =
      new ListenerProvider<ICriteriumCacheListener>();

  /**
   * Adds a new expression tree to the cache.
   *
   * @param tree The tree to cache.
   */
  public void add(final CCachedExpressionTree tree) {
    for (final CCachedExpressionTree cachedTree : m_trees) {
      // Don't store duplicate formulas

      if (cachedTree.getFormulaString().equals(tree.getFormulaString())) {
        return;
      }
    }

    m_trees.add(tree);

    while (m_trees.size() > MAXIMUM_CACHE_SIZE) {
      m_trees.remove(0);
    }

    for (final ICriteriumCacheListener listener : m_listeners) {
      try {
        listener.changedCriteria(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a listener object that is notified about changes in the criterium cache.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ICriteriumCacheListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the cached expression trees.
   *
   * @return The cached expression trees.
   */
  public IFilledList<CCachedExpressionTree> getTrees() {
    return new FilledList<CCachedExpressionTree>(m_trees);
  }

  /**
   * Removes a listener object that was previously added to the criterium cache.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final ICriteriumCacheListener listener) {
    m_listeners.removeListener(listener);
  }
}
