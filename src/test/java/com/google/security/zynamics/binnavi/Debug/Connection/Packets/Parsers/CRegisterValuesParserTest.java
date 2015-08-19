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
package com.google.security.zynamics.binnavi.Debug.Connection.Packets.Parsers;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class CRegisterValuesParserTest {
  @Test
  public void test() throws MessageParserException {
    final RegisterValues information =
        RegisterValuesParser
            .parse("<Registers><Thread id=\"5\"><Register name=\"eax\" value=\"123\" memory=\"\"/></Thread></Registers>"
                .getBytes());

    assertEquals("eax", information.getThreads().get(0).getRegisters().get(0).getName());
  }

  @Test
  public void test2() throws MessageParserException {
    @SuppressWarnings("unused")
    final RegisterValues information =
        RegisterValuesParser
            .parse("<Registers><Thread id=\"2988\"><Register name=\"EAX\" value=\"1a1eb4\" memory=\"\"/><Register name=\"EBX\" value=\"7ffda000\" memory=\"\"/><Register name=\"ECX\" value=\"7\" memory=\"\"/><Register name=\"EDX\" value=\"80\" memory=\"\"/><Register name=\"ESI\" value=\"1a1f48\" memory=\"\"/><Register name=\"EDI\" value=\"1a1eb4\" memory=\"\"/><Register name=\"ESP\" value=\"7fb20\" memory=\"\" sp=\"true\"/><Register name=\"EBP\" value=\"7fc94\" memory=\"\"/><Register name=\"EIP\" value=\"7c91120f\" memory=\"\" pc=\"true\"/><Register name=\"EFLAGS\" value=\"202\" memory=\"\"/><Register name=\"CF\" value=\"0\" memory=\"\"/><Register name=\"PF\" value=\"0\" memory=\"\"/><Register name=\"AF\" value=\"0\" memory=\"\"/><Register name=\"ZF\" value=\"0\" memory=\"\"/><Register name=\"SF\" value=\"0\" memory=\"\"/><Register name=\"OF\" value=\"0\" memory=\"\"/></Thread></Registers>"
                .getBytes());
  }

  @Test
  public void test3() throws MessageParserException {
    final RegisterValues information =
        RegisterValuesParser
            .parse("<Registers><Thread id=\"3607910890\"><Register name=\"R0\" value=\"0\" memory=\"\"/><Register name=\"R1\" value=\"96722c80\" memory=\"\"/><Register name=\"R2\" value=\"6000001f\" memory=\"\"/><Register name=\"R3\" value=\"ffffc800\" memory=\"\"/><Register name=\"R4\" value=\"2\" memory=\"\"/><Register name=\"R5\" value=\"96101c30\" memory=\"\"/><Register name=\"R6\" value=\"0\" memory=\"\"/><Register name=\"R7\" value=\"0\" memory=\"\"/><Register name=\"R8\" value=\"ffffc894\" memory=\"\"/><Register name=\"R9\" value=\"964c1e70\" memory=\"\"/><Register name=\"R10\" value=\"1\" memory=\"\"/><Register name=\"R11\" value=\"9661f2b4\" memory=\"\"/><Register name=\"R12\" value=\"88045644\" memory=\"\"/><Register name=\"R13(SP)\" value=\"3e02fdf4\" memory=\"\" sp=\"true\"/><Register name=\"R14(LR)\" value=\"88045644\" memory=\"\"/><Register name=\"R15(PC)\" value=\"88045644\"  memory=\"\" pc=\"true\"/><Register name=\"R16(PSR)\" value=\"6000001f\" memory=\"\"/></Thread></Registers>"
                .getBytes());

    assertEquals(3607910890L, information.getThreads().get(0).getTid());
  }
}
