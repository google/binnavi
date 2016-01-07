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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CAbstractOperatorPanel;

/**
 * Panel shown when NOT nodes are selected in the tree.
 */
public final class CNotCriteriumPanel extends CAbstractOperatorPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7539495335934502842L;

  @Override
  public String getBorderTitle() {
    return "NOT Operator";
  }

  @Override
  public String getInvalidInfoString() {
    return "NOT operator requires exactly one child.";
  }

  @Override
  public String getValidInfoString() {
    return "NOT Operator is valid.";
  }
}
