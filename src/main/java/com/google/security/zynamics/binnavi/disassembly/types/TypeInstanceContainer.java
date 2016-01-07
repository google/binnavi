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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTree;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains and owns all instances of TypeInstance and TypeInstanceReference objects. Provides
 * methods to notify client code about changes regarding those objects.
 * 
 * This whole class is thread safe since changes to the container can be triggered from multiple
 * threads (i.e. GUI vs database notifications).
 */
public final class TypeInstanceContainer {

  /**
   * Stores all {@link TypeInstance type instances} in this {@link TypeInstanceContainer container}.
   */
  private final List<TypeInstance> instances = Lists.newArrayList();

  /**
   * Holds the mapping of each {@link TypeInstance} to all of its {@link TypeInstanceReference}.
   */
  private final Multimap<TypeInstance, TypeInstanceReference> referencesByInstance =
      ArrayListMultimap.create();

  /**
   * Stores a mapping of the internal {@link TypeInstanceAddress} to its {@link TypeInstance}.
   */
  private final TreeMap<TypeInstanceAddress, TypeInstance> instancesByAddress =
      new TreeMap<TypeInstanceAddress, TypeInstance>();

  /**
   * The {@link TypeInstanceContainerBackend} used to store and retrieve {@link TypeInstance}
   * information from the database.
   */
  private final TypeInstanceContainerBackend backend;

  /**
   * The listener which is informed of changes in the {@link TypeInstanceContainer}.
   */
  private final ListenerProvider<TypeInstanceContainerListener> listeners =
      new ListenerProvider<TypeInstanceContainerListener>();

