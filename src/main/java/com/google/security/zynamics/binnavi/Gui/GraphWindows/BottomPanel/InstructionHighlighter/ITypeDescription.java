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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.Color;
import java.util.Collection;


import com.google.common.collect.ListMultimap;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;


/**
 * Interface for classes that want to describe special instructions.
 */
public interface ITypeDescription {
  /**
   * Adds a listener object that is notified about changes in the description.
   * 
   * @param listener The listener object to add.
   */
  void addListener(ITypeDescriptionListener listener);

  /**
   * Returns the color used to highlight instructions of this type.
   * 
   * @return The color used to highlight.
   */
  Color getColor();

  /**
   * Returns the description of instructions of this type.
   * 
   * @return The description string.
   */
  String getDescription();

  /**
   * Returns the hint to show for instructions of this type in the options panel.
   * 
   * @return The hint to show.
   */
  String getHint();

  /**
   * Flag that says whether instructions of this type are included in the current search results.
   * 
   * @return True, if instructions of this type are included in the current results.
   */
  boolean isEnabled();

  /**
   * Removes a previously listening listener object.
   * 
   * @param listener The listener object to remove.
   */
  void removeListener(ITypeDescriptionListener listener);

  /**
   * Changes the color used to highlight instructions of this type.
   * 
   * @param color The new color.
   */
  void setColor(Color color);

  /**
   * Includes or excludes instructions of this type from the search results.
   * 
   * @param enabled True, to include the instructions. False, to exclude them.
   */
  void setEnabled(boolean enabled);

  /**
   * Determines all instructions of this type.
   * 
   * @param reilCode The REIL code to search through.
   * @param m_instructionMap Maps addresses to instructions.
   * 
   * @return The instructions of this type that were found in the REIL code.
   */
  Collection<CSpecialInstruction> visit(ReilFunction reilCode,
      ListMultimap<IAddress, INaviInstruction> m_instructionMap);
}
