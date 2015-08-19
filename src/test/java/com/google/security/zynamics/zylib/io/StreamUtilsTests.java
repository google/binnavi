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
package com.google.security.zynamics.zylib.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Tests for the {@link StreamUtils} class.
 * 
 * @author cblichmann@google.com (Christian Blichmann)
 */
@RunWith(JUnit4.class)
public class StreamUtilsTests {
  @Test
  public void testReadLinesFromReader() throws IOException {
    final Reader validReader = new StringReader("3.2.2\n2011-08-03");
    final String[] expected = new String[] {"3.2.2", "2011-08-03"};
    assertArrayEquals(expected, StreamUtils.readLinesFromReader(validReader).toArray());

    final Reader emptyReader = new StringReader("");
    assertTrue(StreamUtils.readLinesFromReader(emptyReader).isEmpty());
  }
}
