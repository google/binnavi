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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyInstructionBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeComments;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which provides functions used by the table models for local and global instruction
 * comments.
 * 
 * @author timkornau
 * 
 */
public class CCommentUtilities {

  /**
   * TODO (timkornau): either comment function or find a nicer way to generate a pretty printed
   * instruction line which does not depend on the graph model.
   */
  public static String createInstructionLine(final INaviInstruction instruction,
      final CGraphModel graphModel) {
    final ZyGraphViewSettings graphSettings = graphModel.getGraph().getSettings();
    final BackEndDebuggerProvider provider = graphModel.getDebuggerProvider();
    final INodeModifier modifier = new CDefaultModifier(graphSettings, provider);

    return ZyInstructionBuilder.buildInstructionLine(instruction, graphSettings, modifier)
        .first();
  }

  /**
   * TODO (timkornau): either comment the function or find a better way on how the commenting for
   * instructions can access all comments.
   */
  public static List<Pair<INaviInstruction, IComment>> getLocalInstructionComments(
      final CCodeNode codeNode) {
    Preconditions.checkNotNull(codeNode, "IE02633: codeNode argument can not be null");
    final List<Pair<INaviInstruction, IComment>> values =
        new ArrayList<Pair<INaviInstruction, IComment>>();
    final CCodeNodeComments currentComments = codeNode.getComments();

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      final List<IComment> comments = currentComments.getLocalInstructionComment(instruction);

      if ((comments == null) || comments.isEmpty()) {
        values.add(new Pair<INaviInstruction, IComment>(instruction, null));
        continue;
      } else {
        for (final IComment comment : comments) {
          values.add(new Pair<INaviInstruction, IComment>(instruction, comment));
        }
      }
    }

    return values;
  }
}
