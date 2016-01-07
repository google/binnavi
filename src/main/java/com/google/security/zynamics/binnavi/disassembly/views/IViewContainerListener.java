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

import java.util.List;


/**
 * Interface for objects that want to be notified about changes in view containers.
 */
public interface IViewContainerListener {
  /**
   * Invoked after a view was added to the container.
   * 
   * @param container The container the view was added to.
   * @param view The view that was added.
   */
  void addedView(IViewContainer container, INaviView view);

  /**
   * Invoked after the view container was closed.
   * 
   * @param container The container that was closed.
   * @param views The views that belonged to the container.
   */
  void closedContainer(IViewContainer container, List<INaviView> views);

  /**
   * Invoked after a view was removed from a container.
   * 
   * @param container The container the view was removed from.
   * @param view The view that was removed from the container.
   */
  void deletedView(IViewContainer container, INaviView view);

  /**
   * Invoked after a container was loaded.
   * 
   * @param container The container that was loaded.
   */
  void loaded(IViewContainer container);
}
