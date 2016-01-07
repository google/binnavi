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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;

/**
 * Adapter class for listeners on changes in the comment manager with empty default implementations.
 */
public class CommentListenerAdapter implements CommentListener {
  @Override
  public void appendedFunctionNodeComment(final INaviFunctionNode functionNode,
      final IComment comment) {
  }

  @Override
  public void appendedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void appendedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void appendedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
  }

  @Override
  public void appendedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
  }

  @Override
  public void appendedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
  }

  @Override
  public void appendedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void appendedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void appendedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
  }

  @Override
  public void appendedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
  }

  @Override
  public void appendedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
  }

  @Override
  public void deletedFunctionNodeComment(final INaviFunctionNode functionNode,
      final IComment comment) {
  }

  @Override
  public void deletedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void deletedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void deletedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
  }

  @Override
  public void deletedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
  }

  @Override
  public void deletedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
  }

  @Override
  public void deletedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void deletedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void deletedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
  }

  @Override
  public void deletedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
  }

  @Override
  public void deletedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
  }

  @Override
  public void editedFunctionNodeComment(final INaviFunctionNode functionNode, final IComment comment) {
  }

  @Override
  public void editedGLobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void editedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void editedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
  }

  @Override
  public void editedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
  }

  @Override
  public void editedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
  }

  @Override
  public void editedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
  }

  @Override
  public void editedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
  }

  @Override
  public void editedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
  }

  @Override
  public void editedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
  }

  @Override
  public void editedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
  }

  @Override
  public void initializedFunctionNodeComments(final INaviFunctionNode functionNode,
      final List<IComment> comments) {
  }

  @Override
  public void initializedGlobalCodeNodeComments(final INaviCodeNode codeNode,
      final List<IComment> comments) {
  }

  @Override
  public void initializedGlobalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
  }

  @Override
  public void initializedGlobalFunctionComments(final INaviFunction function,
      final List<IComment> comments) {
  }

  @Override
  public void initializedGlobalInstructionComments(final INaviInstruction instruction,
      final List<IComment> comments) {
  }

  @Override
  public void initializedGroupNodeComments(final INaviGroupNode groupNode,
      final List<IComment> comments) {
  }

  @Override
  public void initializedLocalCodeNodeComments(final INaviCodeNode codeNode,
      final List<IComment> comments) {
  }

  @Override
  public void initializedLocalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
  }

  @Override
  public void initializedLocalInstructionComments(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final List<IComment> comments) {
  }

  @Override
  public void initializedTextNodeComments(final INaviTextNode textNode,
      final List<IComment> comments) {
  }

  @Override
  public void initializedTypeInstanceComment(final TypeInstance instance,
      final List<IComment> comments) {
  }

  @Override
  public void appendedSectionComment(final Section section, final IComment comment) {
  }

  @Override
  public void deletedSectionComment(final Section section, final IComment comment) {
  }

  @Override
  public void editedSectionComment(final Section section, final IComment comment) {
  }

  @Override
  public void initializedSectionComments(final Section section, final List<IComment> comments) {
  }
}
