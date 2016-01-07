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
 * This interface must be implemented by all classes that want to be notified about changes in
 * memory bookmarks.
 */
public interface IBookmarkListener {
  /**
   * Invoked after the description of a bookmark changed.
   *
   * @param bookmark The bookmark whose description changed.
   * @param description The new description of the bookmark.
   */
  void changedDescription(CBookmark bookmark, String description);
}
