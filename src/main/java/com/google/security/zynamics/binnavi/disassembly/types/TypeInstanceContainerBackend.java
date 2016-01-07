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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTree;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ViewManager;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Why this class? 1) Dependency injection -> better testable in isolation. 2) Loading of type
 * instances handled by this backend: less code in CModule (which is already too big) as well as
 * decoupling of other classes from TypeInstanceContainer itself (e.g. CModule, SQLProvider,
 * TypeManager, SectionContainer, etc).
 */
public class TypeInstanceContainerBackend {

  /**
   * The {@link SQLProvider} to access the database with.
   */
  private final SQLProvider provider;

  /**
   * The {@link INaviModule} to which the {@link TypeInstanceContainerBackend} is associated.
   */
  private final INaviModule module;

  /**
   * A Map which stores the {@link TypeInstance} by its corresponding id.
   */
  private final Map<Integer, TypeInstance> instancesById = Maps.newHashMap();

  /**
   * The {@link TypeManager} to associated the corresponding types to the {@link TypeInstance}.
   */
  private final TypeManager typeManager;

  /**
   * The {@link SectionContainer} to associate the corresponding section to the {@link TypeInstance}
   * .
   */
  private final SectionContainer sectionContainer;

  private final Map<InstanceReferenceLookup, TypeInstanceReference> referenceLookup =
      Maps.newHashMap();

  /**
   * Creates a new {@link TypeInstanceContainerBackend}.
   *
   * @param provider The {@link SQLProvider} to access the database with.
   * @param module The {@link INaviModule} to which the {@link TypeInstanceContainerBackend} is
   *        associated to.
   * @param typeManager The {@link TypeManager} which holds the types the {@link TypeInstance} refer
   *        to.
   * @param sectionContainer The {@link SectionContainer} which holds the actual binary data where
   *        the {@link TypeInstance} live.
   */
  public TypeInstanceContainerBackend(final SQLProvider provider, final INaviModule module,
      final TypeManager typeManager, final SectionContainer sectionContainer) {
    this.provider =
        Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    this.sectionContainer = Preconditions.checkNotNull(sectionContainer,
        "Error: sectionContainer argument can not be null");
  }

  /**
   * Appends the {@link IComment comment} to the existing comments of the given {@link TypeInstance
   * type instance} and returns the list of all comments for that instance.
   *
   * @param instance The type instance to which a the comment should be appended.
   * @param commentText The text of the comment to be appended.
   * @return The list of all comments associated with the given type instance.
   * @throws CouldntSaveDataException Thrown if the comment could not be saved in the database.
   * @throws CouldntLoadDataException Thrown if the list of comments could not be re-read from the
   *         database.
   */
  public List<IComment> appendComment(final TypeInstance instance, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(instance, "Error: instance can not be null.");
    Preconditions.checkNotNull(commentText, "Error: comment can not be null.");
    return CommentManager.get(provider).appendTypeInstanceComment(instance, commentText);
  }

  /**
   * Creates a new type instance and stores it in the database.
   *
   * @param name The {@link String} to name the {@link TypeInstance}.
   * @param commentString The potential comment {@link String} for the {@link TypeInstance}.
   * @param baseType The {@link BaseType} on which the {@link TypeInstance} is based.
   * @param section The {@link Section} where the {@link TypeInstance} lives.
   * @param sectionOffset The offset in the {@link Section} where the {@link TypeInstance} can be
   *        found.
   *
   * @return The newly created {@link TypeInstance}.
   *
   * @throws CouldntSaveDataException if the {@link TypeInstance} could not be saved in the
   *         database.
   * @throws CouldntLoadDataException if the necessary information for commenting the
   *         {@link TypeInstance} could not be loaded from the database.
   */
  public TypeInstance createTypeInstance(final String name, final String commentString,
      final BaseType baseType, final Section section, final long sectionOffset)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkNotNull(baseType, "Error: baseType argument can not be null");
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    Preconditions.checkArgument(sectionOffset >= 0,
        "Error: section offset must be greater or equal to zero");

    final CommentManager commentManager = CommentManager.get(provider);
    final int typeId = baseType.getId();
    final int sectionId = section.getId();
    final int instanceId = provider.createTypeInstance(module.getConfiguration().getId(),
        name,
        null,
        typeId,
        sectionId,
        sectionOffset);
    final TypeInstance instance =
        new TypeInstance(instanceId, name, baseType, section, sectionOffset, module);
    instancesById.put(instanceId, instance);

