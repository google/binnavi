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

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModuleConfiguration;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.Date;

public final class MockModuleConfiguration implements INaviModuleConfiguration {
  private MockDebugger m_debugger;
  private final CRawModule m_rawModule;

  public MockModuleConfiguration(final SQLProvider provider) {
    m_rawModule = new CRawModule(1, "", 0, false, provider);
  }

  @Override
  public Date getCreationDate() {
    return new Date();
  }

  @Override
  public IDebugger getDebugger() {
    return m_debugger;
  }

  @Override
  public DebuggerTemplate getDebuggerTemplate() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String getDescription() {
    return "Mock Module Description";
  }

  @Override
  public IAddress getFileBase() {
    return new CAddress(0);
  }

  @Override
  public int getId() {
    return 1;
  }

  @Override
  public IAddress getImageBase() {
    return new CAddress(0);
  }

  @Override
  public String getMD5() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Date getModificationDate() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String getName() {
    return "Mock Module";
  }

  @Override
  public INaviRawModule getRawModule() {
    return m_rawModule;
  }

  @Override
  public String getSha1() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isStared() {
    throw new IllegalStateException("Not yet implemented");
  }

  public void setDebugger(final MockDebugger debugger) {
    m_debugger = debugger;
  }

  @Override
  public void setDebuggerTemplate(final DebuggerTemplate template) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setDescription(final String description) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setFileBase(final IAddress fileBase) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setImageBase(final IAddress imageBase) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setName(final String name) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setStared(final boolean stared) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void updateModificationDate() {
  }
}
