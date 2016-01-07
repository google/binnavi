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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.ArrayList;
import java.util.List;



/**
 * Synchronizes global instruction comments between all open views.
 */
public final class CGlobalInstructionLineCommentSynchronizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGlobalInstructionLineCommentSynchronizer() {
  }

  /**
   * Collects the instructions of a code node that have the same address as the given instruction.
   *
   * @param codenode The code node to search through.
   * @param instruction The instruction that provides the address to look for.
   *
   * @return A list of instructions with the same address as the given instruction.
   */
  private static List<INaviInstruction> collectInstructions(
      final INaviCodeNode codenode, final INaviInstruction instruction) {
    final List<INaviInstruction> naviinstrlist = new ArrayList<INaviInstruction>();

    for (final IInstruction instr : codenode.getInstructions()) {
      if (instr instanceof INaviInstruction) {
        final INaviInstruction naviinstr = (INaviInstruction) instr;

        if (instruction.getAddress().equals(naviinstr.getAddress())
            && !naviinstrlist.contains(naviinstr)) {
          if (naviinstr != instruction) {
            naviinstrlist.add(naviinstr);
          }
        }
      }
    }

    return naviinstrlist;
  }

  /**
   * Updates the global instruction comments of an open view.
   *
   * @param view The view whose instructions are updated.
   * @param instruction The instruction that has the new comment.
   * @param comments The new comments of the instruction.
   *
   * @throws CouldntSaveDataException Thrown if a global comment could not be saved to the database.
   */
  private static void updateOpenView(
      final INaviView view, final INaviInstruction instruction, final ArrayList<IComment> comments)
      throws CouldntSaveDataException {
    final List<INaviInstruction> naviinstrlist = new ArrayList<INaviInstruction>();

    for (final INaviViewNode viewnode : view.getGraph().getNodes()) {
      if (viewnode instanceof INaviCodeNode) {
        naviinstrlist.addAll(collectInstructions((INaviCodeNode) viewnode, instruction));
      }
    }

    for (final INaviInstruction instr : naviinstrlist) {
      instr.initializeGlobalComment(comments);
    }
  }

  /**
   * Pushes a new global instruction comment to all open views.
   *
   * @param module The module whose views require updating.
   * @param instruction The instruction that has the new comment.
   * @param comments The new comments of the instruction.
   *
   * @throws CouldntSaveDataException Thrown if updating the instruction comments failed.
   */
  public static void updateOpenViews(final INaviModule module, final INaviInstruction instruction,
      final ArrayList<IComment> comments) throws CouldntSaveDataException {
    if (module.isLoaded()) {
      for (final INaviView view : module.getContent().getViewContainer().getViews()) {
        if (view.isLoaded()) {
          updateOpenView(view, instruction, comments);
        }
      }
    }
  }
}
