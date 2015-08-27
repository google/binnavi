/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Importers;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File filter for selecting IDB files.
 */
public final class CIdbFileFilter extends FileFilter {
  @Override
  public boolean accept(final File file) {
    if (file.isDirectory()) {
      return true;
    }

    final String filenameLower = file.getName().toLowerCase();
    if (filenameLower.endsWith(".idb") || filenameLower.endsWith(".i64")) {
      return true;
    }

    return false;
  }

  @Override
  public String getDescription() {
    return "IDA Pro Database File (idb, i64)";
  }
}
