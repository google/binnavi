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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;



/**
 * Model that encapsulates the results of a special instruction search.
 */
public final class CSpecialInstructionsModel {
  /**
   * List of special instructions found in the search.
   */
  private final List<CSpecialInstruction> m_instructions = new ArrayList<CSpecialInstruction>();

  /**
   * Listeners that are notified about changes in the model.
   */
  private final ListenerProvider<ISpecialInstructionsModelListener> m_listeners =
      new ListenerProvider<ISpecialInstructionsModelListener>();

  /**
   * List of available special instruction descriptions.
   */
  private final List<ITypeDescription> m_descriptions = new ArrayList<ITypeDescription>();

  /**
   * Creates a new model object.
   */
  public CSpecialInstructionsModel() {
    m_descriptions.add(new CCallsDescription());
    m_descriptions.add(new CReadsDescription());
    m_descriptions.add(new CWritesDescription());
  }

  /**
   * Adds a listener object that is notified about changes in the model.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ISpecialInstructionsModelListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the available special instruction descriptions.
   *
   * @return The available special instruction descriptions.
   */
  public List<ITypeDescription> getDescriptions() {
    return new ArrayList<ITypeDescription>(m_descriptions);
  }

  /**
   * Returns the search result with the given index.
   *
   * @param index The index of the search result.
   *
   * @return The search result with the given index.
   */
  public CSpecialInstruction getInstruction(final int index) {
    return m_instructions.get(index);
  }

  /**
   * Returns all search results.
   *
   * @return All search results.
   */
  public List<CSpecialInstruction> getInstructions() {
    return new ArrayList<CSpecialInstruction>(m_instructions);
  }

  /**
   * Removes a listener that was previously listening on the model.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final ISpecialInstructionsModelListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Updates the search results.
   *
   * @param instructions The new search result.
   */
  public void setInstructions(final List<CSpecialInstruction> instructions) {
    m_instructions.clear();
    m_instructions.addAll(instructions);

    for (final ISpecialInstructionsModelListener listener : m_listeners) {
      try {
        listener.changedInstructions();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
