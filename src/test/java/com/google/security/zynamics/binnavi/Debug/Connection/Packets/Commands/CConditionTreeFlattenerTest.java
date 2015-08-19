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
package com.google.security.zynamics.binnavi.Debug.Connection.Packets.Commands;

import static org.junit.Assert.assertArrayEquals;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.conditions.ConditionTreeFlattener;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointConditionParser;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CConditionTreeFlattenerTest {
  @Test
  public void testFlattening() throws RecognitionException, MaybeNullException {
    final ConditionNode node = BreakpointConditionParser.parse("eax == 2 && (1 + 5) != [3]");

    final byte[] result = ConditionTreeFlattener.flatten(node);

    final byte[] expected = {0, 0, 0, 0, // IDENTIFIER_NODE '&&'
        0, 0, 0, 2, // Payload size
        38, 38, // Payload '&&'
        0, 0, 0, 2, // Child count
        0, 0, 0, 1, // ID Child 1
        0, 0, 0, 4, // ID Child 2

        0, 0, 0, 5, // RELATION_NODE '=='
        0, 0, 0, 2, // Payload size
        61, 61, // Payload '=='
        0, 0, 0, 2, // Child count
        0, 0, 0, 2, // ID Child 1
        0, 0, 0, 3, // ID Child 2

        0, 0, 0, 2, // IDENTIFIER_NODE 'eax'
        0, 0, 0, 3, // Payload size
        101, 97, 120, // Payload 'eax'
        0, 0, 0, 0, // Child count

        0, 0, 0, 4, // ID_NUMBER_NODE '2'
        0, 0, 0, 4, // Payload size
        0, 0, 0, 2, // Payload '2'
        0, 0, 0, 0, // Child count

        0, 0, 0, 5, // RELATION_NODE '!='
        0, 0, 0, 2, // Payload size
        33, 61, // Payload '=='
        0, 0, 0, 2, // Child count
        0, 0, 0, 5, // ID Child 1
        0, 0, 0, 9, // ID Child 2

        0, 0, 0, 6, // ID_SUB_NODE '('
        0, 0, 0, 0, // Payload size
        0, 0, 0, 1, // Child count
        0, 0, 0, 6, // ID Child 1

        0, 0, 0, 1, // FORMULA_NODE '+'
        0, 0, 0, 1, // Payload size
        43, // Payload '+'
        0, 0, 0, 2, // Child count
        0, 0, 0, 7, // ID Child 1
        0, 0, 0, 8, // ID Child 2

        0, 0, 0, 4, // ID_NUMBER_NODE '1'
        0, 0, 0, 4, // Payload size
        0, 0, 0, 1, // Payload '1'
        0, 0, 0, 0, // Child count

        0, 0, 0, 4, // ID_NUMBER_NODE '5'
        0, 0, 0, 4, // Payload size
        0, 0, 0, 5, // Payload '5'
        0, 0, 0, 0, // Child count

        0, 0, 0, 3, // ID_MEMORY_NODE '['
        0, 0, 0, 0, // Payload size
        0, 0, 0, 1, // Child count
        0, 0, 0, 10, // ID Child 1

        0, 0, 0, 4, // ID_NUMBER_NODE '3'
        0, 0, 0, 4, // Payload size
        0, 0, 0, 3, // Payload '3'
        0, 0, 0, 0 // Child count
        };

    assertArrayEquals(expected, result);
  }
}
