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

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IMemoryProvider;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.memmanager.IMemoryListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataChangedListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataProvider;

/**
 * This class provides the memory viewer with its data. If memory data is not available, the memory
 * is automatically requested from the debugger.
 */
public final class CMemoryProvider implements IDataProvider, IMemoryListener {
  /**
   * Memory provider that communicates with the debugger.
   */
  private IMemoryProvider m_memoryProvider;

  /**
   * Keeps the total size of the memory to be shown in the hex view. This is necessary to keep the
   * scroll bar working even if only parts of the memory have been loaded so far.
   */
  private int m_size;

  /**
   * Listeners that are notified about changes in the available memory data.
   */
  private final ListenerProvider<IDataChangedListener> m_listeners =
      new ListenerProvider<IDataChangedListener>();

  /**
   * Debugger from which the memory is read.
   */
  private IDebugger m_debugger;

  /**
   * Notifies data changes.
   */
  private void notifyChanged() {
    for (final IDataChangedListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener.
      try {
        listener.dataChanged();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final IDataChangedListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public byte[] getData() {
    return getData(0, getDataLength());
  }

  @Override
  public byte[] getData(final long offset, final int length) {
    Preconditions.checkArgument(length >= 0, "IE01401: Length can not be negative");

    if (length == 0) {
      return new byte[0];
    } else {
      return m_debugger.getProcessManager().getMemory().getData(offset, length);
    }
  }

  @Override
  public int getDataLength() {
    return m_size;
  }

  @Override
  public boolean hasData(final long offset, final int size) {
    Preconditions.checkArgument(size >= 0, "IE01402: Size can not be negative");
    m_memoryProvider.hasData(BigInteger.valueOf(offset), size);

    return m_debugger.getProcessManager().getMemory().hasData(offset, size);
  }

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  public boolean keepTrying() {
    return (m_debugger != null) && m_debugger.isConnected();
  }

  @Override
  public void memoryChanged(final long address, final int size) {
    notifyChanged();
  }

  @Override
  public void memoryCleared() {
    notifyChanged();
  }

  @Override
  public void removeListener(final IDataChangedListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setData(final long offset, final byte[] data) {
    if (m_debugger != null) {
      try {
        m_debugger.writeMemory(new CAddress(offset), data);
        m_debugger.getProcessManager().getMemory().store(offset, data);
      } catch (final DebugExceptionWrapper exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the debugger used for memory providing.
   *
   * @param debugger The new debugger.
   */
  public void setDebugger(final IDebugger debugger) {
    m_debugger = debugger;
    m_memoryProvider = debugger == null ? null : debugger.getMemoryProvider();

    notifyChanged();
  }

  /**
   * Sets the new memory size. This function is necessary because the memory size can be different
   * than the size of the partially loaded memory.
   *
   * @param size The new memory size.
   */
  public void setMemorySize(final int size) {
    Preconditions.checkArgument(size >= 0, "IE01403: Size argument can not be negative");
    m_size = size;
    notifyChanged();
  }
}
