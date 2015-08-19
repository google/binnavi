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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({AddressTest.class, AddressSpaceTest.class, BasicBlockTest.class,
    BlockEdgeTest.class, CallgraphTest.class, CodeNodeTest.class, DatabaseTest.class,
    DatabaseManagerTest.class, DebuggerTemplateTest.class, DebuggerTemplateManagerTest.class,
    ExpressionTypeTest.class, FlowgraphTest.class, FunctionTest.class, FunctionBlockTest.class,
    FunctionEdgeTest.class, FunctionNodeTest.class, FunctionTypeTest.class, GraphTypeTest.class,
    GroupNodeTest.class, InstructionTest.class, ModuleHelpersTest.class, ModuleTest.class,
    OperandTest.class, OperandExpressionTest.class, ProjectTest.class, ReferenceTest.class,
    ReferenceTypeTest.class, TagTest.class, TagManagerTest.class, TagTypeTest.class,
    TextNodeTest.class, TraceEventTypeTest.class, TraceTest.class, TraceEventTest.class,
    ViewTest.class, View2DTest.class, ViewEdgeTest.class, ViewGraphTest.class,
    ViewGraphHelpersTest.class, ViewNodeTest.class, ViewTypeTest.class})
public final class AllTests {
}
