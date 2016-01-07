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
package com.google.security.zynamics.binnavi.API.debug;

// / Adapter class for bookmark managers
/**
 * Adapter class that can be used by objects that want to listen on bookmark managers but only need
 * to process few events.
 */
public class BookmarkManagerListenerAdapter implements IBookmarkManagerListener {
  @Override
  public void addedBookmark(final BookmarkManager manager, final Bookmark bookmark) {
    // Empty default implementation
  }

  @Override
  public void removedBookmark(final BookmarkManager manager, final Bookmark bookmark) {
    // Empty default implementation
  }
}
