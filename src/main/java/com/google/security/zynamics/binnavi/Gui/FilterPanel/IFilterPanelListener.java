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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;

import java.awt.event.MouseEvent;

/**
 * Interface to be implemented by all objects that want to be notified about changes in filter panel
 * filters.
 * 
 * @param <T> Type of the elements shown in the filtered table.
 */
public interface IFilterPanelListener<T> {
  /**
   * Invoked after the filter of a filter panel changed.
   * 
   * @param filterPanel The filter panel whose filter changed.
   * @param filter The new active filter of the filter panel.
   */
  void changedFilter(CFilterPanel<T> filterPanel, IFilter<T> filter);

  /**
   * Invoked after the mouse was pressed on the filter panel.
   * 
   * @param event The mouse event.
   */
  void mousePressed(MouseEvent event);

  /**
   * Invoked after the mouse was released on the filter panel.
   * 
   * @param event The mouse event.
   */
  void mouseReleased(MouseEvent event);
}
