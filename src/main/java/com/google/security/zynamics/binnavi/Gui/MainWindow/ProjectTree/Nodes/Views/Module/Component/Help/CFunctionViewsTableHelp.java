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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Help;



import java.net.URL;

import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;


/**
 * Context-sensitive help information for tables that show function properties.
 */
public final class CFunctionViewsTableHelp implements IHelpInformation {
  @Override
  public String getText() {
    return "This table shows functions and their properties.\n\nAddress: Start address of the function\nName: Name of the function\nDescription: Description of the function\nModule: Name of the module this function belongs to\nForwarded to: Imported function this function is forwarded to\nBasic Blocks: Number of basic blocks in the function\nEdges: Number of edges in the view\nIn: Number of functions that call this function\nOut: Number of functions called by this function";
  }

  @Override
  public URL getUrl() {
    return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
  }
}
