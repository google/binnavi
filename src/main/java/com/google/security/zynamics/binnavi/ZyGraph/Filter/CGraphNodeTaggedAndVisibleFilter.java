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
package com.google.security.zynamics.binnavi.ZyGraph.Filter;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.util.Set;

/**
 * Filter used to select visible nodes according to their tag state.
 */
public final class CGraphNodeTaggedAndVisibleFilter implements ICollectionFilter<NaviNode> {

  private final Set<CTag> m_tags;

  /**
   * Creates a new filter object.
   * 
   * @param tags The list of tags to filter the nodes for.
   */
  public CGraphNodeTaggedAndVisibleFilter(final Set<CTag> tags) {
    m_tags = Preconditions.checkNotNull(tags, "IE02111: Tags argument can not be null");
  }

  @Override
  public boolean qualifies(final NaviNode item) // NO_UCD
  {
    if (!item.getRawNode().isVisible()) {
      return false;
    }

    for (final CTag tag : m_tags) {
      if (item.getRawNode().isTagged(tag)) {
        return true;
      }
    }

    return false;
  }
}
