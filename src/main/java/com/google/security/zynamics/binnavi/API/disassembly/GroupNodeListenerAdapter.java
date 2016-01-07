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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


// / Adapter class for group nodes
/**
 * Adapter class that can be used by objects that want to listen on group nodes but only need to
 * process few events.
 */
public class GroupNodeListenerAdapter implements IGroupNodeListener {
  @Override
  public void addedNode(final GroupNode groupNode, final ViewNode node) {
    // Empty default implementation
  }

  @Override
  public void appendedComment(final GroupNode groupNode, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void changedState(final GroupNode groupNode, final boolean collapsed) {
    // Empty default implementation
  }

  @Override
  public void deletedComment(final GroupNode groupNode, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void editedComment(final GroupNode groupNode, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void initializedComment(final GroupNode groupNode, final List<IComment> comment) {
    // Empty default implementation
  }

  @Override
  public void removedNode(final GroupNode groupNode, final ViewNode node) {
    // Empty default implementation
  }
}
