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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.API.disassembly.IOperandExpressionListener;
import com.google.security.zynamics.binnavi.API.disassembly.OperandExpression;
import com.google.security.zynamics.binnavi.API.disassembly.Reference;

public final class MockOperandExpressionListener implements IOperandExpressionListener {
  public String events = "";

  @Override
  public void addedReference(final OperandExpression operandExpression, final Reference reference) {
    events += "addedReference;";
  }

  @Override
  public void changed(final OperandExpression operandExpression) {
    events += "changed;";
  }

  @Override
  public void removedReference(final OperandExpression operandExpression, final Reference reference) {
    events += "removedReference;";
  }

}
