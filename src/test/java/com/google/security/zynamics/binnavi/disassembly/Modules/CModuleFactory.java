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

import java.util.Date;

import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

public class CModuleFactory {
  public static CModule get() {
    final MockSqlProvider provider = new MockSqlProvider();

    return new CModule(123, "Name", "Comment", new Date(), new Date(),
        "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
        new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
            "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
  }
}
