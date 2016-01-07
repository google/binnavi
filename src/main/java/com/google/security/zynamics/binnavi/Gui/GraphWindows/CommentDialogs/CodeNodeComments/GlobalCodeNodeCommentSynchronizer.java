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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CodeNodeComments;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Synchronizes global code node comments between all open views.
 * 
 * TODO: What about function nodes? TODO: What about views in projects?
 */
public final class GlobalCodeNodeCommentSynchronizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private GlobalCodeNodeCommentSynchronizer() {
  }

  /**
   * Collects the nodes of a view that have the same address as the given code node.
   * 
   * @param view The view to search through.
   * @param codeNode The code node that provides the base address.
   * 
   * @return A list of code nodes from the view that have the same base address as the code node.
   */
  private static List<INaviCodeNode> collectNodes(final INaviView view, final INaviCodeNode codeNode) {
    final List<INaviCodeNode> nodelist = new ArrayList<INaviCodeNode>();

    for (final INaviViewNode viewnode : view.getGraph().getNodes()) {
      if (viewnode instanceof INaviCodeNode) {
        final INaviCodeNode node = (INaviCodeNode) viewnode;

        if (codeNode.getAddress().equals(node.getAddress()) && !nodelist.contains(node)
            && (node != codeNode)) {
          nodelist.add(node);
        }
      }
    }

    return nodelist;
  }

  /**
   * Pushes a new global code node comment to all open views.
   * 
   * @param module The module whose views require updating.
   * @param codeNode The code node that has the new comment.
   * @param comments The new comment of the code node.
   * 
   * @throws CouldntSaveDataException Thrown if updating the code node comments failed.
   */
  public static void updateOpenViews(final INaviModule module, final INaviCodeNode codeNode,
      final ArrayList<IComment> comments) throws CouldntSaveDataException {
    if (module.isLoaded()) {
      final List<INaviCodeNode> nodelist = new ArrayList<INaviCodeNode>();

      for (final INaviView view : module.getContent().getViewContainer().getViews()) {
        if (view.isLoaded()) {
          nodelist.addAll(collectNodes(view, codeNode));
        }
      }

      final List<IComment> codeNodeComments = codeNode.getComments().getGlobalCodeNodeComment();

      for (final INaviCodeNode currentCodeNode : nodelist) {
        final List<IComment> currentNodeComments =
            currentCodeNode.getComments().getGlobalCodeNodeComment();

        if (codeNodeComments.equals(currentNodeComments)) {
          continue;
        } else {
          currentCodeNode.getComments().initializeGlobalCodeNodeComment(comments);
        }
      }
    }
  }
}