  /**
   * Creates a new {@link TypeInstanceContainer}. Only the {@link TypeInstance} are loaded in this
   * constructor. The {@link TypeInstanceReference} are loaded when the corresponding
   * {@link INaviView} is opened.
   * 
   * @param backend The {@link TypeInstanceContainerBackend} to store and retrieve
   *        {@link TypeInstance} information from the database.
   * 
   * @throws CouldntLoadDataException if the {@link TypeInstanceContainerBackend} could not load the
   *         {@link TypeInstance} information from the database.
   */
  public TypeInstanceContainer(final TypeInstanceContainerBackend backend,
      final SQLProvider provider) throws CouldntLoadDataException {
    this.backend = Preconditions.checkNotNull(backend, "Error: backend argument can not be null");
    CommentManager.get(provider).addListener(new InternalCommentListener());
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener} about a added {@link TypeInstance}.
   * 
   * @param instance The {@link TypeInstance} which has been added.
   */
  private void notifyInstanceAdded(final TypeInstance instance) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.addedTypeInstance(instance);
    }
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener} about a changed {@link TypeInstance}.
   * 
   * @param instance The old {@link TypeInstance}.
   */
  private void notifyInstanceChanged(final TypeInstance instance) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.changedTypeInstance(instance);
    }
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener} about a removed {@link TypeInstance}.
   * 
   * @param instance The {@link TypeInstance} which has been removed.
   */
  private void notifyInstanceRemoved(final TypeInstance instance) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.removedTypeInstance(instance);
    }
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener} about a added
   * {@link TypeInstanceReference}.
   * 
   * @param reference The {@link TypeInstanceReference} which has been added.
   */
  private void notifyReferenceAdded(final TypeInstanceReference reference) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.addedTypeInstanceReference(reference);
    }
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener listeners} about a changed
   * {@link TypeInstanceReference reference}.
   * 
   * @param reference The {@link TypeInstanceReference reference} which has changed.
   */
  private void notifyReferenceChanged(final TypeInstanceReference reference) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.changedTypeInstanceReference(reference);
    }
  }

  /**
   * Notifies all known {@link TypeInstanceContainerListener} about a removed
   * {@link TypeInstanceReference}.
   * 
   * @param reference The {@link TypeInstanceReference} which has been removed.
   */
  private void notifyReferenceRemoved(final TypeInstanceReference reference) {
    for (final TypeInstanceContainerListener listener : listeners) {
      listener.removedTypeInstanceReference(reference);
    }
  }

  /**
   * Adds a {@link TypeInstanceContainerListener} to this {@link TypeInstanceContainer}.
   * 
   * @param listener The {@link TypeInstanceContainerListener} to be added.
   */
  public synchronized void addListener(final TypeInstanceContainerListener listener) {
    Preconditions.checkNotNull(listener, "Error: listener argument can not be null");
    listeners.addListener(Preconditions.checkNotNull(listener,
        "Error: listener argument can not be null"));
  }

  /**
   * Appends a comment to the given type instance and saves it in the database.
   * 
   * @param instance The {@link TypeInstance type instance} to which a comment is appended.
   * @param commentText The text of the comment to be appended.
   * @return Returns the list of all comments for the given {@link TypeInstance type instance}.
   * @throws CouldntLoadDataException Thrown if the list of comments could not be re-read from the
   *         database.
   * @throws CouldntSaveDataException Thrown if the new comment could not be saved in the database.
   */
  public synchronized List<IComment> appendComment(final TypeInstance instance,
      final String commentText) throws CouldntSaveDataException, CouldntLoadDataException {
    return backend.appendComment(instance, commentText);
  }

  /**
   * Creates a new type instance instance and stores it in the database.
   * 
   * @param name The name of the type instance.
   * @param comment The comment string for this type instance.
   * @param baseType The {@link BaseType} of this {@link TypeInstance}.
   * @param section The {@link Section} this {@link TypeInstance} is contained in.
   * @param sectionOffset The offset relative to the beginning of the containing {@link Section}.
   * 
   * @return The newly created {@link TypeInstance}.
   * 
   * @throws CouldntSaveDataException Thrown if the {@link TypeInstance} could not be saved in the
   *         database.
   * @throws CouldntLoadDataException Thrown if the comment could not be reloaded from the database.
   */
  public synchronized TypeInstance createInstance(final String name, final String comment,
      final BaseType baseType, final Section section, final long sectionOffset)
      throws CouldntSaveDataException, CouldntLoadDataException {

    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkArgument(comment == null || !comment.isEmpty(),
        "Error: comment can either be null or a non empty string");
    Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkArgument(sectionOffset >= 0,
        "Error: section offset must be greater or equal to zero");
    Preconditions.checkArgument(!instancesByAddress.containsKey(new TypeInstanceAddress(section
        .getStartAddress(), sectionOffset)));

    final TypeInstance instance =
        backend.createTypeInstance(name, comment, baseType, section, sectionOffset);
    instancesByAddress.put(instance.getAddress(), instance);
    instances.add(instance);
    notifyInstanceAdded(instance);
    return instance;
  }

  /**
   * Instantiates a new type instance reference and stores it in the database.
   * 
   * @param address The {@link IAddress} of the {@link INaviInstruction} where the
   *        {@link TypeInstanceReference} is associated to.
   * @param operandPosition The position of the {@link INaviOperandTree} within the
   *        {@link INaviInstruction} where the {@link TypeInstanceReference} is associated to.
   * @param node The {@link INaviOperandTreeNode} to which the {@link TypeInstanceReference} is
   *        associated to.
   * @param instance The {@link TypeInstance} which the {@link TypeInstanceReference} references.
   * @param view TODO
   * @return The {@link TypeInstanceReference} created.
   * 
   * @throws CouldntSaveDataException if the {@link TypeInstanceReference} could not be saved in the
   *         database.
   */
  public synchronized TypeInstanceReference createReference(final IAddress address,
      final int operandPosition, final INaviOperandTreeNode node, final TypeInstance instance,
      final INaviView view) throws CouldntSaveDataException {

    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    Preconditions.checkArgument(operandPosition >= 0,
        "Error: operand position must be equal or greater then zero");
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");

    final TypeInstanceReference reference =
        backend.createTypeInstanceReference(address, operandPosition, node, instance, view);
    referencesByInstance.put(instance, reference);
    notifyReferenceAdded(reference);
    return reference;
  }

  /**
   * Deactivates a {@link TypeInstanceReference reference} when the node it belongs to is unloaded.
   * 
   * @param reference The {@link TypeInstanceReference} to deactivate.
   */
  public synchronized void deactivateTypeInstanceReference(final TypeInstanceReference reference) {
    Preconditions.checkNotNull(reference, "Error: reference argument can not be null");
    reference.setTreeNode(null);
    notifyReferenceChanged(reference);
  }

  /**
   * Deletes the given comment that belongs to the {@link TypeInstance type instance}.
   * 
   * @param instance The {@link TypeInstance type instance} for which to delete the given
   *        {@link IComment comment}
   * @param comment The {@link IComment comment} to delete.
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  public synchronized void deleteComment(final TypeInstance instance, final IComment comment)
      throws CouldntDeleteException {
    backend.deleteComment(instance, comment);
  }

  /**
   * Delete a {@link TypeInstance} from the internal storage.
   * 
   * @param typeInstanceId The {@link Integer id} of the {@link TypeInstance instance} to delete.
   */
  public synchronized void deleteInstance(final Integer typeInstanceId) {
    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");
    final TypeInstance backendInstance = backend.lookupTypeInstance(typeInstanceId);
    final TypeInstance storedTypeInstance = instancesByAddress.remove(backendInstance.getAddress());
    instances.remove(storedTypeInstance);
    backend.deleteInstanceInternal(backendInstance);
    notifyInstanceRemoved(storedTypeInstance);
  }

  /**
   * Delete a {@link TypeInstance} from the database and the internal storage.
   * 
   * @param instance The {@link TypeInstance} to be deleted.
   * @throws CouldntDeleteException if the {@link TypeInstance} could not be delete from the
   *         database.
   */
  public synchronized void deleteInstance(final TypeInstance instance)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    instancesByAddress.remove(instance.getAddress());
    instances.remove(instance);
    backend.deleteInstance(instance);
    notifyInstanceRemoved(instance);
  }

  /**
   * Delete a {@link TypeInstanceReference} from the internal storage.
   * 
   * @param typeInstanceId The id of the {@link TypeInstanceReference reference}
   * @param address The address of the {@link INaviInstruction instruction} this
   *        {@link TypeInstanceReference reference} belongs to.
   * @param position The position of the {@link OperandTree operand tree} within the
   *        {@link INaviInstruction instruction}.
   * @param expressionId The id of the {@link OperandTreeNode node} with in the {@link OperandTree
   *        operand tree}.
   */
  public synchronized void deleteReference(final Integer typeInstanceId, final BigInteger address,
      final Integer position, final Integer expressionId) {
    final TypeInstanceReference reference =
        backend.lookupReference(new CAddress(address), position, expressionId);
    referencesByInstance.remove(reference.getTypeInstance(), reference);
    backend.deleteInstanceReferenceInternal(reference);
    reference.setTreeNode(null);
    notifyReferenceRemoved(reference);
  }

  /**
   * Delete a {@link TypeInstanceReference} from the database and the internal storage.
   * 
   * @param reference The {@link TypeInstanceReference} to be deleted.
   * @throws CouldntDeleteException if the {@link TypeInstanceReference} could not be deleted from
   *         the database.
   */
  public synchronized void deleteReference(final TypeInstanceReference reference)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(reference, "Error: reference argument can not be null");
    referencesByInstance.remove(reference.getTypeInstance(), reference);
    backend.deleteInstanceReference(reference);
    notifyReferenceRemoved(reference);
  }

  /**
   * Replaces the comment text of the given comment of the type instance and saves the changed
   * comment in the database.
   * 
   * @param instance The type instance for which to edit the comment.
   * @param comment The comment to edit.
   * @param newCommentText The new comment string.
   * @return The edited comment instance.
   * @throws CouldntSaveDataException Thrown if the comment could not be saved in the database.
   */
  public synchronized IComment editComment(final TypeInstance instance, final IComment comment,
      final String newCommentText) throws CouldntSaveDataException {
    return backend.editComment(instance, comment, newCommentText);
  }

  /**
   * Returns all comments for the given type instance.
   * 
   * @param instance The type instance to retrieve comments for.
   * @return Returns all comments for the given type instance.
   */
  public synchronized List<IComment> getComments(final TypeInstance instance) {
    return backend.getComments(instance);
  }

  /**
   * Returns the number of {@link TypeInstanceReference references} to the given {@TypeInstance
   *  instance}.
   * 
   * @param typeInstance The type instance for which to determine the number of references.
   * @return The number of references to the given type instance.
   */
  public synchronized int getReferenceCount(final TypeInstance typeInstance) {
    Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null.");
    return referencesByInstance.get(typeInstance).size();
  }

  /**
   * Returns all {@link TypeInstanceReference references} that are associated to the given
   * {@link TypeInstance} .
   * 
   * @param typeInstance The {@link TypeInstance} to get the {@link List} of
   *        {@link TypeInstanceReference} for.
   * 
   * @return A {@link List} of {@link TypeInstanceReference} for the given {@link TypeInstance}.
   */
  public synchronized List<TypeInstanceReference> getReferences(final TypeInstance typeInstance) {
    Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null.");
    return Collections.unmodifiableList((List<TypeInstanceReference>) referencesByInstance
        .get(typeInstance));
  }

  /**
   * Returns the {@link TypeInstance} which is located at the given offset in the given
   * {@link Section}.
   * 
   * @param address The {@link Section} in which the {@link TypeInstance} is located.
   * @return The {@link TypeInstance} if found.
   */
  public synchronized TypeInstance getTypeInstance(final TypeInstanceAddress address) {
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    return instancesByAddress.get(address);
  }

  public synchronized TypeInstance getTypeInstanceById(final Integer typeInstanceId) {
    return backend.lookupTypeInstance(typeInstanceId);
  }

  /**
   * Returns all type instances for the current module.
   */
  public synchronized List<TypeInstance> getTypeInstances() {
    return Collections.unmodifiableList(instances);
  }

  /**
   * Returns all instances contained in the given section.
   * 
   * @param section The section for which all type instances should be determined.
   * @return The collection of type instances contained in the given section.
   */
  public synchronized Collection<TypeInstance> getTypeInstances(final Section section) {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    return instancesByAddress
        .tailMap(new TypeInstanceAddress(section.getStartAddress(), 0), true)
        .headMap(new TypeInstanceAddress(section.getStartAddress(), section.getVirtualSize()),
            false).values();
  }

  /**
   * Initializes the internal storage with the information from the database.
   * 
   * @throws CouldntLoadDataException if the data could not be loaded from the database.
   */
  public synchronized void initialize() throws CouldntLoadDataException {
    for (final TypeInstance instance : backend.loadTypeInstances()) {
      instancesByAddress.put(instance.getAddress(), instance);
      instances.add(instance);
    }

    for (final TypeInstanceReference reference : backend.loadTypeInstanceReferences()) {
      referencesByInstance.put(reference.getTypeInstance(), reference);
    }
  }

  /**
   * Activates a {@link TypeInstanceReference reference} with its appropriate
   * {@link INaviOperandTreeNode node} when it is loaded.
   * 
   * @param node The {@link INaviOperandTreeNode} to which this {@link TypeInstanceReference} will
   *        now be associated.
   */
  public synchronized void initializeTypeInstanceReference(final IAddress address,
      final int operandPosition, final int expressionId, final INaviOperandTreeNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    final TypeInstanceReference reference =
        backend.lookupReference(address, operandPosition, expressionId);
    if (reference != null) {
      reference.setTreeNode(node);
      node.addInstanceReference(reference);
      notifyReferenceChanged(reference);
    }
  }

  /**
   * Returns whether the currently active user is the owner of the given comment.
   * 
   * @param comment The comment to check ownership for.
   * @return Returns whether the currently active user is the owner of the given comment.
   */
  public synchronized boolean isOwner(final IComment comment) {
    return backend.isOwner(comment);
  }

  /**
   * Loads a previously unknown {@link TypeInstance} which is referenced by the given
   * {@link Integer type instance id}.
   * 
   * @param typeInstanceId The {@link Integer id} identifying the type instance.
   * 
   * @return The newly retrieved {@link TypeInstance}.
   * @throws CouldntLoadDataException if the {@link TypeInstance} could not be loaded from the
   *         database.
   */
  public synchronized TypeInstance loadInstance(final Integer typeInstanceId)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");

    if (backend.lookupTypeInstance(typeInstanceId) != null) {
      return reloadInstance(typeInstanceId);
    } else {
      final TypeInstance instance = backend.loadTypeInstance(typeInstanceId);
      instancesByAddress.put(instance.getAddress(), instance);
      instances.add(instance);
      notifyInstanceAdded(instance);
      return instance;
    }
  }

  /**
   * Loads a single type instance reference from the database and initializes the internal storage.
   * 
   * @param typeInstanceId The id of the {@link TypeInstance} the {@link TypeInstanceReference
   *        reference} points to.
   * @param address The address of the {@link INaviInstruction instruction} the {@link TypeInstance}
   *        belongs to.
   * @param position The {@link INaviOperandTree operand tree} position within the
   *        {@link INaviInstruction instruction} which is referenced.
   * @param expressionId The {@link INaviOperandTreeNode operand tree node} id to which the
   *        {@link TypeInstanceReference} is associated.
   * 
   * @return The {@link TypeInstanceReference} loaded from the database.
   * @throws CouldntLoadDataException
   */
  public synchronized TypeInstanceReference loadInstanceReference(final Integer typeInstanceId,
      final BigInteger address, final Integer position, final Integer expressionId)
      throws CouldntLoadDataException {

    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    Preconditions.checkNotNull(position, "Error: position argument can not be null");
    Preconditions.checkNotNull(expressionId, "Error: expressionId argument can not be null");

    final TypeInstanceReference reference =
        backend.loadTypeInstanceReference(typeInstanceId, address, position, expressionId);

    referencesByInstance.put(reference.getTypeInstance(), reference);
    return reference;
  }

  /**
   * Reloads a previously known {@link TypeInstance} which is referenced by the given
   * {@link Integer type instance id}.
   * 
   * @param typeInstanceId The {@link Integer id} identifying the type instance.
   * 
   * @return the currently stored {@link TypeInstance} with the changes of the reload applied.
   * @throws CouldntLoadDataException if the changes could not be loaded from the database.
   */
  public synchronized TypeInstance reloadInstance(final Integer typeInstanceId)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");

    if (backend.lookupTypeInstance(typeInstanceId) == null) {
      return loadInstance(typeInstanceId);
    } else {
      final TypeInstance instance = backend.loadTypeInstance(typeInstanceId);
      final TypeInstance storedInstance = instancesByAddress.get(instance.getAddress());
      storedInstance.setName(instance.getName());
      notifyInstanceChanged(storedInstance);
      return storedInstance;
    }
  }

  /**
   * Removes a {@link TypeInstanceContainerListener} from the known listeners.
   * 
   * @param listener The {@link TypeInstanceContainerListener} to be removed.
   */
  public synchronized void removeListener(final TypeInstanceContainerListener listener) {
    listeners.removeListener(Preconditions.checkNotNull(listener,
        "Error: listener argument can not be null"));
  }

  /**
   * Changes the name of an existing {@link TypeInstance type instance}.
   * 
   * @param instance The {@link TypeInstance} that will be changed.
   * @param name The new name of the {@link TypeInstance}.
   * 
   * @throws CouldntSaveDataException if the changed {@link TypeInstance} could not be saved to the
   *         database.
   * @throws CouldntLoadDataException
   */
  public synchronized void setInstanceName(final TypeInstance instance, final String name)
      throws CouldntSaveDataException, CouldntLoadDataException {

    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkArgument(!name.isEmpty(), "Error: name argument can not be empty");
    Preconditions.checkArgument(instances.indexOf(instance) != -1,
        "Error: the given instance is not known.");

    backend.setInstanceName(instance, name);
    instance.setName(name);

    notifyInstanceChanged(instance);
  }


  /**
   * TODO(timkornau): describe
   */
  private class InternalCommentListener extends CommentListenerAdapter {
    @Override
    public void appendedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
      notifyInstanceChanged(instance);
    }

    @Override
    public void initializedTypeInstanceComment(final TypeInstance instance,
        final List<IComment> comments) {
      notifyInstanceChanged(instance);
    }

    @Override
    public void editedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
      notifyInstanceChanged(instance);
    }

    @Override
    public void deletedTypeInstanceComment(final TypeInstance instance, final IComment comment) {
      notifyInstanceChanged(instance);
    }
  }
}
