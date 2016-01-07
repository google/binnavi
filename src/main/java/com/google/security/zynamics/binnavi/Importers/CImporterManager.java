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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;


/**
 * Manages IDB importing operations.
 */
public final class CImporterManager {
  /**
   * The only valid instance of this class.
   */
  private static CImporterManager m_instance = new CImporterManager();

  /**
   * Currently known imports.
   */
  private final Map<IDatabase, List<String>> m_imports = new HashMap<IDatabase, List<String>>();

  /**
   * Returns the only valid instance of this class.
   *
   * @return The only valid importer manager instance.
   */
  public static CImporterManager instance() {
    return m_instance;
  }

  /**
   * Tells the manager that an import operation finished.
   *
   * @param database Target of the import operation.
   * @param idb Path of the IDB file that was imported.
   */
  public void finishImporting(final IDatabase database, final String idb) {
    m_imports.get(database).remove(idb);
  }

  /**
   * Tells the importer manager that a new import process beings.
   *
   * @param database The import target.
   * @param idb Path to the IDB file to import.
   */
  public void startImporting(final IDatabase database, final String idb) {
    if (!m_imports.containsKey(database)) {
      m_imports.put(database, new ArrayList<String>());
    }

    m_imports.get(database).add(idb);
  }
}
