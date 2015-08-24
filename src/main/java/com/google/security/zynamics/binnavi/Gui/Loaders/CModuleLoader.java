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
package com.google.security.zynamics.binnavi.Gui.Loaders;



import java.awt.Window;

import javax.swing.JTree;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CNodeExpander;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.ModuleLoadEvents;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * This class can be used to load a module while showing the corresponding progress dialog.
 */
public final class CModuleLoader {
  /**
   * Static helper class.
   */
  private CModuleLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads a module inside a thread.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module to load.
   * @param projectTree Project tree to expand on module loading. This argument can be null.
   */
  private static void loadModuleInternal(final Window parent, final INaviModule module,
      final JTree projectTree) {
    final CModuleLoaderOperation operation = new CModuleLoaderOperation(module);

    boolean success = false;

    try {
      if (projectTree != null) {
        // Make sure the lazy UI components responsible for displaying data etc.
        // are properly initialized. Order is important - this has to happen
        // BEFORE module.load() is called, so that all the listeners in the UI
        // component already exist and can hence be filled by the module loading.
        CNodeExpander.findNode(projectTree, module).getComponent();
      }
      module.load();

      success = true;

      if (projectTree != null) {
        new SwingInvoker() {
          @Override
          protected void operation() {
            CNodeExpander.expandNode(projectTree, module);            
            operation.stop();
          }
        }.invokeLater();
      }
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00177: " + "Module data could not be loaded";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not load the module '%s'.", module.getConfiguration().getName()),
              new String[] {"The connection dropped while the data was loaded."},
              new String[] {"BinNavi can not open the module. To fix this situation try "
                  + "to load the module again. Restart BinNavi if necessary and contact "
                  + "the BinNavi support if the problem persists."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final LoadCancelledException e) {
      // Don't show the user that he cancelled the operation.
    } finally {
      if (!success) {
        operation.stop();
      }
    }
  }

  /**
   * Loads a module inside a thread.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module to load.
   * @param projectTree Project tree to expand on module loading. This argument can be null.
   */
  private static void loadModuleThreaded(final Window parent, final INaviModule module,
      final JTree projectTree) {
    Preconditions.checkNotNull(parent, "IE01193: Parent argument can not be null");
    Preconditions.checkNotNull(module, "IE01194: Module argument can not be null");

    if (module.isLoading()) {
      return;
    }

    new Thread() {
      @Override
      public void run() {
        loadModuleInternal(parent, module, projectTree);
      }
    }.start();
  }

  /**
   * Loads a module while showing a progress dialog.
   * 
   * @param tree Project tree to expand on loading.
   * @param module The module to load.
   */
  public static void loadModule(final JTree tree, final INaviModule module) {
    Preconditions.checkNotNull(tree, "IE01195: Tree argument can not be null");
    Preconditions.checkNotNull(module, "IE01196: Module argument can not be null");
    loadModuleThreaded(SwingUtilities.getWindowAncestor(tree), module, tree);
  }

  /**
   * Loads a module while showing a progress dialog.
   * 
   * @param parent The parent window of the progress dialog.
   * @param module The module to load.
   */
  public static void loadModule(final Window parent, final INaviModule module) {
    Preconditions.checkNotNull(parent, "IE01197: Parent argument can not be null");
    Preconditions.checkNotNull(module, "IE01278: Module argument can not be null");
    loadModuleThreaded(parent, module, null);
  }

  /**
   * Operation class for module loading.
   */
  private static class CModuleLoaderOperation implements IProgressOperation {
    /**
     * Module to be loaded.
     */
    private final INaviModule m_module;

    /**
     * Displays progress information about the module load operation.
     */
    private final CProgressPanel m_progressPanel = new CProgressPanel("", false, true) {
      /**
       * Used for serialization.
       */
      private static final long serialVersionUID = 81245428645703865L;

      @Override
      protected void closeRequested() {
        setText("Cancelling module conversion");
        m_continue = false;
      }
    };

    /**
     * Used to cancel module initializations.
     */
    private boolean m_continue = true;

    /**
     * Updates the GUI on relevant changes in the module.
     */
    private final IModuleListener m_listener = new CModuleListenerAdapter() {
      /**
       * Flag that indicates whether the next event to arrive is the first one for a database load
       * operation.
       */
      private boolean m_first = true;

      @Override
      public boolean loading(final ModuleLoadEvents event, final int counter) {
        if (!m_continue) {
          m_continue = true;
          return false;
        }

        m_progressPanel.next();

        if (event == ModuleLoadEvents.Finished) {
          m_progressPanel.setVisible(false);
          m_first = true;
          m_continue = true;
        } else if (m_first) {
          m_progressPanel.setText("Loading module" + ": " + m_module.getConfiguration().getName());
          m_progressPanel.setMaximum(ModuleLoadEvents.values().length);
          m_progressPanel.setValue(counter);
          m_first = false;
        }

        return true;
      }
    };

    /**
     * Creates a new loader operation.
     * 
     * @param module Module to be loaded.
     */
    public CModuleLoaderOperation(final INaviModule module) {
      m_module = module;
      CGlobalProgressManager.instance().add(this);
      module.addListener(m_listener);
    }

    @Override
    public String getDescription() {
      return "Loading Module";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressPanel;
    }

    /**
     * Stops the load operation.
     */
    public void stop() {
      m_module.removeListener(m_listener);
      CGlobalProgressManager.instance().remove(this);
    }
  }
}
