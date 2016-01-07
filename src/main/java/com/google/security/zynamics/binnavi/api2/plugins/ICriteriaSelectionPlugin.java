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
package com.google.security.zynamics.binnavi.api2.plugins;

import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;

import javax.swing.JPanel;

// ! Interface for plugins that extend the Select by Criteria dialog.
/**
 * This interface can be implemented by plugins that want to extend the Select by Criteria dialog in
 * graph windows.
 * 
 * The general workflow of Select by Criteria plugins is this:
 * 
 * - Whenever a Select by Criteria dialog is opened, the plugin is added to the list of potential
 * criteria. - When the user chooses the plugin criterium, getCriterium is called and a new
 * ICriterium object must be returned. The user can configure the ICriterium object using the JPanel
 * object returned by getCriteriumPanel. - When the selection action is executed, the ICriterium
 * object is asked to create an IFixedCriterium object that contains all the configuration option
 * from the configuration panel in a non-modifiable state. This fixed criterium object is then user
 * to re-evaluate the selection action through the recently used criteria menus.
 */
public interface ICriteriaSelectionPlugin extends IPlugin<IPluginInterface> {
  // ! Creates a new criterium.
  /**
   * Creates a new criterium object.
   * 
   * @param view2d View2D object that contains the nodes to be selected.
   * 
   * @return The created criterium object.
   */
  ICriterium getCriterium(View2D view2d);

  // ! Criterium description string.
  /**
   * Returns a description string of the criterium.
   * 
   * @return The criterium description string.
   */
  String getCriteriumDescription();

  // ! Interface for concrete plugin criteria.
  /**
   * This interface must be implemented by all objects that are returned from the getCriterium
   * method of the ICriteriaSelectionPlugin interface.
   */
  interface ICriterium {
    // ! The criterium description.
    /**
     * Returns the description of the selection criterium.
     * 
     * @return The description of the selection criterium.
     */
    String getCriteriumDescription();

    // ! The criterium configuration panel.
    /**
     * Returns the criterium configuration panel where the criterium can be configured.
     * 
     * @return The criterium configuration panel.
     */
    JPanel getCriteriumPanel();

    // ! Returns a fixed input value object.
    /**
     * Returns an object that contains all configuration values from the configuration panel of the
     * criterium. This object is later on passed to matches(ViewNode, Object) to replay previous
     * node selections.
     * 
     * @return An object that contains all configuration values.
     */
    IFixedCriterium getFixedCriterium();

    // ! The criterium formula string.
    /**
     * Returns the criterium formula string that is a textual representation of the criterium.
     * 
     * @return The formula string.
     */
    String getFormulaString();

    // ! Evaluates the criterium.
    /**
     * Evaluates whether the criterium matches the given node.
     * 
     * @param node The node to match.
     * 
     * @return True, if the criterium matches the node. False, otherwise.
     */
    boolean matches(ViewNode node);
  }

  // ! Interface for fixed criteriums.
  /**
   * This interface must be implemented by all objects that want to serve as fixed criteria during
   * Select by Criteria replay operations.
   */
  interface IFixedCriterium {
    /**
     * Evaluates whether the criterium matches the given node.
     * 
     * @param node The node to match.
     * 
     * @return True, if the criterium matches the node. False, otherwise.
     */
    boolean matches(ViewNode node);
  }
}
