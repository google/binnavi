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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

/**
 * Builder class that builds the content of text nodes.
 *
 * TODO (timkornau): this is essentially the same file as ZyTextNodeBuilder
 */
public final class ZyGroupNodeBuilder {

  /**
   * You are not supposed to instantiate this class.
   */
  private ZyGroupNodeBuilder() {}

  /**
   * Builds the content of a single group node.
   *
   * @param node The group node that provides the underlying data.
   *
   * @return The created content.
   */
  public static ZyLabelContent buildContent(final INaviGroupNode node) {
    Preconditions.checkNotNull(node, "IE01558: Node argument can not be null");
    final ZyLabelContent content = new ZyLabelContent(null);
    buildContent(content, node);
    return content;
  }

  public static void buildContent(final ZyLabelContent content, final INaviGroupNode node) {
    Preconditions.checkNotNull(node, "IE00913: Node argument can not be null");
    Preconditions.checkNotNull(content, "IE02731: content argument can not be null");

    while (content.getLineCount() != 0) {
      content.removeLine(0);
    }
    ZyNodeBuilder.addCommentTagLines(content, node, node.getComments(), null);
  }
}
