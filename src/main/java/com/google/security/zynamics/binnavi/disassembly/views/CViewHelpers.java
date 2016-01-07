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

import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;

/**
 * Contains helper classes for working with views.
 */
public final class CViewHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewHelpers() {
  }

  /**
   * Determines whether a given view contains a node with a given address.
   * 
   * @param view The view to search through.
   * @param address The address to search for.
   * 
   * @return True, if the view contains the given address. False, otherwise.
   */
  public static boolean containsAddress(final INaviView view, final UnrelocatedAddress address) {
    for (final INaviViewNode node : view.getGraph().getNodes()) {
      if (node instanceof INaviFunctionNode) {
        final INaviFunctionNode fnode = (INaviFunctionNode) node;

        if (fnode.getFunction().getAddress().equals(address.getAddress())) {
          return true;
        }
      } else if (node instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) node;

        if (CCodeNodeHelpers.containsAddress(cnode, address.getAddress())) {
          return true;
        }
      }
    }

    return false;
  }
}
