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



import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

public final class MockTagManagerListener implements ITagManagerListener {
  public String eventList = "";

  @Override
  public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
    eventList += "addedTag/";
  }

  @Override
  public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
      final ITreeNode<CTag> tagt) {
    eventList += "deletedTag/";
  }

  @Override
  public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
      final ITreeNode<CTag> tag) {
    eventList += "deletedSubtree/";
  }

  @Override
  public void insertedTag(final CTagManager manager, final ITreeNode<CTag> parent,
      final ITreeNode<CTag> tag) {
    eventList += "insertedTag/";
  }
}
