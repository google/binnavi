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
package com.google.security.zynamics.binnavi.Database.MockClasses;

import java.math.BigInteger;
import java.util.ArrayList;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public final class MockCodeNodeData {
  public IAddress address = new CAddress(BigInteger.valueOf(0));
  public int borderColor = 0;
  public int color = 0;
  public byte[] data = new byte[] {(byte) 0x90};
  public int expressionId = 0;
  public ArrayList<IComment> globalNodeComment = null;
  public String immediate = "";
  public ArrayList<IComment> instructionComment = null;
  public int expressionType = 0;
  public ArrayList<IComment> localInstructionComment = null;
  public ArrayList<IComment> localNodeComment = null;
  public String mnemonic = "nop";
  public int module = 1;
  public int nodeId = 0;
  public Integer operandPosition = null;
  public IAddress parentFunction = new CAddress(BigInteger.valueOf(0));
  public Integer parentId = 0;
  public CReference reference = null;
  public String replacement = null;
  public boolean selected = false;
  public String symbol = "";
  public boolean visible = true;
  public int operandId = 0;
  public String architecture = "x86-32";
  public Integer typeInstanceId = null;
}
