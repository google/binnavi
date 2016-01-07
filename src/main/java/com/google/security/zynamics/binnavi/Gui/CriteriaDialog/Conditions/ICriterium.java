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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Interface to be implemented by all classes that want to serve as criteria in the Select by
 * Criteria dialog.
 */
public interface ICriterium extends IAbstractCriterium {
  /**
   * Adds a listener that is notified about changes in the criterium.
   *
   * @param listener The listener object to add.
   */
  void addListener(ICriteriumListener listener);

  /**
   * Takes the input in the criterium and turns it into a cached criterium object.
   *
   * @return The cached criterium.
   */
  ICachedCriterium createCachedCriterium();

  void dispose();

  /**
   * Returns the description of the criterium.
   *
   * @return The description of the criterium.
   */
  String getCriteriumDescription();

  /**
   * Returns the panel that is shown when the node that represents the criterium is selected in the
   * criteria tree.
   *
   * @return The panel shown for this criterium.
   */
  JPanel getCriteriumPanel();

  /**
   * Returns the icon shown for the criterium once the node that represents this criterium is
   * selected in the criteria tree.
   *
   * @return The icon shown for this criterium.
   */
  Icon getIcon();

  /**
   * Removes a listener object that was previously notified about changes in the criterium.
   *
   * @param listener The listener object to add.
   */
  void removeListener(ICriteriumListener listener);
}
