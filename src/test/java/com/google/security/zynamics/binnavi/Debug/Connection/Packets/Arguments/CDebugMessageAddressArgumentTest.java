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
package com.google.security.zynamics.binnavi.Debug.Connection.Packets.Arguments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugMessageAddressArgument;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CDebugMessageAddressArgumentTest {
  @Test
  public void testConstructor() {
    try {
      new DebugMessageAddressArgument(null);
      fail();
    } catch (final NullPointerException e) {
    }

    final CAddress address = new CAddress(0x123);

    final DebugMessageAddressArgument argument = new DebugMessageAddressArgument(address);

    assertEquals(address, argument.getAddress());
    assertEquals("00000123", argument.toString());
  }
}
