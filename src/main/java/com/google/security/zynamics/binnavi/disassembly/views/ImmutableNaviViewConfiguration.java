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

import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * Simple immutable container class for view configuration data.
 * 
 * @author (timkornau)
 * 
 */
public class ImmutableNaviViewConfiguration {

  private final int viewId;
  private final String name;
  private final String description;
  private final ViewType viewType;
  private final Date creationDate;
  private final Date modificationDate;
  private final boolean starState;
  private final int nodeCount;
  private final int edgeCount;

  /**
   * Creates a new immutable navi view configuration container class.
   * 
   * @param viewId {@link Integer} id of the view.
   * @param name {@link String} name of the view.
   * @param description {@link String} description of the view.
   * @param viewType {@link ViewType} type of the view.
   * @param creationDate {@link Date} creation date of the view.
   * @param modificationDate {@link Date} modification date of the view.
   * @param starState {@link Boolean} star state of the view.
   * @param nodeCount {@link Integer} number of nodes in the view.
   * @param edgeCount {@link Integer} number of edges in the view.
   */
  public ImmutableNaviViewConfiguration(final int viewId, final String name,
      final String description, final ViewType viewType, final Date creationDate,
      final Date modificationDate, final boolean starState, final int nodeCount, final int edgeCount) {
    Preconditions.checkArgument(viewId > 0, "Error: view id must be a positive integer");
    this.viewId = viewId;
    this.name = Preconditions.checkNotNull(name, "IE02805: name argument can not be null");
    this.description = description;
    this.viewType = viewType;
    this.creationDate =
        Preconditions.checkNotNull(creationDate, "IE02806: creationDate argument can not be null");
    this.modificationDate =
        Preconditions.checkNotNull(modificationDate,
            "Error: modificationDate argument can not be null");
    this.starState = starState;
    this.nodeCount = nodeCount;
    this.edgeCount = edgeCount;
  }

  public int getViewId() {
    return viewId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public boolean getStarState() {
    return starState;
  }

  public int getNodeCount() {
    return nodeCount;
  }

  public int getEdgeCount() {
    return edgeCount;
  }

  public ViewType getViewType() {
    return viewType;
  }
}
