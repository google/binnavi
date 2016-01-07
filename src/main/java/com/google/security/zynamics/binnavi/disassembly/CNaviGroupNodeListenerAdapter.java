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
package com.google.security.zynamics.binnavi.disassembly;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


/**
 * Adapter class for objects that want to listen on just a few group node events.
 */
public class CNaviGroupNodeListenerAdapter implements INaviGroupNodeListener {
  @Override
  public void addedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
    // Empty default implementation
  }

  @Override
  public void appendedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void changedState(final INaviGroupNode node) {
    // Empty default implementation
  }

  @Override
  public void deletedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void editedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void initializedGroupNodeComment(final INaviGroupNode node, final List<IComment> comment) {
    // Empty default implementation
  }

  @Override
  public void removedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
    // Empty default implementation
  }
}
