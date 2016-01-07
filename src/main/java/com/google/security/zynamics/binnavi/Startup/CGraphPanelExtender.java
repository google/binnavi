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
package com.google.security.zynamics.binnavi.Startup;

import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.CCodeBookmarkExtensionCreator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences.CCrossReferenceExtensionCreator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter.CInstructionHighlighterExtensionCreator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CRegisterTrackingExtensionCreator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences.CVariablesExtensionCreator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.CAbstractGraphPanelExtensionFactory;

/**
 * Contains code for extending the bottom panel of graph windows.
 */
public final class CGraphPanelExtender {
  /**
   * You are not suppoed to instantiate this class.
   */
  private CGraphPanelExtender() {
  }

  /**
   * Registers extension objects.
   */
  public static void extend() {
    CAbstractGraphPanelExtensionFactory.register(new CVariablesExtensionCreator());
    CAbstractGraphPanelExtensionFactory.register(new CCrossReferenceExtensionCreator());
    CAbstractGraphPanelExtensionFactory.register(new CRegisterTrackingExtensionCreator());
    CAbstractGraphPanelExtensionFactory.register(new CInstructionHighlighterExtensionCreator());
    CAbstractGraphPanelExtensionFactory.register(new CCodeBookmarkExtensionCreator());
    // CAbstractGraphPanelExtensionFactory.register(new CRangeTrackingExtensionCreator());

  }
}
