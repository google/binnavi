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
package com.google.security.zynamics.binnavi.models.Bookmarks.memory;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public final class CBookmarkTest {
  @Test(expected = NullPointerException.class)
  public void testConstructor1() {
    new CBookmark(null, "MyDescription");
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor2() {
    new CBookmark(new CAddress(BigInteger.ZERO), null);
  }

  @Test
  public void testConstructor3() {
    final CBookmark bookmark =
        new CBookmark(new CAddress(BigInteger.valueOf(123)), "MyDescription");

    assertEquals(BigInteger.valueOf(123), bookmark.getAddress().toBigInteger());
    assertEquals("MyDescription", bookmark.getDescription());
  }

  @Test
  public void testSetDescription() {
    final CBookmark bookmark =
        new CBookmark(new CAddress(BigInteger.valueOf(123)), "MyDescription");

    bookmark.setDescription("MySecondDescription");

    assertEquals("MySecondDescription", bookmark.getDescription());

  }

  @Test(expected = NullPointerException.class)
  public void testSetDescriptionFail() {
    final CBookmark bookmark =
        new CBookmark(new CAddress(BigInteger.valueOf(123)), "MyDescription");
    bookmark.setDescription(null);
  }
}
