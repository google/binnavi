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

// / Used to listen on tag managers.
/**
 * This interface can be implemented by classes that want to be notified about changes in a tag
 * manager.
 */
public interface ITagTreeManagerListener {
  // ! Signals the creation of a new tag.
  /**
   * Invoked after a tag was added to the tag manager.
   *
   * @param manager The tag manager the tag was added to.
   * @param tag The tag that was added to the tag manager.
   */
  void addedTag(TagTreeManager manager, Tag tag);

  // ! Signals the deletion of a tag.
  /**
   * Invoked after a tag was deleted from the tag manager.
   *
   * @param manager The tag manager the tag was deleted from.
   * @param tag The tag that was deleted from the tag manager.
   */
  void deletedTag(TagTreeManager manager, Tag tag);

  // ! Signals the deletion of a tag and its child tags.
  /**
   * Invoked after a tag and all of its child tags was deleted from the tag manager.
   *
   * @param manager The tag manager the tags were deleted from.
   * @param tag The parent tag of the subtree that was deleted from the tag manager.
   */
  void deletedTagTree(TagTreeManager manager, Tag tag);

  // ! Signals the insertion of tag.
  /**
   * Invoked after a tag was inserted between existing tags in the tag manager.
   *
   * @param manager The tag manager the tag was inserted to.
   * @param parentTag The parent tag after which the tag was inserted.
   * @param tag The tag that was inserted into the tag manager.
   */
  void insertedTag(TagTreeManager manager, Tag parentTag, Tag tag);
}
