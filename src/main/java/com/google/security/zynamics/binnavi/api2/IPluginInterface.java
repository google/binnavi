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
package com.google.security.zynamics.binnavi.api2;

import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.gui.MainWindow;

// ! Interface for plugin collections.
/**
 * Interface to be implemented by classes that want to be plugin interfaces.
 */
public interface IPluginInterface {
  // ! Returns all created plugins.
  /**
   * Returns the global database manager API object.
   * 
   * @return The global database manager API object.
   */
  DatabaseManager getDatabaseManager();

  MainWindow getMainWindow();
}
