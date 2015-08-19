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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public class TypeInstanceTest {
  final SQLProvider provider = new MockSqlProvider();
  final INaviModule module = new MockModule(provider);
  final CommentManager commentManager = CommentManager.get(provider);
  final BaseType baseType = new BaseType(1, "NAME", 32, true, BaseTypeCategory.ATOMIC);
  final Section section = new Section(1, "SECTION", commentManager, module,
      new CAddress("100", 16), new CAddress("200", 16), SectionPermission.READ, null);

  @Test(expected = IllegalArgumentException.class)
  public void typeInstanceConstructorTest1() {
    new TypeInstance(-1, null, null, null, -1, null);
  }

  @Test
  public void typeInstanceConstructorTest10() {
    final TypeInstance typeInstance = new TypeInstance(1, null, baseType, section, 50, module);
    Assert.assertNotNull(typeInstance);
    Assert.assertEquals(1, typeInstance.getId());
    Assert.assertNull(typeInstance.getName());
    Assert.assertEquals(baseType, typeInstance.getBaseType());
    Assert.assertEquals(section, typeInstance.getSection());
    Assert.assertEquals(50, typeInstance.getAddress().getOffset());
    Assert.assertEquals(module, typeInstance.getModule());
    Assert.assertEquals(
        section.getStartAddress().toBigInteger()
        .add(BigInteger.valueOf(typeInstance.getAddress().getOffset())),
        BigInteger.valueOf(typeInstance.getAddress().getVirtualAddress()));
  }

  // TODO(timkornau) take a look at the discussion in the typeInstance we are currently not sure if
  // an empty name should be allowed or not.
  // @Test(expected = IllegalArgumentException.class)
  // public void typeInstanceConstructorTest2() {
  // new TypeInstance(1, "", null, null, -1, null);
  // }

  @Test(expected = NullPointerException.class)
  public void typeInstanceConstructorTest3() {
    new TypeInstance(1, null, null, null, -1, null);
  }

  @Test(expected = NullPointerException.class)
  public void typeInstanceConstructorTest4() {
    new TypeInstance(1, null, baseType, null, -1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void typeInstanceConstructorTest5() {
    new TypeInstance(1, null, baseType, section, -1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void typeInstanceConstructorTest6() {
    new TypeInstance(1, null, baseType, section, -1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void typeInstanceConstructorTest7() {
    new TypeInstance(1, null, baseType, section, 400, null);
  }

  @Test(expected = NullPointerException.class)
  public void typeInstanceConstructorTest8() {
    new TypeInstance(1, null, baseType, section, 50, null);
  }

  @Test(expected = NullPointerException.class)
  public void typeInstanceConstructorTest9() {
    new TypeInstance(1, null, baseType, section, 50, null);
  }
}
