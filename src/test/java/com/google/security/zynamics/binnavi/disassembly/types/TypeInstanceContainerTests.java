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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TypeInstanceContainerTests {
  private final SQLProvider provider = new MockSqlProvider();
  private final INaviModule module = new MockModule(provider);
  private TypeManager typeManager;
  private final SectionContainerBackend sectionContainerBackend =
      new SectionContainerBackend(provider, module);
  private SectionContainer sectionContainer;
  private TypeInstanceContainerBackend typeInstanceContainerBackend;
  private BaseType baseType;
  private Section section;
  private INaviView view;
  private TypeInstanceContainer typeInstanceContainer;
  private final TypeInstanceContainerEvents listener = new TypeInstanceContainerEvents();
  private final TypeInstanceContainerEvents expectedEvents = new TypeInstanceContainerEvents();

  @Test(expected = NullPointerException.class)
  public void addListenerTest1() {
    typeInstanceContainer.addListener(null);
  }

  @Test
  public void addListenerTest2() {
    typeInstanceContainer.addListener(new TypeInstanceContainerEvents());
  }

  @Test(expected = NullPointerException.class)
  public void createInstanceTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance(null, null, null, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createInstanceTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance("TYPEINSTANCE", null, null, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createInstanceTest3() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, null, -1);
  }

  @Test(expected = NullPointerException.class)
  public void createInstanceTest4() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, null, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createInstanceTest5() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, -1);
  }

  @Test
  public void createInstanceTest6() throws CouldntLoadDataException, CouldntSaveDataException {
    final int numberOfTypeInstances = typeInstanceContainer.getTypeInstances().size();
    final TypeInstance typeInstance =
        typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    Assert.assertEquals(numberOfTypeInstances + 1, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance));
    Assert.assertEquals(typeInstance,
        typeInstanceContainer.getTypeInstance(typeInstance.getAddress()));

    expectedEvents.addedTypeInstance(typeInstance);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createInstanceTest7() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    typeInstanceContainer.createInstance("TYPEINSTANCE2", null, baseType, section, 0);
  }

  @Test(expected = NullPointerException.class)
  public void createReferenceTest1() throws CouldntSaveDataException {
    typeInstanceContainer.createReference(null, -1, null, null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createReferenceTest2() throws CouldntSaveDataException {
    typeInstanceContainer.createReference(new CAddress("41000", 16), -1, null, null, view);
  }

  @Test(expected = NullPointerException.class)
  public void createReferenceTest3() throws CouldntLoadDataException, CouldntSaveDataException {
    final TypeInstanceContainer typeInstanceContainer =
        new TypeInstanceContainer(typeInstanceContainerBackend, provider);
    Assert.assertNotNull(typeInstanceContainer);
    typeInstanceContainer.createReference(new CAddress("41000", 16), 0, null, null, view);
  }

  @Test(expected = NullPointerException.class)
  public void createReferenceTest4() throws CouldntSaveDataException {
    typeInstanceContainer.createReference(new CAddress("41000", 16), 0, new MockOperandTreeNode(),
        null, view);
  }

  @Test
  public void createReferenceTest5() throws CouldntLoadDataException, CouldntSaveDataException {
    final int numberOfTypeInstances = typeInstanceContainer.getTypeInstances().size();
    final TypeInstance typeInstance =
        typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    Assert.assertEquals(numberOfTypeInstances + 1, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance));
    Assert.assertEquals(typeInstance,
        typeInstanceContainer.getTypeInstance(typeInstance.getAddress()));
    expectedEvents.addedTypeInstance(typeInstance);
    Assert.assertEquals(expectedEvents, listener);

    Assert.assertEquals(0, typeInstanceContainer.getReferences(typeInstance).size());
    final TypeInstanceReference typeInstanceReference =
        typeInstanceContainer.createReference(new CAddress("41000", 16), 0,
            new MockOperandTreeNode(), typeInstance, view);
    Assert.assertNotNull(typeInstanceReference);
    Assert.assertEquals(1, typeInstanceContainer.getReferences(typeInstance).size());
    Assert.assertTrue(typeInstanceContainer.getReferences(typeInstance).contains(
        typeInstanceReference));

    expectedEvents.addedTypeInstanceReference(typeInstanceReference);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Test(expected = NullPointerException.class)
  public void deleteInstanceTest1() throws CouldntLoadDataException, CouldntDeleteException {
    final TypeInstanceContainer typeInstanceContainer =
        new TypeInstanceContainer(typeInstanceContainerBackend, provider);
    final TypeInstanceContainerEvents listener = new TypeInstanceContainerEvents();
    typeInstanceContainer.addListener(listener);
    typeInstanceContainer.deleteInstance((TypeInstance) null);
  }

  @Test
  public void deleteInstanceTest2()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final int numberOfTypeInstances = typeInstanceContainer.getTypeInstances().size();
    final TypeInstance typeInstance =
        typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    Assert.assertEquals(numberOfTypeInstances + 1, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance));
    Assert.assertEquals(typeInstance,
        typeInstanceContainer.getTypeInstance(typeInstance.getAddress()));
    expectedEvents.addedTypeInstance(typeInstance);
    Assert.assertEquals(expectedEvents, listener);

    typeInstanceContainer.deleteInstance(typeInstance);
    Assert.assertEquals(numberOfTypeInstances, typeInstanceContainer.getTypeInstances().size());
    Assert.assertFalse(typeInstanceContainer.getTypeInstances().contains(typeInstance));
    Assert.assertNull(typeInstanceContainer.getTypeInstance(typeInstance.getAddress()));
    expectedEvents.removedTypeInstance(typeInstance);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Test(expected = NullPointerException.class)
  public void deleteReferenceTest1() throws CouldntDeleteException {
    typeInstanceContainer.deleteReference(null);
  }

  @Test
  public void deleteReferenceTest2()
      throws CouldntLoadDataException, CouldntSaveDataException, CouldntDeleteException {
    final int numberOfTypeInstances = typeInstanceContainer.getTypeInstances().size();
    final TypeInstance typeInstance =
        typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    Assert.assertEquals(numberOfTypeInstances + 1, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance));
    Assert.assertEquals(typeInstance,
        typeInstanceContainer.getTypeInstance(typeInstance.getAddress()));
    Assert.assertEquals(0, typeInstanceContainer.getReferences(typeInstance).size());

    final TypeInstanceReference typeInstanceReference =
        typeInstanceContainer.createReference(new CAddress("41000", 16), 0,
            new MockOperandTreeNode(), typeInstance, view);
    Assert.assertEquals(1, typeInstanceContainer.getReferences(typeInstance).size());
    Assert.assertTrue(typeInstanceContainer.getReferences(typeInstance).contains(
        typeInstanceReference));
    typeInstanceContainer.deleteReference(typeInstanceReference);
    Assert.assertEquals(Lists.newArrayList(), typeInstanceContainer.getReferences(typeInstance));

    expectedEvents.addedTypeInstance(typeInstance);
    expectedEvents.addedTypeInstanceReference(typeInstanceReference);
    expectedEvents.removedTypeInstanceReference(typeInstanceReference);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Test(expected = NullPointerException.class)
  public void getTypeInstancesTest1() {
    typeInstanceContainer.getTypeInstances(null);
  }

  @Test
  public void getTypeInstancesTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final int numberOfTypeInstances = typeInstanceContainer.getTypeInstances().size();
    final TypeInstance typeInstance1 =
        typeInstanceContainer.createInstance("TYPEINSTANCE1", null, baseType, section, 0);
    Assert.assertEquals(numberOfTypeInstances + 1, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance1));
    Assert.assertEquals(typeInstance1,
        typeInstanceContainer.getTypeInstance(typeInstance1.getAddress()));

    final TypeInstance typeInstance2 =
        typeInstanceContainer.createInstance("TYPEINSTANCE2", null, baseType, section, 1);
    Assert.assertEquals(numberOfTypeInstances + 2, typeInstanceContainer.getTypeInstances().size());
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(typeInstance2));
    Assert.assertEquals(typeInstance2,
        typeInstanceContainer.getTypeInstance(typeInstance2.getAddress()));

    Assert.assertTrue(typeInstanceContainer.getTypeInstances(section).contains(typeInstance1));
    Assert.assertTrue(typeInstanceContainer.getTypeInstances(section).contains(typeInstance2));
    Assert.assertTrue(typeInstanceContainer.getTypeInstances()
        .containsAll(Lists.newArrayList(typeInstance1, typeInstance2)));

    expectedEvents.addedTypeInstance(typeInstance1);
    expectedEvents.addedTypeInstance(typeInstance2);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Test(expected = NullPointerException.class)
  public void removeListenerTest1() {
    typeInstanceContainer.removeListener(null);
  }

  @Test
  public void removeListenerTest2() {
    typeInstanceContainer.removeListener(listener);
  }

  @Test(expected = NullPointerException.class)
  public void setInstanceNameTest1() throws CouldntLoadDataException, CouldntSaveDataException {
    typeInstanceContainer.setInstanceName(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void setInstanceNameTest2() throws CouldntLoadDataException, CouldntSaveDataException {
    final TypeInstance instance =
        typeInstanceContainer.createInstance("TYPEINSTANCE1", null, baseType, section, 0);
    typeInstanceContainer.setInstanceName(instance, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setInstanceNameTest3() throws CouldntLoadDataException, CouldntSaveDataException {
    final TypeInstance instance =
        typeInstanceContainer.createInstance("TYPEINSTANCE1", null, baseType, section, 0);
    typeInstanceContainer.setInstanceName(instance, "");
  }

  @Test
  public void setInstanceNameTest4() throws CouldntLoadDataException, CouldntSaveDataException {
    final TypeInstance instance =
        typeInstanceContainer.createInstance("TYPEINSTANCE1", null, baseType, section, 0);
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(instance));
    expectedEvents.addedTypeInstance(instance);

    typeInstanceContainer.setInstanceName(instance, "NEW INSTANCE NAME");
    Assert.assertTrue(typeInstanceContainer.getTypeInstances().contains(instance));
    expectedEvents.changedTypeInstance(instance);
    Assert.assertEquals(expectedEvents, listener);
  }

  @Before
  public void setUp() throws CouldntLoadDataException, CouldntSaveDataException {
    typeManager = new TypeManager(new TypeManagerMockBackend());
    baseType = typeManager.getTypes().get(0);
    Assert.assertNotNull(baseType);
    sectionContainer = new SectionContainer(sectionContainerBackend);
    section =
        sectionContainer.createSection("SECTION", new CAddress("100", 16), new CAddress("200", 16),
            SectionPermission.READ, new byte[0x100]);
    view = module.getContent().getViewContainer().getViews().get(0);
    typeInstanceContainerBackend =
        new TypeInstanceContainerBackend(provider, module, typeManager, sectionContainer);
    typeInstanceContainer = new TypeInstanceContainer(typeInstanceContainerBackend, provider);
    typeInstanceContainer.addListener(listener);
  }

  @Test(expected = NullPointerException.class)
  public void typeInstanceContainerConstructorTest1() throws CouldntLoadDataException {
    new TypeInstanceContainer(null, null);
  }

  @Test
  public void typeInstanceContainerConstructorTest2() throws CouldntLoadDataException {
    Assert.assertNotNull(new TypeInstanceContainer(typeInstanceContainerBackend, provider));
  }

  @Test
  public void updateTypeReferenceTest() throws CouldntSaveDataException, CouldntLoadDataException {
    final TypeInstance typeInstance =
        typeInstanceContainer.createInstance("TYPEINSTANCE", null, baseType, section, 0);
    expectedEvents.addedTypeInstance(typeInstance);
    final TypeInstanceReference typeInstanceReference =
        typeInstanceContainer.createReference(new CAddress("41000", 16), 0,
            new MockOperandTreeNode(), typeInstance, view);
    expectedEvents.addedTypeInstanceReference(typeInstanceReference);
    typeInstanceContainer.deactivateTypeInstanceReference(typeInstanceReference);
    expectedEvents.changedTypeInstanceReference(typeInstanceReference);
    Assert.assertEquals(expectedEvents, listener);
  }

  private class TypeInstanceContainerEvents implements TypeInstanceContainerListener {

    private final StringBuilder events_ = new StringBuilder();
    private static final String ADDED_INSTANCE = " ADDED TypeInstance: ";
    private static final String REMOVED_INSTANCE = " REMOVED TypeInstance: ";
    private static final String CHANGED_INSTANCE = " CHANGED TypeInstance: ";
    private static final String ADDED_REFERENCE = " ADDED TypeInstanceReference ";
    private static final String REMOVED_REFERENCE = " REMOVED TypeInstanceReference ";
    private static final String CHANGED_REFERENCE = " CHANGED TypeInstanceReference ";

    private void appendInstanceEventString(final String event, final TypeInstance instance) {
      events_.append(event).append(instance.getId()).append(instance.getName())
      .append(instance.getBaseType().getName())
      .append(instance.getAddress().getVirtualAddress())
      .append(instance.getSection().getName());
    }

    private void appendReferenceEventString(
        final String event, final TypeInstanceReference reference) {
      events_.append(event).append(reference.getAddress().toHexString())
      .append(reference.getPosition())
      .append(reference.getTreeNode().isPresent() ? reference.getTreeNode().get().getId() : "")
      .append(reference.getTypeInstance().getId()).append(reference.getView().getName());
    }

    @Override
    public void addedTypeInstance(final TypeInstance instance) {
      appendInstanceEventString(ADDED_INSTANCE, instance);
    }

    @Override
    public void addedTypeInstanceReference(final TypeInstanceReference reference) {
      appendReferenceEventString(ADDED_REFERENCE, reference);
    }

    @Override
    public void changedTypeInstance(final TypeInstance instance) {
      appendInstanceEventString(CHANGED_INSTANCE, instance);
    }

    @Override
    public void changedTypeInstanceReference(final TypeInstanceReference reference) {
      appendReferenceEventString(CHANGED_REFERENCE, reference);
    }

    @Override
    public boolean equals(final Object other) {
      return other instanceof TypeInstanceContainerEvents
          && ((TypeInstanceContainerEvents) other).events_.toString().equals(events_.toString());
    }
    @Override
    public void removedTypeInstance(final TypeInstance instance) {
      appendInstanceEventString(REMOVED_INSTANCE, instance);
    }

    @Override
    public void removedTypeInstanceReference(final TypeInstanceReference reference) {
      appendReferenceEventString(REMOVED_REFERENCE, reference);
    }
  }
}
