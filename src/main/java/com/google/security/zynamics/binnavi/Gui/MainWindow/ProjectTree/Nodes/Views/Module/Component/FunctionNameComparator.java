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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Special comparator used to compare function names while clustering functions of the same type.
 */
public final class FunctionNameComparator implements Comparator<CFunctionNameTypePair>,
    Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3139128190821997L;

  @Override
  public int compare(final CFunctionNameTypePair lhs, final CFunctionNameTypePair rhs) // NO_UCD
  {
    if (lhs.getFunctionType().ordinal() < rhs.getFunctionType().ordinal()) {
      return -1;
    } else if (lhs.getFunctionType().ordinal() > rhs.getFunctionType().ordinal()) {
      return 1;
    } else if (lhs.getFunctionType() == rhs.getFunctionType()) {
      return lhs.getName().compareToIgnoreCase(rhs.getName());
    } else {
      return 0;
    }
  }
}
