/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentListener;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;


public class MockCommentListener implements CommentListener {

  public ArrayList<String> listenerMessages = new ArrayList<String>();

  public final static String APPENDED_FUNCTION_NODE_COMMENT = "appendedFunctionNodeComment";
  public final static String APPENDED_GLOBAL_CODE_NODE_COMMENT = "appendedGlobalCodeNodeComment";
  public final static String APPENDED_GLOBAL_EDGE_COMMENT = "appendedGlobalEdgeComment";
  public final static String APPENDED_GLOBAL_FUNCTION_COMMENT = "appendedGlobalFunctionComment";
  public final static String APPENDED_GLOBAL_INSTRUCTION_COMMENT =
      "appendedGlobalInstructionComment";
  public final static String APPENDED_GROUP_NODE_COMMENT = "appendedGroupNodeComment";
  public final static String APPENDED_LOCAL_CODE_NODE_COMMENT = "appendedLocalCodeNodeComment";
  public final static String APPENDED_LOCAL_EDGE_COMMENT = "appendedLocalEdgeComment";
  public final static String APPENDED_LOCAL_INSTRUCTION_COMMENT = "appendedLocalInstructionComment";
  public final static String APPENDED_TEXT_NODE_COMMENT = "appendedTextNodeComment";
  public final static String APPENDED_TYPE_INSTANCE_COMMENT = "appendedTypeInstanceComment";
  public final static String APPENDED_SECTION_COMMENT = "appendedSectionComment";

  public final static String DELETED_FUNCTION_NODE_COMMENT = "deletedFunctionNodeComment";
  public final static String DELETED_GLOBAL_CODE_NODE_COMMENT = "deletedGlobalCodeNodeComment";
  public final static String DELETED_GLOBAL_EDGE_COMMENT = "deletedGlobalEdgeComment";
  public final static String DELETED_GLOBAL_FUNCTION_COMMENT = "deletedGlobalFunctionComment";
  public final static String DELETED_GLOBAL_INSTRUCTION_COMMENT = "deletedGlobalInstructionComment";
  public final static String DELETED_GROUP_NODE_COMMENT = "deletedGroupNodeComment";
  public final static String DELETED_LOCAL_CODE_NODE_COMMENT = "deletedLocalCodeNodeComment";
  public final static String DELETED_LOCAL_EDGE_COMMENT = "deletedLocalEdgeComment";
  public final static String DELETED_LOCAL_INSTRUCTION_COMMENT = "deletedLocalInstructionComment";
  public final static String DELETED_TEXT_NODE_COMMENT = "deletedTextNodeComment";
  public final static String DELETED_TYPE_INSTANCE_COMMENT = "deletedTypeInstanceComment";
  public final static String DELETED_SECTION_COMMENT = "deletedSectionComment";

  public final static String EDITED_FUNCTION_NODE_COMMENT = "editedFunctionNodeComment";
  public final static String EDITED_GLOBAL_CODE_NODE_COMMENT = "editedGlobalCodeNodeComment";
  public final static String EDITED_GLOBAL_EDGE_COMMENT = "editedGlobalEdgeComment";
  public final static String EDITED_GLOBAL_FUNCTION_COMMENT = "editedGlobalFunctionComment";
  public final static String EDITED_GLOBAL_INSTRUCTION_COMMENT = "editedGlobalInstructionComment";
  public final static String EDITED_GROUP_NODE_COMMENT = "editedGroupNodeComment";
  public final static String EDITED_LOCAL_CODE_NODE_COMMENT = "editedLocalCodeNodeComment";
  public final static String EDITED_LOCAL_EDGE_COMMENT = "editedLocalEdgeComment";
  public final static String EDITED_LOCAL_INSTRUCTION_COMMENT = "editedLocalInstructionComment";
  public final static String EDITED_TEXT_NODE_COMMENT = "editedTextNodeComment";
  public final static String EDITED_TYPE_INSTANCE_COMMENT = "editedTypeInstanceComment";
  public final static String EDITED_SECTION_COMMENT = "editedSectionComment";

