/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.debug.connection.packets.arguments;

import java.math.BigInteger;

/**
 * Class that can be used to add long values to debug message arguments.
 */
public class DebugMessageLongArgument extends DebugMessageArgument {
  public DebugMessageLongArgument(final long value) {
    super(DebugArgumentType.LONG);
    appendLong(BigInteger.valueOf(value));
  }
}
