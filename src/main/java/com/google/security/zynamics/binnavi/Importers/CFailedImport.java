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
package com.google.security.zynamics.binnavi.Importers;

/**
 * Represents an IDB file which failed to import into the database and the corresponding exception.
 */
public class CFailedImport {
  private final String m_fileName;
  private final ImportFailedException m_importException;

  public CFailedImport(final String fileName, final ImportFailedException importException) {
    m_fileName = fileName;
    m_importException = importException;
  }

  public String geFileName() {
    return m_fileName;
  }

  public ImportFailedException getImportException() {
    return m_importException;
  }
}