  public final static String INIT_FUNCTION_NODE_COMMENT = "initFunctionNodeComment";
  public final static String INIT_GLOBAL_CODE_NODE_COMMENT = "initGlobalCodeNodeComment";
  public final static String INIT_GLOBAL_EDGE_COMMENT = "initGlobalEdgeComment";
  public final static String INIT_GLOBAL_FUNCTION_COMMENT = "initGlobalFunctionComment";
  public final static String INIT_GLOBAL_INSTRUCTION_COMMENT = "initGlobalInstructionComment";
  public final static String INIT_GROUP_NODE_COMMENT = "initGroupNodeComment";
  public final static String INIT_LOCAL_CODE_NODE_COMMENT = "initLocalCodeNodeComment";
  public final static String INIT_LOCAL_EDGE_COMMENT = "initLocalEdgeComment";
  public final static String INIT_LOCAL_INSTRUCTION_COMMENT = "initdLocalInstructionComment";
  public final static String INIT_TEXT_NODE_COMMENT = "initTextNodeComment";
  public final static String INIT_TYPE_INSTANCE_COMMENT = "initTypeInstanceComment";
  public final static String INIT_SECTION_COMMENT = "initSectionComment";

  @Override
  public void appendedFunctionNodeComment(final INaviFunctionNode functionNode,
      final IComment comment) {
    listenerMessages.add(APPENDED_FUNCTION_NODE_COMMENT);
  }

