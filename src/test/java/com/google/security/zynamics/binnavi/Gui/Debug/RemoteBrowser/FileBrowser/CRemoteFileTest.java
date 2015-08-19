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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser.CRemoteFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;


@RunWith(JUnit4.class)
public class CRemoteFileTest {
  @Test
  public void test1Simple() throws IOException {
    final CRemoteFile remoteFile = new CRemoteFile("C", true);

    try {
      remoteFile.canExecute();
      fail();
    } catch (final IllegalStateException e) {
    }

    try {
      remoteFile.canRead();
      fail();
    } catch (final IllegalStateException e) {
    }

    try {
      remoteFile.listFiles();
      fail();
    } catch (final IllegalStateException e) {
    }

    assertFalse(remoteFile.canWrite());

    final File foo = new File("foo");
    assertFalse(remoteFile.renameTo(foo));
    assertTrue(remoteFile.exists());

    assertEquals("C", remoteFile.getAbsolutePath());

    final File second = remoteFile.getAbsoluteFile();

    assertEquals(second.getAbsolutePath(), remoteFile.getAbsolutePath());
    assertTrue(second.isDirectory());

    final File third = remoteFile.getCanonicalFile();

    assertEquals(third.getCanonicalPath(), remoteFile.getCanonicalPath());
    assertTrue(third.isDirectory());

    assertEquals(null, remoteFile.getParentFile());

    assertEquals(0, remoteFile.lastModified());
    assertEquals(0, remoteFile.length());

  }
}
