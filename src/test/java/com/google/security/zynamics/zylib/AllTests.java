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
package com.google.security.zynamics.zylib;

import com.google.security.zynamics.zylib.disassembly.AddressTests;
import com.google.security.zynamics.zylib.general.ConvertTests;
import com.google.security.zynamics.zylib.gui.license.UpdateCheckHelperTest;
import com.google.security.zynamics.zylib.io.FileUtilsTests;
import com.google.security.zynamics.zylib.io.StreamUtilsTests;
import com.google.security.zynamics.zylib.types.graphs.LengauerTarjanTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AddressTests.class,
    ConvertTests.class,
    UpdateCheckHelperTest.class,
    FileUtilsTests.class,
    StreamUtilsTests.class,
    LengauerTarjanTest.class})
public final class AllTests {
}
