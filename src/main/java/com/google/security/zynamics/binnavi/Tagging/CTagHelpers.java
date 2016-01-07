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

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.trees.BreadthFirstSorter;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Contains helper functions for working with tags.
 */
public final class CTagHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTagHelpers() {
  }

  /**
   * Searches for a tag with the given ID.
   * 
   * @param rootTag The tag where the search begins.
   * @param tagId The ID of the tag to search for.
   * 
   * @return The tag with the given ID or null if no such tag exists.
   */
  public static CTag findTag(final ITreeNode<CTag> rootTag, final int tagId) {
    for (final ITreeNode<CTag> c : BreadthFirstSorter.getSortedList(rootTag)) {
      if (tagId == c.getObject().getId()) {
        return c.getObject();
      }
    }

    return null;
  }

  /**
   * Searches for the tags with a list of given IDs
   * 
   * @param rootTag The tag where the search begins.
   * @param tagIds The list of tag IDs to search for.
   * 
   * @return A list of tags with the given IDs. Note that there is not necessarily a 1:1
   *         correspondence between the input list and the output list.
   */
  public static Collection<CTag> findTags(final ITreeNode<CTag> rootTag,
      final Collection<Integer> tagIds) {
    Preconditions.checkNotNull(tagIds, "IE00866: List argument can't be null");

    final HashSet<CTag> tags = new HashSet<CTag>();

    for (final ITreeNode<CTag> c : BreadthFirstSorter.getSortedList(rootTag)) {
      if (tagIds.contains(c.getObject().getId())) {
        tags.add(c.getObject());
      }
    }

    return tags;
  }
}
