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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import java.net.URL;

import javax.swing.JTable;

import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;


/**
 * Table that shows the calling functions of a view.
 */
public final class CCrossReferencesTable extends JTable implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2116641450877085547L;

  /**
   * Creates a new cross references table object.
   *
   * @param model The table model.
   */
  public CCrossReferencesTable(final CCrossReferencesModel model) {
    super(model);
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "This table shows all the functions that call the function shown in the view. The functions on the left-hand side of the table are called by the functions on the right-hand side of the table.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }
}
