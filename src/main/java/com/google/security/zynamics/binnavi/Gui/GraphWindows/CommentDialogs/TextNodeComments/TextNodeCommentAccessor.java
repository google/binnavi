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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.TextNodeComments;

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;

public class TextNodeCommentAccessor implements ICommentAccessor {

  private final INaviTextNode m_textNode;

  public TextNodeCommentAccessor(final INaviTextNode textNode) {
    m_textNode = Preconditions.checkNotNull(textNode, "IE02713: textNode argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    return m_textNode.appendComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    m_textNode.deleteComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    return m_textNode.editComment(comment, commentText);
  }

  @Override
  public List<IComment> getComment() {
    return m_textNode.getComments();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return m_textNode.isOwner(comment);
  }
}
