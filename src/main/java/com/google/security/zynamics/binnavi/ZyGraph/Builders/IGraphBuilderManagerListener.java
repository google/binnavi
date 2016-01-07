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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphBuilder;

/**
 * Interface for all objects that want to be notified about changes in managed graph builders.
 */
public interface IGraphBuilderManagerListener {
  /**
   * Invoked after the builder of a view was changed.
   *
   * @param view The view to be built.
   * @param builder The builder that builds the view.
   */
  void addedBuilder(INaviView view, ZyGraphBuilder builder);

  /**
   * Invoked after the builder of a view was removed.
   *
   * @param view The view to be built.
   * @param builder The builder that built the view.
   */
  void removedBuilder(INaviView view, ZyGraphBuilder builder);
}
