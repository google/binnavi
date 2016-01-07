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
package com.google.security.zynamics.binnavi.Gui.DebuggerComboBox;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainerListener;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Combobox model for the debugger combobox.
 */
public final class CDebuggerComboModel implements ComboBoxModel<CDebuggerTemplateWrapper> {
  /**
   * The debugger container that provides the debuggers for the combobox.
   */
  private final IDebuggerContainer debuggerContainer;

  /**
   * The currently selected item.
   */
  private CDebuggerTemplateWrapper selectedItem;

  /**
   * Listeners that are notified about changes in the model.
   */
  private final ListenerProvider<ListDataListener> modelListeners =
      new ListenerProvider<ListDataListener>();

  /**
   * Listener that keeps track of changes in the debugger container.
   */
  private final InternalDebuggerContainerListener debuggerListener =
      new InternalDebuggerContainerListener();

  /**
   * Elements that are displayed in the combobox.
   */
  private final List<CDebuggerTemplateWrapper> comboboxElements =
      new ArrayList<CDebuggerTemplateWrapper>();

  /**
   * Creates a new debugger combobox model object.
   *
   * @param debuggerContainer The debugger container that provides the debuggers to be displayed.
   */
  public CDebuggerComboModel(final IDebuggerContainer debuggerContainer) {
    this.debuggerContainer = Preconditions.checkNotNull(
        debuggerContainer, "IE01570: Debugger container argument can not be null");

    debuggerContainer.addListener(debuggerListener);

    updateElements();
  }

  /**
   * Updates the elements of the combobox after relevant changes to the debugger container.
   */
  private void updateElements() {
    comboboxElements.clear();

    comboboxElements.add(new CDebuggerTemplateWrapper(null));

    for (final DebuggerTemplate template : debuggerContainer.getDebuggers()) {
      comboboxElements.add(new CDebuggerTemplateWrapper(template));
    }
  }

  @Override
  public void addListDataListener(final ListDataListener listener) {
    modelListeners.addListener(listener);
  }

  @Override
  public CDebuggerTemplateWrapper getElementAt(final int index) {
    return comboboxElements.get(index);
  }

  @Override
  public Object getSelectedItem() {
    return selectedItem;
  }

  @Override
  public int getSize() {
    return comboboxElements.size();
  }

  @Override
  public void removeListDataListener(final ListDataListener listener) {
    modelListeners.removeListener(listener);
  }

  @Override
  public void setSelectedItem(final Object anItem) {
    selectedItem = (CDebuggerTemplateWrapper) anItem;
  }

  /**
   * Listener that keeps track of changes in the debugger container.
   */
  private class InternalDebuggerContainerListener implements IDebuggerContainerListener {
    @Override
    public void addedDebugger(
        final IDebuggerContainer container, final DebuggerTemplate debugger) {
      updateElements();

      for (final ListDataListener listener : modelListeners) {
        listener.contentsChanged(new ListDataEvent(
            CDebuggerComboModel.this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
      }
    }

    @Override
    public void removedDebugger(
        final IDebuggerContainer container, final DebuggerTemplate debugger) {
      updateElements();

      for (final ListDataListener listener : modelListeners) {
        listener.contentsChanged(new ListDataEvent(
            CDebuggerComboModel.this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
      }
    }
  }
}
