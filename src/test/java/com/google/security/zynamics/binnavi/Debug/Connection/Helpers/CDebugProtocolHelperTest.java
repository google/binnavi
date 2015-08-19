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
package com.google.security.zynamics.binnavi.Debug.Connection.Helpers;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Debug.Connection.CMockReader;
import com.google.security.zynamics.binnavi.debug.connection.helpers.DebugProtocolHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;


@RunWith(JUnit4.class)
public final class CDebugProtocolHelperTest {
  @Test
  public void testReadDword() throws IOException {
    final CMockReader reader =
        new CMockReader(new byte[][] {{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}});

    reader.next();

    assertEquals(0xFFFFFFFFL, DebugProtocolHelper.readDWord(reader));
  }
}
