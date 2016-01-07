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
package com.google.security.zynamics.binnavi.Database;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.ViewGenerator;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to create new project view objects.
 */
public final class CProjectViewGenerator implements ViewGenerator {
  /**
   * The connection to the database.
   */
  private final SQLProvider m_provider;

  /**
   * The project in which the view is created.
   */
  private final INaviProject m_project;

  /**
   * Creates a new generator object.
   *
   * @param provider The connection to the database.
   * @param project The project in which the view is created.
   */
  public CProjectViewGenerator(final SQLProvider provider, final INaviProject project) {
    m_provider = Preconditions.checkNotNull(provider, "IE00272: provider argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE00452: project argument can not be null");
  }

  @Override
  public CView generate(final int viewId,
      final String name,
      final String description,
      final ViewType viewType,
      final GraphType graphType,
      final java.util.Date creationDate,
      final java.util.Date modificationDate,
      final int blockCount,
      final int edgeCount,
      final Set<CTag> tags,
      final Set<CTag> nodeTags,
      final boolean starState) {
    return new CView(viewId,
        m_project,
        name,
        description,
        viewType,
        graphType,
        creationDate,
        modificationDate,
        blockCount,
        edgeCount,
        tags,
        nodeTags,
        starState,
        m_provider);
  }

  public INaviView generate(final ImmutableNaviViewConfiguration configuration) {
    return new CView(configuration.getViewId(),
        m_project,
        configuration.getName(),
        configuration.getDescription(),
        configuration.getViewType(),
        GraphType.FLOWGRAPH,
        configuration.getCreationDate(),
        configuration.getModificationDate(),
        configuration.getNodeCount(),
        configuration.getEdgeCount(),
        new HashSet<CTag>(),
        new HashSet<CTag>(),
        configuration.getStarState(),
        m_provider);
  }
}
