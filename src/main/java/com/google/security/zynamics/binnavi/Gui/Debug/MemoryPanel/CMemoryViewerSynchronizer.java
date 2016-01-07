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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
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
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.JHexPanel.IHexPanelListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.AddressMode;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;

/**
 * Synchronizes a hex view control with a debugger model and the selected debugger options.
 */
public final class CMemoryViewerSynchronizer {
  /**
   * The hex view control to synchronize.
   */
  private final JHexView m_hexView;

  /**
   * Used to receive memory data from the debug client.
   */
  private final CMemoryProvider m_provider;

  /**
   * Provides information about the active debuggers.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Keeps track on changing debuggers.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Keeps track on events inside debuggers.
   */
  private final ProcessManagerListener m_debuggerListener = new InternalProcessListener();

  /**
   * Keeps track on events inside threads.
   */
  private final ThreadListener m_threadListener = new InternalThreadListener();

  /**
   * Listeners that are notified about changes in the memory viewer synchronizer.
   */
  private final ListenerProvider<IMemoryViewerSynchronizerListener> m_listeners =
      new ListenerProvider<IMemoryViewerSynchronizerListener>();

  /**
   * Synchronizes the debug perspective model with the hex viewer.
   */
  private final IHexPanelListener m_hexListener = new InternalHexListener();

  /**
   * Creates a new memory viewer synchronizer.
   *
   * @param hexView The hex view control to synchronize.
   * @param provider Used to receive memory data from the debug client.
   * @param debugPerspectiveModel Provides information about the active debuggers.
   */
  public CMemoryViewerSynchronizer(final JHexView hexView, final CMemoryProvider provider,
      final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(hexView, "IE01406: Hex view argument can not be null");

    Preconditions.checkNotNull(provider, "IE01407: Provider argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01408: Debug perspective model argument can not be null");

    m_hexView = hexView;
    m_provider = provider;
    m_debugPerspectiveModel = debugPerspectiveModel;

    debugPerspectiveModel.addListener(m_debugListener);

    synchronizeDebugger(null, m_debugPerspectiveModel.getCurrentSelectedDebugger());

    m_hexView.addHexListener(m_hexListener);
  }

  /**
   * Determines the lowest address that can be shown for a given memory map.
   *
   * @param memoryMap The memory map in question.
   *
   * @return The lowest memory address of the map or 0 if the map is empty.
   */
  private IAddress getFirstAddress(final MemoryMap memoryMap) {
    for (final MemorySection memorySection : memoryMap) {
      return memorySection.getStart();
    }

    return new CAddress(0);
  }

  /**
   * Adjusts size, base offset, and current offset of the hex view depending on a given address and
   * the memory sections known to the debugger.
   *
   * @param address The memory address to consider.
   *
   * @return True, if the hex view was adjusted. False, if the address is not in any known memory
   *         section.
   */
  private boolean resizeData(final IAddress address) {
    if (address == null) {
      return true;
    }

    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger != null) {
      final MemorySection section =
          ProcessHelpers.getSectionWith(debugger.getProcessManager().getMemoryMap(), address);

      if (section == null) {
        // No section with the given memory address

        m_debugPerspectiveModel.setActiveMemoryAddress(null, false);

        return false;
      } else {
        m_hexView.setBaseAddress(section.getStart().toLong());
        m_provider.setMemorySize(section.getSize());
        m_hexView.gotoOffset(address.toLong());

        updateGui();

        return true;
      }
    }

