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
package com.google.security.zynamics.binnavi.Database.Interfaces;

import java.util.Set;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * Interface for all classes that want to generate view objects.
 */
public interface ViewGenerator {
  /**
   * Generates a new view object.
   * 
   * @param viewId ID of the view.
   * @param name Name of the view.
   * @param description Description of the view.
   * @param viewType Type of the view.
   * @param graphType The type of the view graph.
   * @param creationDate Creation data of the view.
   * @param modificationDate Modification date of the view.
   * @param blockCount The number of blocks in the view.
   * @param edgeCount The number of edges in the view.
   * @param tags Tags the views is tagged with.
   * @param nodeTags Node tags the nodes of the view are tagged with.
   * 
   * @return The created view.
   */
  CView generate(int viewId, String name, String description, ViewType viewType,
      GraphType graphType, java.util.Date creationDate, java.util.Date modificationDate,
      int blockCount, int edgeCount, Set<CTag> tags, Set<CTag> nodeTags, boolean starState);
}
