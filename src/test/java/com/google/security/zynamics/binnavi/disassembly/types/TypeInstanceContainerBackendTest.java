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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.UniqueTestUserGenerator;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

@RunWith(JUnit4.class)
public class TypeInstanceContainerBackendTest {
  private final MockSqlProvider provider = new MockSqlProvider();
  private final MockModule module = new MockModule(provider);
  private TypeManager typeManager;
  private SectionContainer sectionContainer;
  private TypeInstanceContainerBackend typeInstancContainerBackend;
  private Section section;
  private BaseType baseType;
  private TypeInstance typeInstance;
  private INaviView view;

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceReferenceTest1() throws CouldntSaveDataException {
    typeInstancContainerBackend.createTypeInstanceReference(null, -1, null, null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceReferenceTest2() throws CouldntSaveDataException {
    typeInstancContainerBackend.createTypeInstanceReference(new CAddress("1234", 16), -1, null,
        null, view);
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceReferenceTest3() throws CouldntSaveDataException {
    typeInstancContainerBackend.createTypeInstanceReference(new CAddress("1234", 16), 1,
        new MockOperandTreeNode(), null, view);
  }

  @Test
  public void createTypeInstanceReferenceTest4() throws CouldntSaveDataException {
    final MockOperandTreeNode treeNode = new MockOperandTreeNode();
    final TypeInstanceReference typeInstanceReference =
        typeInstancContainerBackend.createTypeInstanceReference(new CAddress("1234", 16), 1,
            treeNode, typeInstance, view);

    Assert.assertNotNull(typeInstanceReference);
    Assert.assertEquals(new CAddress("1234", 16), typeInstanceReference.getAddress());
    Assert.assertEquals(1, typeInstanceReference.getPosition());
    Assert.assertEquals(Optional.of(treeNode), typeInstanceReference.getTreeNode());
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceTest1() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(null, null, null, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceTest2() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", null, null, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceTest3() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", " TYPE INSTANCE COMMENT ",
        null, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstanceTest4() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", " TYPE INSTANCE COMMENT ",
        baseType, null, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceTest5() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", " TYPE INSTANCE COMMENT ",
        baseType, section, -1);
  }

  @Test
  public void createTypeInstanceTest6() throws CouldntSaveDataException, CouldntLoadDataException {
    final TypeInstance typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", null, baseType, section,
            0);
    Assert.assertEquals(typeInstance.getName(), " TYPE INSTANCE ");
    Assert.assertEquals(typeInstance.getBaseType(), baseType);
    Assert.assertEquals(typeInstance.getSection(), section);
    Assert.assertEquals(typeInstance.getAddress().getOffset(), 0);
  }

  @Test
  public void createTypeInstanceTest7() throws CouldntSaveDataException, CouldntLoadDataException {
    final TypeInstance typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ",
            " TYPE INSTANCE COMMENT ", baseType, section, 0);
    Assert.assertEquals(typeInstance.getName(), " TYPE INSTANCE ");
    Assert.assertEquals(typeInstance.getBaseType(), baseType);
    Assert.assertEquals(typeInstance.getSection(), section);
    Assert.assertEquals(typeInstance.getAddress().getOffset(), 0);
  }

  @Test(expected = NullPointerException.class)
  public void deleteInstanceReferenceTest1() throws CouldntDeleteException {
    typeInstancContainerBackend.deleteInstanceReference(null);
  }

  @Test
  public void deleteInstanceReferenceTest2()
      throws CouldntSaveDataException, CouldntDeleteException {
    final MockOperandTreeNode treeNode = new MockOperandTreeNode();
    final CAddress address = new CAddress(0x1234);
    final int position = 1;
    final TypeInstanceReference typeInstanceReference =
        typeInstancContainerBackend.createTypeInstanceReference(
            address, position, treeNode, typeInstance, view);
    typeInstancContainerBackend.deleteInstanceReference(typeInstanceReference);
  }

  @Test(expected = NullPointerException.class)
  public void deleteInstanceTest1() throws CouldntDeleteException {
    typeInstancContainerBackend.deleteInstance(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteInstanceTest2() throws CouldntDeleteException {
    final TypeInstance instance = new TypeInstance(500, "FOO", baseType, section, 0, module);
    typeInstancContainerBackend.deleteInstance(instance);
  }

  @Test
  public void deleteInstanceTest3()
      throws CouldntSaveDataException, CouldntLoadDataException, CouldntDeleteException {
    final TypeInstance typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ",
            " TYPE INSTANCE COMMENT ", baseType, section, 0);
    typeInstancContainerBackend.deleteInstance(typeInstance);
  }

  @Test
  public void loadTypeInstancesTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    final int typeInstancesSize = typeInstancContainerBackend.loadTypeInstances().size();
    final BaseType baseType1 = typeManager.createAtomicType("uint_32", 32, false);
    final BaseType baseType2 = typeManager.createAtomicType("uint_64", 64, false);
    final BaseType baseType3 = typeManager.createAtomicType("sint_32", 32, true);
    final BaseType baseType4 = typeManager.createAtomicType("sint_64", 64, true);
    typeInstancContainerBackend.createTypeInstance("INSTANCE 1", null, baseType1, section, 1);
    typeInstancContainerBackend.createTypeInstance("INSTANCE 2", null, baseType2, section, 2);
    typeInstancContainerBackend.createTypeInstance("INSTANCE 3", null, baseType3, section, 3);
    typeInstancContainerBackend.createTypeInstance("INSTANCE 4", null, baseType4, section, 4);
    final Set<TypeInstance> typeInstances = typeInstancContainerBackend.loadTypeInstances();
    Assert.assertEquals(typeInstancesSize + 4, typeInstances.size());
    Assert.assertNotNull(Iterables.find(typeInstances, new Predicate<TypeInstance>() {
      @Override
      public boolean apply(final TypeInstance instance) {
        return instance.getName().equals("INSTANCE 1");
      }
    }));
    Assert.assertNotNull(Iterables.find(typeInstances, new Predicate<TypeInstance>() {
      @Override
      public boolean apply(final TypeInstance instance) {
        return instance.getName().equals("INSTANCE 2");
      }
    }));
    Assert.assertNotNull(Iterables.find(typeInstances, new Predicate<TypeInstance>() {
      @Override
      public boolean apply(final TypeInstance instance) {
        return instance.getName().equals("INSTANCE 3");
      }
    }));
    Assert.assertNotNull(Iterables.find(typeInstances, new Predicate<TypeInstance>() {
      @Override
      public boolean apply(final TypeInstance instance) {
        return instance.getName().equals("INSTANCE 4");
      }
    }));

  }

  @Test(expected = NullPointerException.class)
  public void setInstanceNameTest1() throws CouldntSaveDataException, CouldntLoadDataException {
    final TypeInstance typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ",
            " TYPE INSTANCE COMMENT ", baseType, section, 0);
    typeInstancContainerBackend.setInstanceName(typeInstance, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setInstanceNameTest2() throws CouldntSaveDataException, CouldntLoadDataException {
    final TypeInstance typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ",
            " TYPE INSTANCE COMMENT ", baseType, section, 0);
    typeInstancContainerBackend.setInstanceName(typeInstance, "");
  }

  @Test
  public void setInstanceNameTest3() throws CouldntSaveDataException, CouldntLoadDataException {
    typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ", " TYPE INSTANCE COMMENT ",
        baseType, section, 0);
    typeInstancContainerBackend.setInstanceName(typeInstance, " NEW NAME ");
    final Set<TypeInstance> instances = typeInstancContainerBackend.loadTypeInstances();
    final TypeInstance instance = Iterables.find(instances, new Predicate<TypeInstance>() {
      @Override
      public boolean apply(final TypeInstance instance) {
        return instance.getName().equals(" NEW NAME ");
      }
    });
    Assert.assertNotNull(instance);
    Assert.assertEquals(" NEW NAME ", instance.getName());
    Assert.assertEquals(baseType, instance.getBaseType());
    Assert.assertEquals(section, instance.getSection());
  }

