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
package com.google.security.zynamics.binnavi.Tagging;


import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Interface that must be implemented by all objects that want to be notified about changed in tag
 * managers.
 */
public interface ITagManagerListener {
  /**
   * Invoked after a tag was added to the manager.
   * 
   * @param manager The tag manager the tag was added to.
   * @param tag The new tag.
   */
  void addedTag(CTagManager manager, ITreeNode<CTag> tag);

  /**
   * Invoked after a tag was deleted from the tag manager.
   * 
   * Note that usage of the deleted tag after this method was invoked leads to undefined behavior.
   * 
   * @param manager The tag manager from which the tag was removed.
   * @param tag The tag that was removed from the tag manager.
   * @param parent The parent tag.
   */
  void deletedTag(CTagManager manager, ITreeNode<CTag> parent, ITreeNode<CTag> tag);

  /**
   * Invoked after a whole subtree of a tag manager's tag tree was deleted.
   * 
   * Note that using any of the deleted tags after this method was invoked leads to undefined
   * behavior.
   * 
   * @param manager The tag manager from which the subtree was removed.
   * @param parent The parent tag.
   * @param tag The root tag of the subtree that was removed from the tag manager.
   */
  void deletedTagSubtree(CTagManager manager, ITreeNode<CTag> parent, ITreeNode<CTag> tag);

  /**
   * Invoked after a new tag was inserted into a tag manager.
   * 
   * @param manager The tag manager where the new tag was inserted.
   * @param parent The parent tag of the new tag.
   * @param tag The tag that was added to the tat manager.
   */
  void insertedTag(CTagManager manager, ITreeNode<CTag> parent, ITreeNode<CTag> tag);

}
