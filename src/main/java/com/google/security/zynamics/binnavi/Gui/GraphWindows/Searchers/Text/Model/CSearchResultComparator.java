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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CNodeTypeSwitcher;
import com.google.security.zynamics.binnavi.disassembly.algorithms.INodeTypeCallback;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.Serializable;
import java.util.Comparator;



/**
 * Comparator class for search results.
 */
public final class CSearchResultComparator implements Comparator<SearchResult>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4131554230719995844L;

  /**
   * Returns the address of the source node of an edge.
   *
   * @param edge The edge.
   *
   * @return The address of the source node of the edge.
   */
  private IAddress getAddress(final NaviEdge edge) {
    return getAddress(edge.getSource().getRawNode());
  }

  /**
   * Returns the address of a given node.
   *
   * @param node The node whose address is returned.
   *
   * @return The address of the node.
   */
  private IAddress getAddress(final INaviViewNode node) {
    return CNodeTypeSwitcher.switchNode(node, new INodeTypeCallback<IAddress>() {
      @Override
      public IAddress handle(final INaviCodeNode node) {
        return node.getAddress();
      }

      @Override
      public IAddress handle(final INaviFunctionNode node) {
        return node.getAddress();
      }

      @Override
      public IAddress handle(final INaviGroupNode node) {
        return getAddress(node.getElements().get(0));
      }

      @Override
      public IAddress handle(final INaviTextNode node) {
        return getAddress(node.getChildren().get(0));
      }
    });
  }

  @Override
  public int compare(final SearchResult first, final SearchResult second) // NO_UCD
  {
    IAddress firstAddress = null;
    IAddress secondAddress = null;

    if (first.getObject() instanceof NaviEdge) {
      firstAddress = getAddress((NaviEdge) first.getObject());
    } else if (first.getObject() instanceof NaviNode) {
      firstAddress = getAddress(((NaviNode) first.getObject()).getRawNode());
    }

    if (second.getObject() instanceof NaviEdge) {
      secondAddress = getAddress((NaviEdge) second.getObject());
    } else if (second.getObject() instanceof NaviNode) {
      secondAddress = getAddress(((NaviNode) second.getObject()).getRawNode());
    }

    if ((firstAddress == null) || (secondAddress == null)) {
      throw new IllegalStateException("IE01155: Address can't be null.");
    }

    return firstAddress.toBigInteger().compareTo(secondAddress.toBigInteger());
  }
}
