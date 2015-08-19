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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManagerMockBackend;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.List;

public final class MockModule implements INaviModule {
  private SQLProvider m_provider = new MockSqlProvider();
  public final ListenerProvider<IModuleListener> m_listeners =
      new ListenerProvider<IModuleListener>();

  public boolean m_loaded = true;
  private MockModuleConfiguration m_configuration;
  private MockModuleContent m_content;
  private boolean m_initialized = false;

  public MockModule(final boolean loaded) {
    m_loaded = loaded;
    m_configuration = new MockModuleConfiguration(m_provider);
    m_content = new MockModuleContent(this, m_provider, m_listeners, null, null);
  }

  public MockModule() {
    this(true);
    m_configuration = new MockModuleConfiguration(m_provider);
    m_content = new MockModuleContent(this, m_provider, m_listeners, null, null);
  }

  public MockModule(final SQLProvider provider) {
    m_provider = provider;
    m_provider.setModules(Lists.<INaviModule>newArrayList(this));
    m_configuration = new MockModuleConfiguration(provider);
    m_content = new MockModuleContent(this, provider, m_listeners, null, null);
  }

  public MockModule(final SQLProvider provider, final boolean loaded) {
    m_provider = provider;
    m_loaded = loaded;
    m_provider.setModules(Lists.<INaviModule>newArrayList(this));
    m_configuration = new MockModuleConfiguration(m_provider);
    m_content = new MockModuleContent(this, m_provider, m_listeners, null, null);
  }

  public MockModule(final SQLProvider provider, final List<INaviView> views,
      final List<INaviFunction> functions) {
    m_provider = provider;
    m_provider.setModules(Lists.<INaviModule>newArrayList(this));
    m_configuration = new MockModuleConfiguration(m_provider);
    m_content = new MockModuleContent(this, m_provider, m_listeners, views, functions);
  }

  @Override
  public void addListener(final IModuleListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviInstruction createInstruction(final IAddress address, final String mnemonic,
      final List<COperandTree> operands, final byte[] data, final String architecture) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public COperandTree createOperand(final COperandTreeNode node) {
    return new COperandTree(node, new MockSqlProvider(), this.getTypeManager(), this.getContent()
        .getTypeInstanceContainer());
  }

  @Override
  public COperandTreeNode createOperandExpression(final String value,
      final ExpressionType expressionType) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public MockModuleConfiguration getConfiguration() {
    return m_configuration;
  }

  @Override
  public MockModuleContent getContent() {
    return m_content;
  }

  @Override
  public int getCustomViewCount() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public byte[] getData() {
    return new byte[0];
  }

  @Override
  public int getFunctionCount() {
    return m_content.getFunctionContainer().getFunctionCount();
  }

  @Override
  public TypeManager getTypeManager() {
    try {
      return new TypeManager(new TypeManagerMockBackend());
    } catch (final CouldntLoadDataException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public int getViewCount() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviView> getViewsWithAddresses(
      final List<UnrelocatedAddress> address, final boolean all) {
    throw new IllegalStateException("Error: Not yet implemented");
  }

  @Override
  public void initialize() {
    m_initialized = true;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(m_provider);
  }

  @Override
  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public boolean isInitializing() {
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
  public boolean isStared() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void load() {
    m_loaded = true;

    for (final IModuleListener listener : m_listeners) {
      listener.loadedModule(this);
    }
  }

  @Override
  public void loadData() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String readSetting(final String key) {
    return new String("");
  }

  @Override
  public void removeListener(final IModuleListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void saveData() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setData(final byte[] data) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setInitialized() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void writeSetting(final String key, final String value) {
    throw new IllegalStateException("Error: Not yet implemented");
  }
}
