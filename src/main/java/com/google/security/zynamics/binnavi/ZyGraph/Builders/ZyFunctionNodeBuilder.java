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
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

import java.awt.Color;
import java.awt.Font;

/**
 * Builds the content of function nodes.
 */
public final class ZyFunctionNodeBuilder {

  /**
   * Default bold font for text content.
   */
  private static final Font BOLD_FONT = new Font(GuiHelper.getMonospaceFont(), Font.BOLD, 12);

  /**
   * Default font for text content.
   */
  private static final Font NORMAL_FONT = new Font(GuiHelper.getMonospaceFont(), Font.PLAIN, 12);

  /**
   * You are not supposed to instantiate this class.
   */
  private ZyFunctionNodeBuilder() {}

  /**
   * Builds the address line of a function node.
   *
   * @param node The function node which provides the raw data.
   * @param content The node content object where the address line is added.
   * @param modifier
   */
  private static void buildAddressLine(
      final INaviFunctionNode node, final ZyLabelContent content, final INodeModifier modifier) {
    final String module = node.getFunction().getModule().getConfiguration().getName();
    final String standardAddress = node.getFunction().getAddress().toHexString();
    final String address = modifier == null ? standardAddress : modifier.getAddress(node);

    final CStyleRunData styleRun =
        address.equals(standardAddress) ? new CStyleRunData(0, -1, Color.BLACK)
            : new CStyleRunData(0, -1, Color.RED);

    final ZyLineContent addressLine =
        new ZyLineContent(module + "::" + address, BOLD_FONT, Lists.newArrayList(styleRun), null);

    content.addLineContent(addressLine);
  }

  /**
   * Builds the name line of a function node.
   *
   * @param node The function node which provides the raw data.
   * @param content The node content object where the name line is added.
   * @param showFunctionInformation True, to show function information in the node. False, to hide
   *        it.
   */
  private static void buildNameLine(final INaviFunctionNode node, final ZyLabelContent content,
      final boolean showFunctionInformation) {
    final INaviFunction function = node.getFunction();
    String informationString = "";
    if (function.getBasicBlockCount() > 0) {
      final StringBuilder builder = new StringBuilder();
      builder.append(" (");
      builder.append(function.getBasicBlockCount());
      builder.append(" basic block");
      if (function.getBasicBlockCount() > 1) {
        builder.append('s');
      }
      if (function.getEdgeCount() > 0) {
        builder.append(", ");
        builder.append(function.getEdgeCount());
        builder.append(" edge");
        if (function.getEdgeCount() > 1) {
          builder.append('s');
        }
      }
      builder.append(')');
      informationString = builder.toString();
    }

    final ZyLineContent nameLine =
        new ZyLineContent(function.getName() + informationString, NORMAL_FONT, null);

    content.addLineContent(nameLine);
  }

  /**
   * Creates the node content of a function node.
   *
   * @param node The node which provides the raw data.
   * @param graphSettings Graph settings used to build the graph.
   * @param modifier Calculates the address strings. This argument can be null.
   *
   * @return The generated node content of the function node.
   */
  public static ZyLabelContent buildContent(final INaviFunctionNode node,
      final ZyGraphViewSettings graphSettings, final INodeModifier modifier) {
    Preconditions.checkNotNull(node, "IE01556: Argument node can't be null");

    final ZyLabelContent content = new ZyLabelContent(null);

    buildContent(content, node, graphSettings, modifier);

    return content;
  }

  public static void buildContent(final ZyLabelContent content, final INaviFunctionNode node,
      final ZyGraphViewSettings graphSettings, final INodeModifier modifier) {
    Preconditions.checkNotNull(node, "IE00903: Argument node can't be null");

    while (content.getLineCount() != 0) {
      content.removeLine(0);
    }

    buildAddressLine(node, content, modifier);
    buildNameLine(node, content, graphSettings.getDisplaySettings().getFunctionNodeInformation());

    ZyNodeBuilder.addCommentTagLines(
        content, node, node.getLocalFunctionComment(), node.getFunction().getGlobalComment());
  }
}
