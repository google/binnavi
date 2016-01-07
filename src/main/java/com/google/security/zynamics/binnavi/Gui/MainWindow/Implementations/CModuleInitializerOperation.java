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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;


import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.ModuleInitializeEvents;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;



/**
 * Operation class for module initialization.
 */
public class CModuleInitializerOperation implements IProgressOperation {
  /**
   * Module to be initialized.
   */
  private final INaviModule m_module;

  /**
   * Displays progress information about the module initialization operation.
   */
  private final CProgressPanel m_progressPanel = new CProgressPanel("", false, true) {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 2022963913601217238L;

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
     * Flag that indicates whether the next event to arrive is the first one for a database
     * initialization operation.
     */
    private boolean m_first = true;

    @Override
    public boolean initializing(final ModuleInitializeEvents event, final int counter) {
      if (!m_continue) {
        m_continue = true;

        return false;
      }

      m_progressPanel.next();

      if (event == ModuleInitializeEvents.Finished) {
        m_progressPanel.setVisible(false);

        m_first = true;
        m_continue = true;
      } else if (m_first) {
        m_progressPanel.setText("Initializing module" + ": '"
            + m_module.getConfiguration().getName() + "'");
        m_progressPanel.setMaximum(ModuleInitializeEvents.values().length);

        m_progressPanel.setValue(counter);

        m_first = false;
      }

      return true;
    }
  };

  /**
   * Creates a new initializer operation.
   * 
   * @param module Module to be initialized.
   */
  public CModuleInitializerOperation(final INaviModule module) {
    m_module = module;

    m_progressPanel.setText("Initializing module" + ": '" + m_module.getConfiguration().getName()
        + "'");

    CGlobalProgressManager.instance().add(this);

    module.addListener(m_listener);
  }

  @Override
  public String getDescription() {
    return "Initializing Module";
  }

  @Override
  public CProgressPanel getProgressPanel() {
    return m_progressPanel;
  }

  /**
   * Stops the initialization operation.
   */
  public void stop() {
    m_module.removeListener(m_listener);

    CGlobalProgressManager.instance().remove(this);
  }
}
