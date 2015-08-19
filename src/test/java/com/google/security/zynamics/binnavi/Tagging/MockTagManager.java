/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Tagging;


import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

public final class MockTagManager implements ITagManager {
  private final TagType m_type;

  private final ListenerProvider<ITagManagerListener> m_listeners =
      new ListenerProvider<ITagManagerListener>();

  private final TreeNode<CTag> m_root;

  public MockTagManager(final TagType type) {
    m_type = type;
    m_root = new TreeNode<CTag>(new CTag(0, "Hannes", "", m_type, new MockSqlProvider()));
  }

  @Override
  public void addListener(final ITagManagerListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public ITreeNode<CTag> addTag(final ITreeNode<CTag> parent, final String name) {
    final TreeNode<CTag> node =
        new TreeNode<CTag>(new CTag(1, name, "", m_type, new MockSqlProvider()));

    parent.addChild(node);
    node.setParent(parent);

    return node;
  }

  @Override
  public void deleteTag(final ITreeNode<CTag> tag) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteTagSubTree(final ITreeNode<CTag> tag) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ITreeNode<CTag> getRootTag() {
    return m_root;
  }

  @Override
  public ITreeNode<CTag> insertTag(final ITreeNode<CTag> parent, final String name) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void moveTag(final ITreeNode<CTag> parent, final ITreeNode<CTag> child) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final ITagManagerListener listener) {
    m_listeners.removeListener(listener);
  }
}
