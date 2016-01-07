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
package com.google.security.zynamics.reil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;

public enum OperandSize {
  EMPTY(0), BYTE(1), WORD(2), DWORD(4), QWORD(8), OWORD(16), ADDRESS(-1);

  private final int value;

  private static final ImmutableBiMap<String, OperandSize> regularOperandSizeMap =
      new ImmutableBiMap.Builder<String, OperandSize>()
          .put("byte", OperandSize.BYTE)
          .put("word", OperandSize.WORD)
          .put("dword", OperandSize.DWORD)
          .put("qword", OperandSize.QWORD)
          .put("oword", OperandSize.OWORD)
          .build();

  private static final
      ImmutableBiMap<String, OperandSize> completeOperandSizeMap = new ImmutableBiMap.Builder<
          String, OperandSize>().putAll(regularOperandSizeMap).put("", OperandSize.EMPTY)
          .put("address", OperandSize.ADDRESS).build();

  private OperandSize(final int value) {
    this.value = value;
  }

  public static boolean isSizeString(final String value) {
    return regularOperandSizeMap.containsKey(value);
  }

  public static OperandSize sizeStringToValue(final String value) {
    return completeOperandSizeMap.get(value);
  }

  public int getBitSize() {
    return getByteSize() * 8;
  }

  public int getByteSize() {
    Preconditions.checkArgument(this != OperandSize.ADDRESS,
        "Error: address does not have a size associated to it.");
    return value;
  }

  public String toSizeString() {
    return completeOperandSizeMap.inverse().get(this);
  }
}
