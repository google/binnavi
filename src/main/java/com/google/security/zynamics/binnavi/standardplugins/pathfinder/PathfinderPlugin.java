/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleListenerAdapter;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.helpers.IProgressThread;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.API.helpers.MessageBox;
import com.google.security.zynamics.binnavi.API.helpers.ProgressDialog;
import com.google.security.zynamics.binnavi.API.plugins.IModuleMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.standardplugins.utils.GuiHelper;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * The path finder plugin can be used to find all paths between two basic blocks of an executable
 * file.
 * 
 * The plugin is an IModuleMenuPlugin. This means that the plugin extends the context menu of module
 * nodes in the project tree.
 */
public final class PathfinderPlugin implements IModuleMenuPlugin {
  private IPluginInterface pluginInterface;

  /**
   * Shows the pathfinding dialog that is used to select the start block and the end block of the
   * pathfinding operation.
   * 
   * @param module The target module of the pathfinding operation.
   * @throws CouldntLoadDataException Thrown if one of the functions couldn't be loaded.
   */
  private void showPathfindingDialog(final Module module) throws CouldntLoadDataException {
    Preconditions.checkArgument(module.isLoaded(), "Internal Error: Target module is not loaded");

    final PathfindingDialog dlg =
        new PathfindingDialog(pluginInterface.getMainWindow().getFrame(), module);
    GuiHelper.centerChildToParent(pluginInterface.getMainWindow().getFrame(), dlg, true);
    dlg.setVisible(true);
    if (dlg.wasCancelled()) {
      return;
    }

    final BasicBlock sourceBlock = dlg.getStartBlock();
    final BasicBlock targetBlock = dlg.getEndBlock();
    final Function firstFunction = dlg.getStartFunction();
    final Function secondFunction = dlg.getEndFunction();

    // for every time when a user has not selected a function but a basic block this breaks.
    // As it does throw a null pointer exception.
    firstFunction.load();
    secondFunction.load();
    final CreationThread creationThread =
        new CreationThread(module, sourceBlock, targetBlock, firstFunction, secondFunction);

    ProgressDialog.show(pluginInterface.getMainWindow().getFrame(), "Creating path ...",
        creationThread);

    if ((!(creationThread.threwException())) && (creationThread.getCreatedView() == null)) {
      MessageBox.showInformation(pluginInterface.getMainWindow().getFrame(),
          "There is no path between the two selected blocks");
    } else {
      new Thread() {
        @Override
        public void run() {
          PluginInterface.instance().showInNewWindow(creationThread.getCreatedView());
        }
      }.start();
    }
  }

  @Override
  public List<JComponent> extendModuleMenu(final List<Module> modules) {
    // This function is used to extend the context menu of module
    // nodes in the project tree or in tables of the main window
    // where modules are listed.

    // The module list given as the parameter contains a list of modules.
    // In case the context menu of a module node is created, this list
    // contains exactly one module. In case the context menu of a
    // table is created, the list contains the corresponding modules of the selected
    // rows of the table.

    final List<JComponent> menus = new ArrayList<JComponent>();

    if (modules.size() == 1) {
      // The pathfinding functionality is only offered when the list
      // contains just a single module. This means that either a node
      // of the project tree was clicked or just one module is selected
      // in the modules table.

      final Module targetModule = modules.get(0);

      menus.add(new JMenuItem(new PathfindingAction(targetModule)));
    }

    return menus;
  }

  @Override
  public String getDescription() {
    return "Finds paths between basic blocks of an executable";
  }

  @Override
  public long getGuid() {
    return 4523525670943L;
  }

  @Override
  public String getName() {
    return "Pathfinding Plugin";
  }

  @Override
  public void init(final IPluginInterface pluginProvider) {
    // Nothing to do here => The context menu of module
    // nodes is created in extendPluginMenu

    pluginInterface = pluginProvider;
  }

  @Override
  public void unload() {
    // Not used yet
  }

  /**
   * This class is used to update the state of the created context menu from disabled to enabled
   * once the target module is loaded.
   */
  private static class ActionUpdater extends ModuleListenerAdapter {
    /**
     * The context menu action to be enabled.
     */
    private final AbstractAction m_action;

    /**
     * Creates a new updater object that keeps module state and menu state synchronized.
     * 
     * @param action The context menu action to be enabled.
     */
    public ActionUpdater(final AbstractAction action) {
      m_action = action;
    }

    @Override
    public void loadedModule(final Module module) {
      // Once the target module is loaded it is possible
      // to enable the context menu to provide the
      // pathfinding functionality for the target module.

      m_action.setEnabled(true);
    }
  }

  /**
   * This thread is used to create the path.
   */
  private class CreationThread implements IProgressThread {
    private final Module module;
    private final BasicBlock sourceBlock;
    private final BasicBlock targetBlock;
    private final Function firstFunction;
    private final Function secondFunction;
    private View view;
    private boolean threwException = true;

    public CreationThread(final Module module, final BasicBlock sourceBlock,
        final BasicBlock targetBlock, final Function firstFunction, final Function secondFunction) {
      this.module = module;
      this.sourceBlock = sourceBlock;
      this.targetBlock = targetBlock;
      this.firstFunction = firstFunction;
      this.secondFunction = secondFunction;
    }

    @Override
    public boolean close() {
      return false;
    }

    public View getCreatedView() {
      return view;
    }

    @Override
    public void run() {
      try {
        view =
            PathFinder.createPath(module, sourceBlock, targetBlock, firstFunction, secondFunction);
        threwException = false;
      } catch (final Exception e) {
        Logger.logException(e);
        MessageBox.showException(pluginInterface.getMainWindow().getFrame(), e,
            "Could not create path");
      }
    }

    public boolean threwException() {
      return threwException;
    }
  }

  /**
   * This class provides the action that is performed when the user clicks the context menu entry
   * created by this plugin.
   */
  private class PathfindingAction extends AbstractAction {

    /**
     * Target module of the pathfinding operation.
     */
    private final Module m_module;

    private final ActionUpdater m_updater = new ActionUpdater(this);

    /**
     * Creates a new pathfinding action object.
     * 
     * @param module Target module of the pathfinding operation.
     */
    public PathfindingAction(final Module module) {
      super("Pathfinder");

      m_module = module;

      // Pathfinding only works when the target module is loaded.
      // If the target module is not loaded, the context menu
      // must be disabled.
      setEnabled(module.isLoaded());

      // Since the state of the module can change, it might be
      // necessary to update the state of the context menu later.
      m_module.addListener(m_updater);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      try {
        showPathfindingDialog(m_module);
      } catch (final CouldntLoadDataException exception) {
        Logger.logException(exception);
        MessageBox.showException(pluginInterface.getMainWindow().getFrame(), exception,
            "Could not create path");
      }
    }
  }
}
