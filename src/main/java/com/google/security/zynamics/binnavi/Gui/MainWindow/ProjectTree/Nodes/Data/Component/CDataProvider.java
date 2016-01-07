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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataChangedListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataProvider;

import java.util.Arrays;



/**
 * Data provider for the data that is shown in the hex display of the project window when the Data
 * node is selected.
 */
public final class CDataProvider implements IDataProvider {
  /**
   * Module whose data is provided by the provider.
   */
  private final INaviModule m_module;

  /**
   * Listeners that are notified about changes in the provided data.
   */
  private final ListenerProvider<IDataChangedListener> m_listeners =
      new ListenerProvider<IDataChangedListener>();

  /**
   * Listener that updates the provided data when the underlying module data changes.
   */
  private final IModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * Data is cached for performance reasons.
   */
  private byte[] m_data;

  /**
   * Creates a new data provider object.
   * 
   * @param module The module whose data is provided.
   */
  public CDataProvider(final INaviModule module) {
    m_module = module;
    m_data = module.getData();

    module.addListener(m_moduleListener);
  }

  @Override
  public void addListener(final IDataChangedListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_module.removeListener(m_moduleListener);
  }

  @Override
  public byte[] getData() {
    return m_data; // OK to return this because speed > safety here
  }

  @Override
  public byte[] getData(final long offset, final int length) {
    return Arrays.copyOfRange(getData(), (int) offset, (int) (offset + length));
  }

  @Override
  public int getDataLength() {
    return m_data.length;
  }

  @Override
  public boolean hasData(final long start, final int length) {
    return true;
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  public boolean keepTrying() {
    return true;
  }

  @Override
  public void removeListener(final IDataChangedListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setData(final long offset, final byte[] data) {
    // Data node data is not editable
  }

  /**
   * Listener that updates the provided data when the underlying module data changes.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedData(final CModule module, final byte[] data) {
      m_data = module.getData();

      for (final IDataChangedListener listener : m_listeners) {
        try {
          listener.dataChanged();
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
