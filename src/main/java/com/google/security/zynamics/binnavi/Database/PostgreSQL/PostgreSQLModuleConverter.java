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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractModuleCreator;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLModuleCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLNativeViewCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;

public class PostgreSQLModuleConverter extends AbstractModuleCreator {
  /**
   * The connection to the database.
   */
  private final SQLProvider m_provider;

  /**
   * Creates a new module converter object.
   * 
   * @param provider The connection to the database where the conversion process happens.
   */
  public PostgreSQLModuleConverter(final SQLProvider provider) {
    m_provider =
        Preconditions.checkNotNull(provider, "IE02042: Connection argument can not be null");
  }

  @Override
  protected void connectViewsFunctions(final int moduleId, final int firstViewId)
      throws SQLException {
    PostgreSQLModuleCreator
        .connectViewsFunctions(m_provider.getConnection(), moduleId, firstViewId);
  }

  @Override
  protected void createNativeCallgraphNodes(final int moduleId, final int viewId)
      throws SQLException {
    PostgreSQLNativeViewCreator.createNativeCallgraphNodes(m_provider.getConnection(), viewId,
        moduleId);
  }

  @Override
  protected int createNativeCallgraphView(final int moduleId) throws SQLException {
    return PostgreSQLNativeViewCreator.createNativeCallgraphView(m_provider.getConnection(),
        moduleId);
  }

  @Override
  protected void createNativeCodeNodes(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLNativeViewCreator.createNativeCodeNodes(m_provider.getConnection(), rawModuleId,
        moduleId);
  }

  @Override
  protected void createNativeFlowgraphEdges(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLNativeViewCreator.createNativeFlowgraphEdges(m_provider.getConnection(), rawModuleId,
        moduleId);
  }

  @Override
  protected int createNativeFlowgraphViews(final int moduleId) throws SQLException {
    return PostgreSQLNativeViewCreator.createNativeFlowgraphViews(m_provider.getConnection(),
        moduleId);
  }

  @Override
  protected int createNewModule(final INaviRawModule rawModule) throws SQLException {
    return PostgreSQLModuleCreator.createNewModule(m_provider.getConnection(), rawModule.getId());
  }

  @Override
  protected SQLProvider getProvider() {
    return m_provider;
  }

  @Override
  protected void importAddressReferences(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLDataImporter.importAddressReferences(m_provider.getConnection(), rawModuleId,
        moduleId);
  }

  @Override
  protected void importFunctions(final int rawModuleId, final int moduleId) throws SQLException {
    PostgreSQLDataImporter.importFunctions(m_provider.getConnection(), rawModuleId, moduleId);
  }
}
