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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class FunctionNotificationContainer {

  final private Integer moduleId;
  final private INaviModule module;
  final private IAddress functionAddress;
  final private String databaseOperation;

  public FunctionNotificationContainer(final Integer moduleId, final INaviModule module,
      final IAddress functionAddress, final String databaseOperation) {
    this.moduleId =
        Preconditions.checkNotNull(moduleId, "IE02625: moduleId argument can not be null.");
    this.module = Preconditions.checkNotNull(module, "IE02626: module argument can not be null.");
    this.functionAddress =
        Preconditions.checkNotNull(functionAddress,
            "Error: functionAddress argument can not be null");
    this.databaseOperation =
        Preconditions.checkNotNull(databaseOperation,
            "Error: databaseOperation argument can not be null");
  }

  public Integer getModuleId() {
    return moduleId;
  }

  public INaviModule getModule() {
    return module;
  }

  public IAddress getFunctionAddress() {
    return functionAddress;
  }

  public String getDatabaseOperation() {
    return databaseOperation;
  }
}
