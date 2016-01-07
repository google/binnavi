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

import java.util.List;

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Base class for concrete classes that find views in containers.
 */
public abstract class ContainerFinder {
  /**
   * Searches for a view with the given ID.
   * 
   * @param views The views to search through.
   * @param viewId The ID of the view to search for.
   * 
   * @return The view object with the given ID.
   */
  protected INaviView findView(final List<INaviView> views, final int viewId) {
    for (final INaviView naviView : views) {
      if (naviView.getConfiguration().getId() == viewId) {
        return naviView;
      }
    }

    throw new IllegalArgumentException("IE00649: Can not find view");
  }

  /**
   * Searches for a view with the given ID.
   * 
   * @param containerId The ID of the container to search through.
   * @param viewId The ID of the view to search for.
   * 
   * @return The view object with the given ID.
   */
  public abstract INaviView findView(int containerId, int viewId);
}
