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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.TypeInstanceComments;

import java.util.List;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;

/**
 * Provides type instance specific comment access.
 */
public class TypeInstanceCommentAccessor implements ICommentAccessor {

  private final TypeInstance instance;
  private final TypeInstanceContainer instanceContainer;

  public TypeInstanceCommentAccessor(
      final TypeInstance instance, final TypeInstanceContainer instanceContainer) {
    this.instance = instance;
    this.instanceContainer = instanceContainer;
  }

  @Override
  public List<IComment> appendComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return instanceContainer.appendComment(instance, commentText);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    instanceContainer.deleteComment(instance, comment);
  }

  @Override
  public IComment editComment(final IComment comment, final String newCommentText)
      throws CouldntSaveDataException {
    return instanceContainer.editComment(instance, comment, newCommentText);
  }

  @Override
  public List<IComment> getComment() {
    return instanceContainer.getComments(instance);
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return instanceContainer.isOwner(comment);
  }
}