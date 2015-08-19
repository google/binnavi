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
package com.google.security.zynamics.binnavi.Database.MockClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseContent;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.zylib.general.ListenerProvider;

public final class MockDatabaseContent implements IDatabaseContent {
  public List<INaviProject> m_projects = new ArrayList<INaviProject>();

  private final ListenerProvider<IDatabaseListener> m_listeners;

  private final DebuggerTemplateManager m_debuggerTemplateManager;

  private final IDatabase m_database;

  public List<INaviModule> m_modules = new ArrayList<INaviModule>();

  private final MockTagManager m_tagManager = new MockTagManager(TagType.VIEW_TAG);

  public List<INaviRawModule> m_rawModules = new ArrayList<INaviRawModule>();

  public MockDatabaseContent(final IDatabase database,
      final DebuggerTemplateManager debuggerTemplateManager,
      final ListenerProvider<IDatabaseListener> listeners) {
    m_database = database;
    m_debuggerTemplateManager = debuggerTemplateManager;
    m_listeners = listeners;
  }

  public void add(final CRawModule module) {
    m_rawModules.add(module);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedRawModules(m_database, new Stack<INaviRawModule>(),
            Lists.newArrayList((INaviRawModule) module));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  public void addModule(final INaviModule module) {
    m_modules.add(module);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.addedModule(m_database, module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  public void addProject(final CProject project) {
    m_projects.add(project);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.addedProject(m_database, project);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public INaviProject addProject(final String string) {
    final MockProject project = new MockProject();

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.addedProject(m_database, project);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return project;
  }

  @Override
  public void delete(final INaviModule module) {
    m_modules.remove(module);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedModule(m_database, module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void delete(final INaviProject project) {
    m_projects.remove(project);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedProject(m_database, project);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void delete(final INaviRawModule rawModule) {
    // TODO Auto-generated method stub
  }

  public void deleteProject(final INaviProject project) {
    m_projects.remove(project);

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.deletedProject(m_database, project);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public DebuggerTemplateManager getDebuggerTemplateManager() {
    return m_debuggerTemplateManager;
  }

  @Override
  public INaviModule getModule(final int moduleId) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviModule> getModules() {
    return m_modules;
  }

  @Override
  public ITagManager getNodeTagManager() {
    return new MockTagManager(TagType.NODE_TAG);
  }

  @Override
  public List<INaviProject> getProjects() {
    return m_projects;
  }

  @Override
  public List<INaviRawModule> getRawModules() {
    return new ArrayList<INaviRawModule>(m_rawModules);
  }

  @Override
  public ITagManager getViewTagManager() {
    return m_tagManager;
  }

  @Override
  public void refreshRawModules() {
    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedRawModules(m_database, new ArrayList<INaviRawModule>(),
            new ArrayList<INaviRawModule>());
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
