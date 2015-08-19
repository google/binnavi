/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;

import java.util.ArrayList;
import java.util.List;


import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfiguration;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;

public final class MockAddressSpace implements INaviAddressSpace {
  private final List<INaviModule> m_modules = new ArrayList<INaviModule>();

  public final ListenerProvider<IAddressSpaceListener> m_listeners =
      new ListenerProvider<IAddressSpaceListener>();

  public boolean m_loaded = false;

  @Override
  public void addListener(final IAddressSpaceListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public CAddressSpaceConfiguration getConfiguration() {
    return null;
  }

  @Override
  public CAddressSpaceContent getContent() {
    return null;
  }

  @Override
  public int getModuleCount() {
    return m_modules.size();
  }

  @Override
  public INaviProject getProject() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isLoaded() {
    return m_loaded;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  @Override
  public void load() {
    m_loaded = true;

    for (final IAddressSpaceListener listener : m_listeners) {
      listener.loaded(this);
    }
  }

  @Override
  public void removeListener(final IAddressSpaceListener listener) {
    m_listeners.removeListener(listener);
  }
}
