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
package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerSelectionPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;

import java.net.URL;

import javax.swing.JComboBox;

/**
 * Combobox class that is used to choose from available debuggers.
 *
 *  The combobox automatically updates itself on changes in the debugger provider that is passed in
 * the constructor.
 */
public final class CDebuggerComboBox extends JComboBox<CDebuggerWrapper> implements IHelpProvider {
  /**
   * Keeps track of changes in the available debuggers.
   */
  private final DebuggerProviderListener m_internalDebuggerListener =
      new InternalDebuggerListener();

  /**
   * Provides information about the available debuggers.
   */
  private final BackEndDebuggerProvider m_provider;

  /**
   * Creates a new debugger combobox.
   *
   * @param provider Provides information about the available debuggers.
   */
  public CDebuggerComboBox(final BackEndDebuggerProvider provider) {
    m_provider = Preconditions.checkNotNull(provider, "IE01363: Provider can not be null");

    // Each existing debugger is added to the combobox of debuggers for
    // the user to choose from.
    for (final IDebugger debugger : provider.getDebuggers()) {
      addItem(new CDebuggerWrapper(debugger));
    }

    // We want to be notified about new debuggers or deleted debuggers
    provider.addListener(m_internalDebuggerListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_provider.removeListener(m_internalDebuggerListener);
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "Used to select the active debugger. This debugger is the one who receives input when you click buttons like Resume or set breakpoints.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }

  @Override
  public CDebuggerWrapper getSelectedItem() {
    return (CDebuggerWrapper) super.getSelectedItem();
  }

  /**
   * Keeps the debugger combobox synchronized with the existing debuggers.
   */
  private class InternalDebuggerListener implements DebuggerProviderListener {
    /**
     * When a new debugger is added, we have to add another entry in the combobox for that debugger.
     */
    @Override
    public void debuggerAdded(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      addItem(new CDebuggerWrapper(debugger));
    }

    /**
     * When a debugger is removed, we have to remove the corresponding entry from the combobox.
     */
    @Override
    public void debuggerRemoved(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      for (int i = 0; i < getItemCount(); i++) {
        if (getItemAt(i).getObject() == debugger) {
          removeItemAt(i);

          return;
        }
      }
    }
  }
}
