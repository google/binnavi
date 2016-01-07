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
package com.google.security.zynamics.binnavi.API.plugins;

import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

// / Used to write batch plugins.
/**
 * Plugins that implement this interface can be run in batch mode from the BinNavi command line.
 */
public interface IBatchPlugin extends IPlugin<IPluginInterface> {
  // ! Invoked to execute the plugin.
  /**
   * Invoked to execute the plugin.
   */
  void run();
}
