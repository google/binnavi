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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser;

import java.io.File;

import com.google.common.base.Preconditions;

/**
 * Contains helper functions for remote browsing.
 */
public final class CRemoteBrowserHelpers {
  /**
   * Do not create instances of this class.
   */
  private CRemoteBrowserHelpers() {
    // You are not supposed to instantiate this class
  }

  /**
   * Determines whether a given file is a drive or not.
   *
   * @param file The file in question.
   *
   * @return True, if the file is a drive. False, otherwise.
   */
  public static boolean isDrive(final File file) {
    Preconditions.checkNotNull(file, "IE01493: File argument can not be null");

    return file.getParent() == null;
  }
}
