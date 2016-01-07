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

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;

/**
 * Implements the comment accessor functionality for global function comments.
 * 
 * @author timkornau
 * 
 */
public class CGlobalFunctionCommentAccessor implements ICommentAccessor {
  private final INaviFunction m_function;

  /**
   * Generates a new {@link CGlobalFunctionCommentAccessor} object.
   * 
   * @param function The function to access comments on.
   */
  public CGlobalFunctionCommentAccessor(final INaviFunction function) {
    m_function = Preconditions.checkNotNull(function, "IE02665: function argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02666: commentText argument can not be null");
    return m_function.appendGlobalComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02667: commentId argument can not be null");
    m_function.deleteGlobalComment(comment);
  }

  @Override
  public IComment editComment(final IComment commentId, final String newComment)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(commentId, "IE02668: commentId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02669: newComment argument can not be null");
    return m_function.editGlobalComment(commentId, newComment);
  }

  @Override
  public List<IComment> getComment() {
    return m_function.getGlobalComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    Preconditions.checkNotNull(comment, "IE02670: comment argument can not be null");
    return m_function.isOwner(comment);
  }
}
