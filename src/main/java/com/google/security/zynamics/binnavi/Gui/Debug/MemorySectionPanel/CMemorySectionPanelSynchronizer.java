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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Synchronizes a memory section box with a debug GUI perspective.
 */
public final class CMemorySectionPanelSynchronizer {
  /**
   * The memory section box to synchronize.
   */
  private final CMemorySectionBox memorySectionBox;

  /**
   * The debug GUI perspective to synchronize.
   */
  private final CDebugPerspectiveModel debugPerspectiveModel;

  /**
   * Keeps track of relevant events in the process of the active debugger.
   */
  private final ProcessManagerListener processManagerListener =
      new InternalProcessManagerListener();

  /**
   * Keeps track of relevant events in the debug GUI perspective.
   */
  private final IDebugPerspectiveModelListener debugListener = new InternalDebugListener();

  /**
   * Keeps track of changes in the memory section box.
   */
  private final ItemListener memoryBoxListener = new InternalMemMapChangeListener();

  /**
   * Flag that signals that an external event (instead of a selection in the section box) triggered
   * a change in the active address of the debug GUI perspective.
   */
  private boolean updating;

  /**
   * Keeps track on events inside threads.
   */
  private final ThreadListener threadListener = new InternalThreadListener();

  /**
   * Creates a new memory section synchronizer.
   *
   * @param sectionBox The memory section box to synchronize.
   * @param debugPerspectiveModel The debug GUI perspective to synchronize.
   */
  public CMemorySectionPanelSynchronizer(final CMemorySectionBox sectionBox,
      final CDebugPerspectiveModel debugPerspectiveModel) {
    memorySectionBox =
        Preconditions.checkNotNull(sectionBox, "IE01454: Section box argument can not be null");
    this.debugPerspectiveModel = Preconditions.checkNotNull(debugPerspectiveModel,
        "IE01455: Debug perspective model argument can not be null");

    synchronizeDebugger(null, debugPerspectiveModel.getCurrentSelectedDebugger());
    debugPerspectiveModel.addListener(debugListener);
    memorySectionBox.addItemListener(memoryBoxListener);
  }

  /**
   * Keeps the memory section box synchronized with the currently active debugger of the debug GUI
   * perspective.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The currently active debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    TargetProcessThread oldThread = null;

    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(processManagerListener);
      oldThread = oldDebugger.getProcessManager().getActiveThread();
    }

    if (newDebugger != null) {
      newDebugger.getProcessManager().addListener(processManagerListener);

      final TargetInformation targetInformation =
          newDebugger.getProcessManager().getTargetInformation();

      if (targetInformation != null) {
        updateMemoryBox();
      }

      synchronizeThreads(oldThread, newDebugger.getProcessManager().getActiveThread());
    }

    updateGui();
  }

  /**
   * Keeps listeners up to date on changing threads.
   *
   * @param oldThread The previously active thread.
   * @param newThread The new active thread.
   */
  private void synchronizeThreads(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(threadListener);
    }

    if (newThread != null) {
      newThread.addListener(threadListener);
    }

    updateGui();
  }

  /**
   * Updates the GUI of the synchronized memory section box depending on the current state of the
   * debug GUI perspective and the active debugger.
   */
  private void updateGui() {
    final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();

    final boolean enable = (activeThread != null) && (activeDebugger != null)
        && activeDebugger.isConnected()
        && (activeDebugger.getProcessManager().getTargetInformation() != null) && activeDebugger
            .getProcessManager().getTargetInformation().getDebuggerOptions().canMemmap()
        && (memorySectionBox.getItemCount() != 0);

    new SwingInvoker() {
      @Override
      protected void operation() {
        memorySectionBox.setEnabled(enable);
      }
    }.invokeAndWait();
  }

  /**
   * Updates the memory selection box.
   */
  private void updateMemoryBox() {
    final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();
    final ArrayList<CMemorySectionWrapper> sections = new ArrayList<>();
    if ((activeDebugger != null) && activeDebugger.isConnected()) {
      for (final MemorySection section : activeDebugger.getProcessManager().getMemoryMap()) {
        sections.add(new CMemorySectionWrapper(section));
      }
    }

    new SwingInvoker() {
      @Override
      protected void operation() {
        memorySectionBox.removeAllItems();
        if ((activeDebugger != null) && activeDebugger.isConnected()) {
          for (final CMemorySectionWrapper sectionWrapper : sections) {
            memorySectionBox.addItem(sectionWrapper);
          }
        }
      }
    }.invokeAndWait();
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    memorySectionBox.removeItemListener(memoryBoxListener);
    debugPerspectiveModel.removeListener(debugListener);

    final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();

    if (activeDebugger != null) {
      synchronizeThreads(activeDebugger.getProcessManager().getActiveThread(), null);
    }

    synchronizeDebugger(debugPerspectiveModel.getCurrentSelectedDebugger(), null);
  }

  /**
   * This listener is responsible for keeping the memory section box synchronized with the state of
   * the debug GUI perspective.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveAddress(final IAddress address, final boolean focusMemoryWindow) {
      // If the active address was changed, the corresponding memory section
      // should be selected in the memory section box.

      if (address == null) {
        return;
      }

      if (updating) {
        // This happens when the memory section changed and the memory box is
        // being updated.

        return;
      }

      updating = true;

      final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();

      if (activeDebugger != null) {
        final MemorySection section = ProcessHelpers.getSectionWith(
            activeDebugger.getProcessManager().getMemoryMap(), address);
        memorySectionBox.setSelectedItem(section);
      }

      updating = false;
    }

    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * Listener that updates the displayed target process memory when the user changes the selection
   * in the memory section box.
   */
  private class InternalMemMapChangeListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      if (updating) {
        return;
      }

      final CMemorySectionWrapper section = memorySectionBox.getSelectedItem();

      if (section == null) {
        // No memory section left in the box.
        return;
      }

      debugPerspectiveModel.setActiveMemoryAddress(section.getObject().getStart(), false);
    }
  }

  /**
   * This listener is responsible for keeping the memory section box synchronized with important
   * events in the debugged target process.
   */
  private class InternalProcessManagerListener extends ProcessManagerListenerAdapter {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      synchronizeThreads(oldThread, newThread);

      updateGui();
    }

    @Override
    public void changedMemoryMap() {
      // A new memory map arrived, this means that the memory section combo box is about
      // to be refilled. As we want to keep displaying the current memory section we
      // have to suppress updating while the combo box is refilled.
      updating = true;

      final IAddress previousAddress = debugPerspectiveModel.getActiveAddress();

      updateMemoryBox();
      updateGui();

      final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();

      if (activeDebugger != null) {
        final MemoryMap memoryMap = activeDebugger.getProcessManager().getMemoryMap();

        if (previousAddress == null) {
          updating = false;

          if (memoryMap.getNumberOfSections() != 0) {
            final MemorySection firstSection = memoryMap.getSection(0);

            debugPerspectiveModel.setActiveMemoryAddress(firstSection.getStart(), false);
          }
        } else {
          final MemorySection updatedSection =
              ProcessHelpers.getSectionWith(memoryMap, previousAddress);

          memorySectionBox.setSelectedItem(updatedSection);

          updating = false;
        }
      }
    }

    @Override
    public void changedTargetInformation(final TargetInformation information) {
      updateMemoryBox();
      updateGui();
    }
  }

  /**
   * Keeps track on events inside threads.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void stateChanged(final TargetProcessThread thread) {
      updateGui();
    }
  }
}
