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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders;

import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.view.Arrow;
import y.view.LineType;

/**
 * Class that can be used for initializing edges.
 */
public final class EdgeInitializer {
  /**
   * You are not supposed to instantiate this class.
   */
  private EdgeInitializer() {
  }

  /**
   * Initializes the color of an edge.
   *
   * @param edge The edge whose color is changed.
   */
  public static void adjustColor(final INaviEdge edge) {
    switch (edge.getType()) {
      case JUMP_CONDITIONAL_FALSE:
        edge.setColor(ConfigManager.instance().getColorSettings().getConditionalJumpFalseColor());
        return;
      case JUMP_CONDITIONAL_FALSE_LOOP:
        edge.setColor(ConfigManager.instance().getColorSettings().getConditionalJumpFalseColor());
        return;
      case JUMP_CONDITIONAL_TRUE:
        edge.setColor(ConfigManager.instance().getColorSettings().getConditionalJumpTrueColor());
        return;
      case JUMP_CONDITIONAL_TRUE_LOOP:
        edge.setColor(ConfigManager.instance().getColorSettings().getConditionalJumpTrueColor());
        return;
      case JUMP_UNCONDITIONAL_LOOP:
        edge.setColor(ConfigManager.instance().getColorSettings().getUnconditionalJumpColor());
        return;
      case ENTER_INLINED_FUNCTION:
        edge.setColor(ConfigManager.instance().getColorSettings().getEnterInlinedJumpColor());
        return;
      case LEAVE_INLINED_FUNCTION:
        edge.setColor(ConfigManager.instance().getColorSettings().getLeaveInlinedJumpColor());
        return;
      case TEXTNODE_EDGE:
        edge.setColor(ConfigManager.instance().getColorSettings().getTextEdgeColor());
        return;
      case JUMP_SWITCH:
        edge.setColor(ConfigManager.instance().getColorSettings().getSwitchJumpColor());
        return;
      default:
        edge.setColor(ConfigManager.instance().getColorSettings().getUnconditionalJumpColor());
        return;
    }
  }

  /**
   * Modifies the realizer of an edge according to the edge type.
   *
   * @param edge The edge that determines the realizer appearance.
   * @param realizer The realizer whose appearance is changed.
   */
  public static void initializeEdgeType(
      final INaviEdge edge, final ZyEdgeRealizer<NaviEdge> realizer) {
    switch (edge.getType()) {
      case JUMP_CONDITIONAL_FALSE:
        realizer.setLineType(LineType.LINE_2);
        return;
      case JUMP_CONDITIONAL_FALSE_LOOP:
        realizer.setLineType(LineType.DASHED_DOTTED_2);
        return;
      case JUMP_CONDITIONAL_TRUE:
        realizer.setLineType(LineType.LINE_2);
        return;
      case JUMP_CONDITIONAL_TRUE_LOOP:
        realizer.setLineType(LineType.DASHED_DOTTED_2);
        return;
      case JUMP_UNCONDITIONAL_LOOP:
        realizer.setLineType(LineType.DASHED_DOTTED_2);
        return;
      case ENTER_INLINED_FUNCTION:
        realizer.setLineType(LineType.DASHED_2);
        return;
      case LEAVE_INLINED_FUNCTION:
        realizer.setLineType(LineType.DASHED_2);
        return;
      case TEXTNODE_EDGE:
        realizer.setArrow(Arrow.WHITE_DIAMOND);
        return;
      default:
        realizer.setLineType(LineType.LINE_2);
        return;
    }
  }
}