  @Before
  public void setUp() throws CouldntLoadDataException, CouldntSaveDataException {
    typeManager = new TypeManager(new TypeManagerMockBackend());
    sectionContainer = new SectionContainer(new SectionContainerBackend(provider, module));
    section =
        sectionContainer.createSection(" SECTION NAME ", new CAddress("100", 16), new CAddress(
            "200", 16), SectionPermission.READ_WRITE_EXECUTE, new byte[] {});
    typeInstancContainerBackend =
        new TypeInstanceContainerBackend(provider, module, typeManager, sectionContainer);
    new UniqueTestUserGenerator(provider).nextActiveUser();
    baseType = typeManager.getTypes().get(0);
    Assert.assertNotNull(baseType);
    typeInstance =
        typeInstancContainerBackend.createTypeInstance(" TYPE INSTANCE ",
            " TYPE INSTANCE COMMENT ", baseType, section, 0);
    Assert.assertNotNull(typeInstancContainerBackend);
    view = module.getContent().getViewContainer().getViews().get(0);
  }

  @Test(expected = NullPointerException.class)
  public void TypeInstanceContainerBackendTestConstructor1() {
    new TypeInstanceContainerBackend(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void TypeInstanceContainerBackendTestConstructor2() {
    new TypeInstanceContainerBackend(provider, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void TypeInstanceContainerBackendTestConstructor3() {
    new TypeInstanceContainerBackend(provider, module, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void TypeInstanceContainerBackendTestConstructor4() {
    new TypeInstanceContainerBackend(provider, module, typeManager, null);
  }

  @Test
  public void TypeInstanceContainerBackendTestConstructor5() {
    final TypeInstanceContainerBackend typeInstancContainerBackend =
        new TypeInstanceContainerBackend(provider, module, typeManager, sectionContainer);
    Assert.assertNotNull(typeInstancContainerBackend);
  }
}
