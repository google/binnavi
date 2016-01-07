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
package com.google.security.zynamics.binnavi.models.Bookmarks.memory;

/**
 * Interface that must be implemented by all classes that want to be notified about changes in the
 * active bookmarks.
 *
 */
public interface IBookmarkManagerListener {
  /**
   * Invoked after a bookmark was added to a bookmark manager.
   *
   * @param manager The bookmark manager the bookmark was added to.
   * @param bookmark The bookmark added to the bookmark manager.
   */
  void addedBookmark(BookmarkManager manager, CBookmark bookmark);

  /**
   * Invoked after a bookmark was removed from a bookmark manager.
   *
   * @param manager The manager from which the boomark was removed.
   * @param bookmark The bookmark that was removed from the bookmark manager.
   */
  void removedBookmark(BookmarkManager manager, CBookmark bookmark);
}
