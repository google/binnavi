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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EdgeComments;

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;

public class CGlobalEdgeCommentAccessor implements ICommentAccessor {
  private final INaviEdge m_edge;

  public CGlobalEdgeCommentAccessor(final INaviEdge edge) {
    m_edge = Preconditions.checkNotNull(edge, "IE02656: edge argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02657: commentText argument can not be null");
    return m_edge.appendGlobalComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02658: comment argument can not be null");
    m_edge.deleteGlobalComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newComment)
      throws CouldntSaveDataException {
    return m_edge.editGlobalComment(comment, newComment);
  }

  @Override
  public List<IComment> getComment() {
    return m_edge.getGlobalComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return m_edge.isOwner(comment);
  }
}
