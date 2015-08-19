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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.API.disassembly.DebuggerTemplate;
import com.google.security.zynamics.binnavi.API.disassembly.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.API.disassembly.IDebuggerTemplateManagerListener;

public final class MockDebuggerTemplateManagerListener implements IDebuggerTemplateManagerListener {
  public String events = "";

  @Override
  public void addedDebuggerTemplate(final DebuggerTemplateManager manager,
      final DebuggerTemplate template) {
    events += "addedDebuggerTemplate;";
  }

  @Override
  public void deletedDebuggerTemplate(final DebuggerTemplateManager manager,
      final DebuggerTemplate template) {
    events += "deletedDebuggerTemplate;";
  }
}