  @Override
  public void appendedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(APPENDED_GLOBAL_CODE_NODE_COMMENT);
  }

  @Override
  public void appendedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(APPENDED_GLOBAL_EDGE_COMMENT);
  }

  @Override
  public void appendedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
    listenerMessages.add(APPENDED_GLOBAL_FUNCTION_COMMENT);
  }

  @Override
  public void appendedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
    listenerMessages.add(APPENDED_GLOBAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void appendedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
    listenerMessages.add(APPENDED_GROUP_NODE_COMMENT);
  }

  @Override
  public void appendedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(APPENDED_LOCAL_CODE_NODE_COMMENT);
  }

  @Override
  public void appendedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(APPENDED_LOCAL_EDGE_COMMENT);
  }

  @Override
  public void appendedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
    listenerMessages.add(APPENDED_LOCAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void appendedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
    listenerMessages.add(APPENDED_TEXT_NODE_COMMENT);
  }

  @Override
  public void appendedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
    listenerMessages.add(APPENDED_TYPE_INSTANCE_COMMENT);
  }

  @Override
  public void deletedFunctionNodeComment(final INaviFunctionNode functionNode,
      final IComment comment) {
    listenerMessages.add(DELETED_FUNCTION_NODE_COMMENT);
  }

  @Override
  public void deletedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(DELETED_GLOBAL_CODE_NODE_COMMENT);
  }

  @Override
  public void deletedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(DELETED_GLOBAL_EDGE_COMMENT);
  }

  @Override
  public void deletedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
    listenerMessages.add(DELETED_GLOBAL_FUNCTION_COMMENT);
  }

  @Override
  public void deletedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
    listenerMessages.add(DELETED_GLOBAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void deletedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
    listenerMessages.add(DELETED_GROUP_NODE_COMMENT);
  }

  @Override
  public void deletedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(DELETED_LOCAL_CODE_NODE_COMMENT);
  }

  @Override
  public void deletedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(DELETED_LOCAL_EDGE_COMMENT);
  }

  @Override
  public void deletedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
    listenerMessages.add(DELETED_LOCAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void deletedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
    listenerMessages.add(DELETED_TEXT_NODE_COMMENT);
  }

  @Override
  public void deletedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
    listenerMessages.add(DELETED_TYPE_INSTANCE_COMMENT);
  }

  @Override
  public void editedFunctionNodeComment(final INaviFunctionNode functionNode,
      final IComment comment) {
    listenerMessages.add(EDITED_FUNCTION_NODE_COMMENT);
  }

  @Override
  public void editedGLobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(EDITED_GLOBAL_CODE_NODE_COMMENT);
  }

  @Override
  public void editedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(EDITED_GLOBAL_EDGE_COMMENT);
  }

  @Override
  public void editedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
    listenerMessages.add(EDITED_GLOBAL_FUNCTION_COMMENT);
  }

  @Override
  public void editedGlobalInstructionComment(final INaviInstruction instruction,
      final IComment comment) {
    listenerMessages.add(EDITED_GLOBAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void editedGroupNodeComment(final INaviGroupNode groupNode, final IComment comment) {
    listenerMessages.add(EDITED_GROUP_NODE_COMMENT);
  }

  @Override
  public void editedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {
    listenerMessages.add(EDITED_LOCAL_CODE_NODE_COMMENT);
  }

  @Override
  public void editedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    listenerMessages.add(EDITED_LOCAL_EDGE_COMMENT);
  }

  @Override
  public void editedLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final IComment comment) {
    listenerMessages.add(EDITED_LOCAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void editedTextNodeComment(final INaviTextNode textNode, final IComment comment) {
    listenerMessages.add(EDITED_TEXT_NODE_COMMENT);
  }

  @Override
  public void editedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
    listenerMessages.add(EDITED_TYPE_INSTANCE_COMMENT);
  }

  @Override
  public void initializedFunctionNodeComments(final INaviFunctionNode functionNode,
      final List<IComment> comments) {
    listenerMessages.add(INIT_FUNCTION_NODE_COMMENT);
  }

  @Override
  public void initializedGlobalCodeNodeComments(final INaviCodeNode codeNode,
      final List<IComment> comments) {
    listenerMessages.add(INIT_GLOBAL_CODE_NODE_COMMENT);
  }

  @Override
  public void initializedGlobalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
    listenerMessages.add(INIT_GLOBAL_EDGE_COMMENT);
  }

  @Override
  public void initializedGlobalFunctionComments(final INaviFunction function,
      final List<IComment> comments) {
    listenerMessages.add(INIT_GLOBAL_FUNCTION_COMMENT);
  }

  @Override
  public void initializedGlobalInstructionComments(final INaviInstruction instruction,
      final List<IComment> comments) {
    listenerMessages.add(INIT_GLOBAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void initializedGroupNodeComments(final INaviGroupNode groupNode,
      final List<IComment> comments) {
    listenerMessages.add(INIT_GROUP_NODE_COMMENT);
  }

  @Override
  public void initializedLocalCodeNodeComments(final INaviCodeNode codeNode,
      final List<IComment> comments) {
    listenerMessages.add(INIT_LOCAL_CODE_NODE_COMMENT);
  }

  @Override
  public void initializedLocalEdgeComments(final INaviEdge edge, final List<IComment> comments) {
    listenerMessages.add(INIT_LOCAL_EDGE_COMMENT);
  }

  @Override
  public void initializedLocalInstructionComments(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final List<IComment> comments) {
    listenerMessages.add(INIT_LOCAL_INSTRUCTION_COMMENT);
  }

  @Override
  public void initializedTextNodeComments(final INaviTextNode textNode,
      final List<IComment> comments) {
    listenerMessages.add(INIT_TEXT_NODE_COMMENT);
  }

  @Override
  public void initializedTypeInstanceComment(final TypeInstance instance,
      final List<IComment> comments) {
    listenerMessages.add(INIT_TYPE_INSTANCE_COMMENT);
  }

  @Override
  public void appendedSectionComment(final Section section, final IComment comment) {
    listenerMessages.add(APPENDED_SECTION_COMMENT);
  }

  @Override
  public void deletedSectionComment(final Section section, final IComment comment) {
    listenerMessages.add(DELETED_SECTION_COMMENT);
  }

  @Override
  public void editedSectionComment(final Section section, final IComment comment) {
    listenerMessages.add(EDITED_SECTION_COMMENT);
  }

  @Override
  public void initializedSectionComments(final Section section, final List<IComment> comments) {
    listenerMessages.add(INIT_SECTION_COMMENT);
  }
}
