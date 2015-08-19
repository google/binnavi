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
package com.google.security.zynamics.binnavi;

import com.google.security.zynamics.zylib.io.FileUtils;

import java.io.IOException;

public final class CConfigLoader {
  public static String[] loadPostgreSQL() throws IOException {
    if (FileUtils.exists("tests/postgresql.txt")) {
      String text = FileUtils.readTextfile("tests/postgresql.txt");

      text = text.replaceAll("\r", "");

      return text.split("\\n");
    } else {
      // Check cl/46987242 for detail on why there is an IP address here
      // instead of a host name.
      return new String[] {"127.0.0.1", "user", "CENSORED", "test_user"};
    }
  }
}