    if (commentString != null) {
      commentManager.appendTypeInstanceComment(instance, commentString);
    }
    return instance;
  }

  /**
   * Creates a new type instance reference and stores it in the database.
   *
   * @param address The {@link IAddress} of the {@link INaviInstruction} where the
   *        {@link TypeInstanceReference} will be generated.
   * @param position The position of the operand in the {@link INaviInstruction} where the
   *        {@link TypeInstanceReference} will be generated.
   * @param node The {@link INaviOperandTreeNode} in the {@link INaviOperandTree} where the
   *        {@link TypeInstanceReference} will be generated.
   * @param instance The {@link TypeInstance} for the {@link TypeInstanceReference}.
   * @param view TODO
   * @return A {@link TypeInstanceReference} based upon the given arguments.
   *
   * @throws CouldntSaveDataException if the {@link TypeInstanceReference} could not be saved to the
   *         database.
   */
  public TypeInstanceReference createTypeInstanceReference(final IAddress address,
      final int position, final INaviOperandTreeNode node, final TypeInstance instance,
      final INaviView view) throws CouldntSaveDataException {
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    Preconditions.checkArgument(position >= 0,
        "Error: position argument must be greater or equal to zero");
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    Preconditions.checkNotNull(view, "Error: view argument can not be null");

    provider.createTypeInstanceReference(module.getConfiguration().getId(), address.toLong(),
        position, node.getId(), instance.getId());
    final TypeInstanceReference reference =
        new TypeInstanceReference(address, position, Optional.of(node), instance, view);
    referenceLookup.put(new InstanceReferenceLookup(address, position, node.getId()), reference);
    return reference;
  }

  /**
   * Deletes the given comment that belongs to the {@link TypeInstance type instance}.
   *
   * @param instance The {@link TypeInstance type instance} for which to delete the given
   *        {@link IComment comment}
   * @param comment The {@link IComment comment} to delete.
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  public void deleteComment(final TypeInstance instance, final IComment comment)
      throws CouldntDeleteException {
    CommentManager.get(provider).deleteTypeInstanceComment(instance, comment);
  }

  /**
   * Deletes a {@link TypeInstance} from the database and from the internal map.
   *
   * @param instance The {@link TypeInstance} to be deleted.
   *
   * @throws CouldntDeleteException if the {@link TypeInstance} could not be deleted from the
   *         database.
   */
  public void deleteInstance(final TypeInstance instance) throws CouldntDeleteException {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    Preconditions.checkArgument(instancesById.containsKey(instance.getId()),
        "Error: type instance id is not known to the backend");
    instancesById.remove(instance.getId());
    provider.deleteTypeInstance(instance.getModule().getConfiguration().getId(), instance.getId());
  }

  /**
   * Deletes a {@link TypeInstance instance} from the internal storage but not the database.
   *
   * @param backendInstance The {@link TypeInstance} to remove from the internal storage.
   */
  public void deleteInstanceInternal(final TypeInstance backendInstance) {
    Preconditions.checkNotNull(backendInstance, "Error: backendInstance argument can not be null");
    instancesById.remove(backendInstance);
  }

  /**
   * Deletes a {@link TypeInstanceReference} from the database.
   *
   * @param reference The {@link TypeInstanceReference} to be deleted.
   * @throws CouldntDeleteException if the {@link TypeInstanceReference} could not be deleted from
   *         the database.
   */
  public void deleteInstanceReference(final TypeInstanceReference reference)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(reference, "Error: reference argument can not be null");
    if (reference.getTreeNode().isPresent()) {
      provider.deleteTypeInstanceReference(
          reference.getTypeInstance().getModule().getConfiguration().getId(),
          reference.getAddress().toBigInteger(), reference.getPosition(),
          reference.getTreeNode().get().getId());
      referenceLookup.remove(new InstanceReferenceLookup(reference.getAddress(),
          reference.getPosition(), reference.getTreeNode().get().getId()));
    } else {
      throw new IllegalStateException("Error: reference must be associated to a node.");
    }
  }

  /**
   * Deletes a {@link TypeInstanceReference reference} from the internal storage but not the
   * database.
   *
   * @param reference The {@link TypeInstanceReference} to remove from the internal storage.
   */
  public void deleteInstanceReferenceInternal(final TypeInstanceReference reference) {
    Preconditions.checkNotNull(reference, "Error: reference argument can not be null");
    referenceLookup.remove(reference);
  }

  /**
   * Replaces the comment text of the given {@link IComment comment} of the {@link TypeInstance type
   * instance} and saves the changed comment in the database.
   *
   * @param instance The type instance for which to edit the comment.
   * @param comment The comment to edit.
   * @param newCommentText The new comment string.
   * @return The edited comment instance.
   * @throws CouldntSaveDataException Thrown if the comment could not be saved in the database.
   */
  public IComment editComment(final TypeInstance instance, final IComment comment,
      final String newCommentText) throws CouldntSaveDataException {
    return CommentManager.get(provider).editTypeInstanceComment(instance, comment, newCommentText);
  }

  /**
   * Returns the list of {@link IComment comments} associated with this {@link TypeInstance type
   * instance}.
   *
   *  Note: the semantics of this method differs from the one provided by the comment manager in
   * that if returns an empty list instead of null for non-existing comments.
   *
   * @return The list of comments for this type instance.
   */
  public List<IComment> getComments(final TypeInstance instance) {
    final List<IComment> comments = CommentManager.get(provider).getTypeInstanceComments(instance);
    return comments == null ? new ArrayList<IComment>() : comments;
  }

  public void initializeTypeInstanceComment(final TypeInstance instance,
      final List<IComment> comments) {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    Preconditions.checkNotNull(comments, "Error: comments argument can not be null");
    CommentManager.get(provider).initializeTypeInstanceComment(instance, comments);
  }

  /**
   * Returns whether the currently active {@link IUser user} is the owner of the given
   * {@link IComment comment}.
   *
   * @param comment The comment to check ownership for.
   * @return Returns whether the currently active user is the owner of the given comment.
   */
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(provider).getCurrentActiveUser().equals(comment.getUser());
  }

  /**
   * Loads a single type instance from the database.
   *
   * @param typeInstanceId The {@link Integer} id of the {@link TypeInstance type instance} to load.
   * @return A {@link TypeInstance instance} if present in the database.
   *
   * @throws CouldntLoadDataException Thrown if the {@link TypeInstance type instance} could not be
   *         loaded from the database.
   */
  public TypeInstance loadTypeInstance(final Integer typeInstanceId)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");
    final RawTypeInstance rawTypeInstance = provider.loadTypeInstance(module, typeInstanceId);

    final BaseType baseType = typeManager.getBaseType(rawTypeInstance.getTypeId());
    final Section section = sectionContainer.getSection(rawTypeInstance.getSectionId());
    final TypeInstance typeInstance = new TypeInstance(rawTypeInstance.getId(),
        rawTypeInstance.getName(),
        baseType,
        section,
        rawTypeInstance.getSectionOffset(),
        module);
    instancesById.put(typeInstance.getId(), typeInstance);

    if (rawTypeInstance.getCommentId() != null) {
      final CommentManager manager = CommentManager.get(provider);
      final List<IComment> comments = provider.loadCommentById(rawTypeInstance.getCommentId());
      manager.initializeTypeInstanceComment(typeInstance, comments);
    }
    return typeInstance;
  }

  /**
   * Load a single type instance reference from the database.
   *
   * @param typeInstanceId the id of the {@link TypeInstanceReference reference}.
   * @param address The address of the {@link INaviInstruction instruction} where the
   *        {@link TypeInstanceReference reference} is associated.
   * @param position The {@link OperandTree operand tree} position in the {@link INaviInstruction
   *        instruction} the {@link TypeInstanceReference reference} is associated to.
   * @param expressionId The {@link OperandTreeNode operand tree node} id within the
   *        {@link OperandTree operand tree}.
   *
   * @return The {@link TypeInstanceReference} loaded from the database.
   * @throws CouldntLoadDataException
   */
  public TypeInstanceReference loadTypeInstanceReference(final Integer typeInstanceId,
      final BigInteger address, final Integer position, final Integer expressionId)
      throws CouldntLoadDataException {

    Preconditions.checkNotNull(typeInstanceId, "Error: typeInstanceId argument can not be null");
    Preconditions.checkNotNull(address, "Error: address argument can not be null");
    Preconditions.checkNotNull(position, "Error: position argument can not be null");
    Preconditions.checkNotNull(expressionId, "Error: expressionId argument can not be null");

    final RawTypeInstanceReference rawReference =
        provider.loadTypeInstanceReference(module, typeInstanceId, address, position, expressionId);

    final TypeInstance typeInstance = instancesById.get(rawReference.getTypeInstanceId());
    final INaviView view = module.getContent().getViewContainer().getView(rawReference.getViewId());

    final TypeInstanceReference reference = new TypeInstanceReference(new CAddress(address),
        position, Optional.<INaviOperandTreeNode>absent(), typeInstance, view);
    referenceLookup.put(new InstanceReferenceLookup(new CAddress(address), position, expressionId),
        reference);
    return reference;
  }

  /**
   * Loads all type instance references for the module this {@link TypeInstanceContainerBackend} was
   * initialized with.
   *
   * @return A {@link List} of {@link TypeInstanceReference references} loaded from the database.
   *
   * @throws CouldntLoadDataException if the data could not be loaded from the database.
   */
  public List<TypeInstanceReference> loadTypeInstanceReferences() throws CouldntLoadDataException {
    final List<RawTypeInstanceReference> rawReferences =
        provider.loadTypeInstanceReferences(module);
    final List<TypeInstanceReference> references = Lists.newArrayList();
    for (final RawTypeInstanceReference rawReference : rawReferences) {
      final TypeInstance typeInstance = instancesById.get(rawReference.getTypeInstanceId());
      final INaviView view = ViewManager.get(provider).getView(rawReference.getViewId());
      if (view != null) {
        final Optional<INaviOperandTreeNode> node = Optional.absent();
        final IAddress address = rawReference.getAddress();
        final int position = rawReference.getOperandPosition();
        final int expressionId = rawReference.getExpressionId();
        final TypeInstanceReference reference =
            new TypeInstanceReference(address, position, node, typeInstance, view);
        references.add(reference);
        referenceLookup.put(new InstanceReferenceLookup(address, position, expressionId),
            reference);
      }
    }
    return references;
  }

  /**
   * Load all type instances from the database.
   *
   * @return A {@link Set} of {@link TypeInstance} associated to the {@link INaviModule} the
   *         {@link TypeInstanceContainerBackend} is associated to.
   *
   * @throws CouldntLoadDataException if the {@link Set} of {@link TypeInstance} could not be loaded
   *         from the database.
   */
  public Set<TypeInstance> loadTypeInstances() throws CouldntLoadDataException {
    final List<RawTypeInstance> rawInstances = provider.loadTypeInstances(module);
    final HashMap<TypeInstance, Integer> instanceToComment = Maps.newHashMap();

    for (final RawTypeInstance rawInstance : rawInstances) {
      final BaseType baseType = typeManager.getBaseType(rawInstance.getTypeId());
      final Section section = sectionContainer.getSection(rawInstance.getSectionId());
      final TypeInstance typeInstance = new TypeInstance(rawInstance.getId(),
          rawInstance.getName(),
          baseType,
          section,
          rawInstance.getSectionOffset(),
          module);
      instanceToComment.put(typeInstance, rawInstance.getCommentId());
      instancesById.put(typeInstance.getId(), typeInstance);
    }

    final Map<TypeInstance, Integer> typeInstanceWithComment =
        Maps.filterValues(instanceToComment, new Predicate<Integer>() {
          @Override
          public boolean apply(final Integer commentId) {
            return commentId != null;
          }
        });

    final CommentManager manager = CommentManager.get(provider);
    final HashMap<Integer, ArrayList<IComment>> typeInstanceTocomments =
        provider.loadMultipleCommentsById(typeInstanceWithComment.values());
    for (final Entry<TypeInstance, Integer> entry : typeInstanceWithComment.entrySet()) {
      manager.initializeTypeInstanceComment(entry.getKey(),
          typeInstanceTocomments.get(entry.getValue()));
    }

    return instanceToComment.keySet();
  }

  public TypeInstanceReference lookupReference(final IAddress address, final int position,
      final int expressionId) {
    return referenceLookup.get(new InstanceReferenceLookup(address, position, expressionId));
  }

  /**
   * Queries the internal storage for the given type instance id.
   *
   * @param typeInstanceId the id of the type instance to look up.
   * @return The {@link TypeInstance instance} if present in the internal storage.
   */
  public TypeInstance lookupTypeInstance(final Integer typeInstanceId) {
    return instancesById.get(typeInstanceId);
  }

  /**
   * This function changes the name of a {@link TypeInstance instance} in the database.
   *
   * @param instance The {@link TypeInstance} where the name is changed..
   * @param name The new name of the {@link TypeInstance}
   *
   * @throws CouldntSaveDataException if the changes to the {@link TypeInstance} name could not be
   *         saved to the database.
   */
  public void setInstanceName(final TypeInstance instance, final String name)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    Preconditions.checkNotNull(name, "Error: name argument can not be null");
    Preconditions.checkArgument(!name.isEmpty(), "Error: name argument can not be empty");

    provider.setTypeInstanceName(instance.getModule().getConfiguration().getId(), instance.getId(),
        name);
  }

  /**
   * Used to lookup references by the triple (address, operand position, expression id).
   */
  private class InstanceReferenceLookup {

    private final IAddress address;
    private final int operandPosition;
    private final int expressionId;

    public InstanceReferenceLookup(final IAddress address, final int operandPosition,
        final int expressionId) {
      this.address = address;
      this.operandPosition = operandPosition;
      this.expressionId = expressionId;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof InstanceReferenceLookup)) {
        return false;
      }
      final InstanceReferenceLookup other = (InstanceReferenceLookup) o;
      return address.equals(other.address) && operandPosition == other.operandPosition
          && expressionId == other.expressionId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(address, operandPosition, expressionId);
    }
  }
}
