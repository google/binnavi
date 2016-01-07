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

/**
 * Implements the comment accessor functionality for local edge comments.
 * 
 * @author timkornau
 * 
 */
public class CLocalEdgeCommentAccessor implements ICommentAccessor {
  private final INaviEdge m_edge;

  /**
   * Generates a new {@link CLocalEdgeCommentAccessor} object.
   * 
   * @param edge The edge to access comments on.
   */
  public CLocalEdgeCommentAccessor(final INaviEdge edge) {
    m_edge = Preconditions.checkNotNull(edge, "IE02659: edge argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String newComment) throws CouldntSaveDataException,
      CouldntLoadDataException {
    Preconditions.checkNotNull(newComment, "IE02660: newComment argument can not be null");
    return m_edge.appendLocalComment(newComment);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02661: comment argument can not be null");
    m_edge.deleteLocalComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newComment)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02662: comment argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02663: newComment argument can not be null");
    return m_edge.editLocalComment(comment, newComment);
  }

  @Override
  public List<IComment> getComment() {
    return m_edge.getLocalComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    Preconditions.checkNotNull(comment, "IE02664: comment argument can not be null");
    return m_edge.isOwner(comment);
  }
}
