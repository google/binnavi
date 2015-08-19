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
package com.google.security.zynamics.zylib.gui.license;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the {@link UpdateCheckHelper} class.
 * 
 * @author cblichmann@google.com (Christian Blichmann)
 */
@RunWith(JUnit4.class)
public class UpdateCheckHelperTest {
  @Test
  public void testVersionCompare() {
    assertEquals(UpdateCheckHelper.versionCompare("4.0.0", "4.0.0"), 0);
    assertEquals(UpdateCheckHelper.versionCompare("4.0.0", "4.0.1"), -1);
    assertEquals(UpdateCheckHelper.versionCompare("4.0.0", "3.2.2"), 1);
    assertEquals(UpdateCheckHelper.versionCompare("4.0.0", "10.0 beta 1"), -1);
    assertEquals(UpdateCheckHelper.versionCompare("4.0 beta 2", "10.0 beta 1"), -1);
    assertEquals(UpdateCheckHelper.versionCompare("1.0 beta 2", "10.0 beta 1"), -1);
  }
}
