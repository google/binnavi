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
 * Listener interface for objects that to be notified about changed global comments.
 */
public interface CommentListener {

  /**
   * Invoked after a new {@link INaviFunctionNode} {@link IComment} has been appended.
   * 
   * @param functionNode The {@link INaviFunctionNode} where the {@link IComment} has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedFunctionNodeComment(INaviFunctionNode functionNode, IComment comment);

  /**
   * Invoked after a new global {@link INaviCodeNode} {@link IComment} has been appended to a
   * {@link INaviCodeNode}.
   * 
   * @param codeNode The {@link INaviCodeNode} where the {@link IComment} has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedGlobalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after a new global edge {@link IComment} has been appended to an edge.
   * 
   * @param edge The edge where the {@link IComment} has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a new global function {@link IComment} has been appended to a function.
   * 
   * @param function The function where the {@link IComment} has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedGlobalFunctionComment(INaviFunction function, IComment comment);

  /**
   * Invoked after a new global instruction comment has been appended to an instruction.
   * 
   * @param instruction The instruction where the comment has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedGlobalInstructionComment(INaviInstruction instruction, IComment comment);

  /**
   * Invoked after a new group node comment has been appended to a group node.
   * 
   * @param groupNode The {@link INaviGroupNode} where the comment has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedGroupNodeComment(INaviGroupNode groupNode, IComment comment);

  /**
   * Invoked after a new local {@link INaviCodeNode} comment has been appended to a
   * {@link INaviCodeNode}.
   * 
   * @param codeNode The {@link INaviCodeNode} where the comment has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedLocalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after a new local edge comment has been appended to an edge.
   * 
   * @param edge The edge where the comment has been appended to an edge.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a new local instruction comment has been appended to an instruction.
   * 
   * @param codeNode The {@link INaviCodeNode} where the commented instruction is located in.
   * @param instruction The {@link INaviInstruction} where the comment has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      IComment comment);

  /**
   * Invoked after a new text node comment has been appended to a text node.
   * 
   * @param textNode The {@link INaviTextNode} where the comment has been appended.
   * @param comment The {@link IComment} which has been appended.
   */
  void appendedTextNodeComment(INaviTextNode textNode, IComment comment);

  /**
   * Invoked after a new comment has been appended to a type instance.
   * 
   * @param instance The type instance where the comment has been appended to.
   * @param comment The comment that was appended.
   */
  void appendedTypeInstanceComment(TypeInstance instance, IComment comment);


  /**
   * Invoked after a new comment has been appended to a section.
   * 
   * @param section The section where the comment has been appended.
   * @param comment The comment that was appended.
   */
  void appendedSectionComment(Section section, IComment comment);

