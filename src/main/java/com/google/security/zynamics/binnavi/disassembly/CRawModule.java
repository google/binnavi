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
package com.google.security.zynamics.binnavi.disassembly;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;

/**
 * Represents a raw module in a database. A raw module is a module which was exported using one of
 * the available exporters (IDA, OllyDbg, Immunity Debugger, ...).
 */
public final class CRawModule implements INaviRawModule {
  /**
   * The ID of the raw module as it can be found in the database.
   */
  private final int m_id;

  /**
   * The name of the raw module.
   */
  private final String m_name;

  /**
   * The SQL provider that is used to communicate with the database where the raw module is stored.
   */
  private final SQLProvider m_provider;

  /**
   * Number of functions in the raw module.
   */
  private final int m_functionCount;

  /**
   * Flag that indicates whether the raw module is complete or not.
   */
  private final boolean m_isComplete;

  /**
   * Creates a new raw module object.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param name The name of the module.
   * @param functionCount Number of functions in the raw module.
   * @param isComplete True, if the raw module is complete. False, otherwise.
   * @param provider The SQL provider that is used to communicate with the database.
   */
  public CRawModule(final int rawModuleId, final String name, final int functionCount,
      final boolean isComplete, final SQLProvider provider) {
    Preconditions.checkArgument(rawModuleId > 0, "IE00258: Module ID must be positive");
    m_name = Preconditions.checkNotNull(name, "IE00259: Module Name can't be null");
    m_provider = Preconditions.checkNotNull(provider, "IE00265: Invalid SQL provider");

    m_id = rawModuleId;
    m_functionCount = functionCount;
    m_isComplete = isComplete;
  }

  @Override
  public int getFunctionCount() {
    return m_functionCount;
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public String getName() {
    return m_name;
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
  public boolean isComplete() {
    return m_isComplete;
  }
}
