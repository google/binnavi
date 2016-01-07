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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.IAbstractCriterium;


/**
 * Interface for normal and cached criterium tree nodes.
 */
public interface IAbstractCriteriumTreeNode {
  /**
   * Returns the children of the node.
   *
   * @return The children of the node.
   */
  List<? extends IAbstractCriteriumTreeNode> getChildren();

  /**
   * Returns the criterium stored in the node.
   *
   * @return The criterium stored in the node.
   */
  IAbstractCriterium getCriterium();
}