    return true;
  }

  /**
   * Makes sure that the synchronizer is listening on the active debugger.
   *
   * @param oldDebugger The previously active debugger (or null).
   * @param newDebugger The currently active debugger (or null).
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_debuggerListener);

      final TargetProcessThread activeThread = oldDebugger.getProcessManager().getActiveThread();

      if (activeThread != null) {
        synchronizeThreads(activeThread, null);
      }
    }

    if (newDebugger != null) {
      newDebugger.getProcessManager().addListener(m_debuggerListener);
      m_provider.setDebugger(newDebugger);

      final TargetProcessThread activeThread = newDebugger.getProcessManager().getActiveThread();

      if (activeThread != null) {
        synchronizeThreads(null, activeThread);
      }
    }

    updateGui();
  }

  /**
   * Keeps listeners synchronized with current threads.
   *
   * @param oldThread The previously active thread.
   * @param newThread The new active thread.
   */
  private void synchronizeThreads(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(m_threadListener);
    }

    if (newThread != null) {
      newThread.addListener(m_threadListener);
    }

    updateGui();
  }

  /**
   * Updates the GUI depending on the state of the active debugger.
   */
  private void updateGui() {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread thread = debugger == null ? null : debugger.getProcessManager().getActiveThread();

    final boolean connected = debugger != null && debugger.isConnected();
    final boolean suspended = connected && thread != null;

    m_hexView.setEnabled(connected && suspended && m_provider.getDataLength() != 0);

    if (connected) {
      m_hexView.setDefinitionStatus(DefinitionStatus.DEFINED);
    } else {
      // m_hexView.setDefinitionStatus(DefinitionStatus.UNDEFINED);

      m_provider.setMemorySize(0);
      m_hexView.setBaseAddress(0);
      m_hexView.uncolorizeAll();
    }
  }

  /**
   * Adds a listener that is notified about events in the memory synchronizer.
   *
   * @param listener The listener to add.
   */
  public void addListener(final IMemoryViewerSynchronizerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_hexView.removeHexListener(m_hexListener);

    synchronizeDebugger(m_debugPerspectiveModel.getCurrentSelectedDebugger(), null);

    m_debugPerspectiveModel.removeListener(m_debugListener);
  }

  /**
   * Removes a listener from the memory viewer synchronizer.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IMemoryViewerSynchronizerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Keeps track on changing debuggers.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveAddress(final IAddress address, final boolean focusMemoryWindow) {
      if (!resizeData(address)) {
        for (final IMemoryViewerSynchronizerListener listener : m_listeners) {
          try {
            listener.requestedUnsectionedAddress(address);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      } else if (focusMemoryWindow) {
        m_hexView.requestFocusInWindow();
      }
    }

    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);

      if (newDebugger != null) {
        resizeData(getFirstAddress(newDebugger.getProcessManager().getMemoryMap()));
      }
    }
  }

  /**
   * Synchronizes the debug perspective model with the hex viewer.
   */
  private class InternalHexListener implements IHexPanelListener {
    @Override
    public void selectionChanged(final long start, final long length) {
      m_debugPerspectiveModel.setActiveMemoryAddress(
          new CAddress(m_hexView.getCurrentOffset()), false);
    }
  }

  /**
   * Keeps track on events inside debuggers.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      synchronizeThreads(oldThread, newThread);

      updateGui();
    }

    @Override
    public void changedMemoryMap() {
      final boolean showingData = m_provider.getDataLength() != 0;
      final long currentOffset = m_hexView.getCurrentOffset();

      if (!resizeData(new CAddress(m_hexView.getCurrentOffset()))) {
        final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

        if (debugger != null
            && debugger.getProcessManager().getMemoryMap().getNumberOfSections() != 0) {
          final MemorySection section = debugger.getProcessManager().getMemoryMap().getSection(0);

          m_hexView.setBaseAddress(section.getStart().toLong());
          m_provider.setMemorySize(section.getSize());
          m_hexView.gotoOffset(section.getStart().toLong());
        }

        if (showingData) {
          for (final IMemoryViewerSynchronizerListener listener : m_listeners) {
            try {
              listener.addressTurnedInvalid(currentOffset);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void changedTargetInformation(final TargetInformation information) {
      m_hexView.setAddressMode(
          information.getAddressSize() == 64 ? AddressMode.BIT64 : AddressMode.BIT32);
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
