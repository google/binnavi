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

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;

/**
 * Implements the comment accessor functionality for global code node comments.
 * 
 * @author timkornau
 * 
 */
public class GlobalCodeNodeCommentAccessor implements ICommentAccessor {
  final INaviCodeNode m_codeNode;

  /**
   * Generates a new {@link GlobalCodeNodeCommentAccessor} object.
   * 
   * @param codeNode The code node to access comments on.
   */
  public GlobalCodeNodeCommentAccessor(final INaviCodeNode codeNode) {
    m_codeNode = Preconditions.checkNotNull(codeNode, "IE02638: codeNode argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02639: commentText argument can not be null");
    return m_codeNode.getComments().appendGlobalCodeNodeComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02640: comment argument can not be null");
    m_codeNode.getComments().deleteGlobalCodeNodeComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newComment)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02641: comment argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02642: newComment argument can not be null");
    return m_codeNode.getComments().editGlobalCodeNodeComment(comment, newComment);
  }

  @Override
  public List<IComment> getComment() {
    return m_codeNode.getComments().getGlobalCodeNodeComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    Preconditions.checkNotNull(comment, "IE02643: comment argument can not be null");
    return m_codeNode.isOwner(comment);
  }
}
