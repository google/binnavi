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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.GroupNodeComments;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;

public class GroupNodeCommentAccessor implements ICommentAccessor {

  final INaviGroupNode groupNode;

  public GroupNodeCommentAccessor(final INaviGroupNode node) {
    groupNode = Preconditions.checkNotNull(node, "IE02680: node argument can not be null");
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    return groupNode.appendComment(commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02681: comment argument can not be null");
    groupNode.deleteComment(comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newCommentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02682: comment argument can not be null");
    Preconditions.checkNotNull(newCommentText, "IE02683: newCommentText argument can not be null");
    groupNode.editComment(comment, newCommentText);
    return null;
  }

  @Override
  public List<IComment> getComment() {
    return groupNode.getComments();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return groupNode.isOwner(comment);
  }
}