  /**
   * Invoked after a function node comment has been deleted from a function node.
   * 
   * @param functionNode The {@link INaviFunctionNode} where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedFunctionNodeComment(INaviFunctionNode functionNode, IComment comment);

  /**
   * Invoked after a global comment of a {@link INaviCodeNode} has been deleted.
   * 
   * @param codeNode The {@link INaviCodeNode} where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedGlobalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after a global comment of an edge has been deleted.
   * 
   * @param edge The edge where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a global comment of a function has been deleted.
   * 
   * @param function The function where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedGlobalFunctionComment(INaviFunction function, IComment comment);

  /**
   * Invoked after a global {@link IComment} of an instruction has been deleted.
   * 
   * @param instruction The instruction where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedGlobalInstructionComment(INaviInstruction instruction, IComment comment);

  /**
   * Invoked after a group node comment has been deleted from a group node.
   * 
   * @param groupNode The {@link INaviGroupNode} where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedGroupNodeComment(INaviGroupNode groupNode, IComment comment);

  /**
   * Invoked after a local comment of a {@link INaviCodeNode} has been deleted.
   * 
   * @param codeNode The {@link INaviCodeNode} to which the deleted comment did belong.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedLocalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after a local comment of an edge has been edited.
   * 
   * @param edge The edge where the comment has been edited.
   * @param comment The {@link IComment} which was edited.
   */
  void deletedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a local instruction comment has been deleted from an instruction.
   * 
   * @param codeNode The {@link INaviCodeNode} where the instruction is located in.
   * @param instruction The {@link INaviInstruction} where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      IComment comment);

  /**
   * Invoked after a text node comment has been deleted from a text node.
   * 
   * @param textNode The {@link INaviTextNode} where the comment has been deleted.
   * @param comment The {@link IComment} which has been deleted.
   */
  void deletedTextNodeComment(INaviTextNode textNode, IComment comment);

  /**
   * Invoked after a type instance comment has been deleted.
   * 
   * @param instance The type instance whose comment has been deleted.
   * @param comment The comment that has been deleted.
   */
  void deletedTypeInstanceComment(TypeInstance instance, IComment comment);

  /**
   * Invoked after a section comment has been deleted.
   * 
   * @param section The section whose comment has been deleted.
   * @param comment The comment that has been deleted.
   */
  void deletedSectionComment(Section section, IComment comment);

  /**
   * Invoked after a function node comment has been edited.
   * 
   * @param functionNode The {@link INaviFunctionNode} where the comment has been edited.
   * @param comment The {@link IComment} which has been edited.
   */
  void editedFunctionNodeComment(INaviFunctionNode functionNode, IComment comment);

  /**
   * Invoked after a global comment of a {@link INaviCodeNode} changed.
   * 
   * @param codeNode The {@link INaviCodeNode} whose global comment changed.
   * @param comment The {@link IComment} which has changed.
   */
  void editedGLobalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after a global comment of an edge changed.
   * 
   * @param edge The edge whose global comment changed.
   * @param comment The {@link IComment} which has changed.
   */
  void editedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a global comment of a function changed.
   * 
   * @param function The function whose global comment changed.
   * @param comment The {@link IComment} which has changed.
   */
  void editedGlobalFunctionComment(INaviFunction function, IComment comment);

  /**
   * Invoked after a global comment of an instruction changed.
   * 
   * @param instruction The instruction whose global comment changed.
   * @param comment The previous global comment.
   */
  void editedGlobalInstructionComment(INaviInstruction instruction, IComment comment);

  /**
   * Invoked after a group node comment has been edited.
   * 
   * @param groupNode The {@link INaviGroupNode} where the comment has been edited.
   * @param comment The {@link IComment} which has been edited.
   */
  void editedGroupNodeComment(INaviGroupNode groupNode, IComment comment);

  /**
   * Invoked after a local comment of a {@link INaviCodeNode} changed.
   * 
   * @param codeNode The {@link INaviCodeNode} whose local comment changed.
   * @param comment The {@link IComment} which has changed.
   */
  void editedLocalCodeNodeComment(INaviCodeNode codeNode, IComment comment);

  /**
   * Invoked after the local comment of an edge has been edited.
   * 
   * @param edge The edge where the comment was edited.
   * @param comment The edited comment.
   */
  void editedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a {@link INaviCodeNode} comment has been edited.
   * 
   * @param codeNode The {@link INaviCodeNode} where the {@link INaviInstruction} is located in.
   * @param instruction The {@link INaviInstruction} where the {@link IComment} has been edited.
   * @param comment The {@link IComment} which has been edited.
   */
  void editedLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      IComment comment);

  /**
   * Invoked after a text node comment has been edited.
   * 
   * @param textNode The {@link INaviTextNode} where the comment has been edited.
   * @param comment The {@link IComment} which has been edited.
   */
  void editedTextNodeComment(INaviTextNode textNode, IComment comment);

  /**
   * Invoked after a type instance comment has been edited.
   * 
   * @param instance The type instance whose comment has been edited.
   * @param comment The comment that has been edited.
   */
  void editedTypeInstanceComment(TypeInstance instance, IComment comment);

  /**
   * Invoked after a section comment has been edited.
   * 
   * @param section The section whose comment has beed edited.
   * @param comment The comment that has been edited.
   */
  void editedSectionComment(Section section, IComment comment);

  /**
   * Invoked after a {@link INaviFunctionNode} {@link List}<{@link IComment}> has been initialized.
   * 
   * @param functionNode The {@link INaviFunctionNode} where the comment has been initialized.
   * @param comments The {@link List}<{@link IComment}> with which it was initialized.
   */
  void initializedFunctionNodeComments(INaviFunctionNode functionNode, List<IComment> comments);

  /**
   * Invoked after the global comments of a {@link INaviCodeNode} have been initialized.
   * 
   * @param codeNode The {@link INaviCodeNode} where the comments have been initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the
   *        {@link INaviCodeNode} .
   */
  void initializedGlobalCodeNodeComments(INaviCodeNode codeNode, List<IComment> comments);

  /**
   * Invoked after the global comments of an edge have been initialized.
   * 
   * @param edge The edge where the comments have been initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the edge.
   */
  void initializedGlobalEdgeComments(INaviEdge edge, List<IComment> comments);

  /**
   * Invoked after the global comments of a function have been initialized.
   * 
   * @param function The function where the comments have been initialized.
   * @param comments The {@link List}<{@link IComment}>which are now associated with the edge.
   */
  void initializedGlobalFunctionComments(INaviFunction function, List<IComment> comments);

  /**
   * Invoked after the global comments of an instruction have been initialized.
   * 
   * @param instruction The instruction where the global comments have been initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the edge.
   */
  void initializedGlobalInstructionComments(INaviInstruction instruction, List<IComment> comments);

  /**
   * Invoked after a {@link INaviGroupNode} {@link List}<{@link IComment}> has been initialized.
   * 
   * @param groupNode The {@link INaviGroupNode} where the {@link List}<{@link IComment}> has been
   *        initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the
   *        {@link INaviGroupNode}.
   */
  void initializedGroupNodeComments(INaviGroupNode groupNode, List<IComment> comments);

  /**
   * Invoked after a {@link INaviCodeNode} {@link List}<{@link IComment}> has been initialized.
   * 
   * @param codeNode The {@link INaviCodeNode} where the {@link List}<{@link IComment}> has been
   *        initialized.
   * @param comments he {@link List}<{@link IComment}> which are now associated with the
   *        {@link INaviCodeNode}.
   */
  void initializedLocalCodeNodeComments(INaviCodeNode codeNode, List<IComment> comments);

  /**
   * Invoked after the local comment of an edge has been initialized.
   * 
   * @param edge The edge where the comment has been initialized.
   * @param comments The {@link IComment} with which the edge has been initialized.
   */
  void initializedLocalEdgeComments(INaviEdge edge, List<IComment> comments);

  /**
   * Invoked after the local {@link List}<{@link IComment}> of an instruction has been initialized.
   * 
   * @param codeNode The {@link INaviCodeNode} where the {@link INaviInstruction} is located in.
   * @param instruction The {@link INaviInstruction} where the {@link List}<{@link IComment}> has
   *        been initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the
   *        {@link INaviInstruction}.
   */
  void initializedLocalInstructionComments(INaviCodeNode codeNode, INaviInstruction instruction,
      List<IComment> comments);

  /**
   * Invoked after the local {@link List}<{@link IComment}> of an text node has been initialized.
   * 
   * @param textNode The {@link INaviTextNode} where the {@link List}<{@link IComment}> has been
   *        initialized.
   * @param comments The {@link List}<{@link IComment}> which are now associated with the
   *        {@link INaviTextNode}.
   */
  void initializedTextNodeComments(INaviTextNode textNode, List<IComment> comments);

  /**
   * Invoked after a type instance comment has been initialized.
   * 
   * @param instance The type instance whose comment has been initialized.
   * @param comments The comments that have been initialized.
   */
  void initializedTypeInstanceComment(TypeInstance instance, List<IComment> comments);

  /**
   * Invoked after a section comment has been initialized.
   * 
   * @param section The section whose comment has been initialized.
   * @param comments The comments that have been initialized.
   */
  void initializedSectionComments(Section section, List<IComment> comments);
}
