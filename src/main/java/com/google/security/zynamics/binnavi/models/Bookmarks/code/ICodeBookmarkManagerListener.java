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
package com.google.security.zynamics.binnavi.models.Bookmarks.code;

/**
 * Interface to be implemented by objects that want to be notified about changes in code bookmark
 * managers.
 */
public interface ICodeBookmarkManagerListener {
  /**
   * Invoked after a bookmark was added to the bookmark manager.
   *
   * @param manager The manager where the bookmark was added.
   * @param bookmark The bookmark that was added to the manager.
   */
  void addedBookmark(CCodeBookmarkManager manager, CCodeBookmark bookmark);

  /**
   * Invoked after a bookmark was removed from the bookmark manager.
   *
   * @param manager The manager where the bookmark was removed.
   * @param bookmark The bookmark that was removed from the manager.
   */
  void removedBookmark(CCodeBookmarkManager manager, CCodeBookmark bookmark);
}
