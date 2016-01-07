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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.FunctionComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;

import java.util.List;

public class CLocalFunctionNodeCommentAccessor implements ICommentAccessor {

  private final INaviFunctionNode m_functionNode;

  public CLocalFunctionNodeCommentAccessor(final INaviFunctionNode functionNode) {
    m_functionNode =
        Preconditions.checkNotNull(functionNode, "IE02671: functionNode argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02672: commentText argument can not be null");
    return m_functionNode.appendLocalFunctionComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02673: comment argument can not be null");
    m_functionNode.deleteLocalFunctionComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newCommentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02674: comment argument can not be null");
    Preconditions.checkNotNull(newCommentText, "IE02675: newCommentText argument can not be null");
    return m_functionNode.editLocalFunctionComment(comment, newCommentText);
  }

  @Override
  public List<IComment> getComment() {
    return m_functionNode.getLocalFunctionComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return m_functionNode.isOwner(comment);
  }
}
