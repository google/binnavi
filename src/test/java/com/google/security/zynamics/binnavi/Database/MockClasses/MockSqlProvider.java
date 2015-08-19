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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.DatabaseVersion;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUser;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.MockCreator;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleInitializeReporter;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.RawBaseType;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.SectionPermission;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Mock class for the {@link SQLProvider SQL provider}. Used when tests need a fake connection to an
 * SQL server.
 */
public final class MockSqlProvider implements SQLProvider {
  private int tagIdCounter = 1;
  private int viewCounter = 1;
  private int traceListCounter = 1;
  private int typeInstanceCounter = 1;
  private int typeId;
  private final HashMap<Commentable, ArrayList<IComment>> comments =
      new HashMap<Commentable, ArrayList<IComment>>();

  private final List<IFlowgraphView> flowGraphViews = new ArrayList<IFlowgraphView>();
  private final List<INaviFunction> functions = new ArrayList<INaviFunction>();
  private final HashMap<IAddress, ReferenceType> references =
      new HashMap<IAddress, ReferenceType>();
  private final List<INaviModule> modules = Lists.<INaviModule>newArrayList();
  private final ArrayList<IUser> users = new ArrayList<IUser>();
  private final ArrayListMultimap<INaviModule, RawTypeMember> members = ArrayListMultimap.create();
  private final ArrayListMultimap<Integer, RawBaseType> types = ArrayListMultimap.create();
  private final ArrayListMultimap<Integer, RawTypeInstance> typeInstances =
      ArrayListMultimap.create();
  private final ArrayListMultimap<INaviModule, RawTypeSubstitution> substitutions =
      ArrayListMultimap.create();
  private final ArrayListMultimap<Integer, Section> sections = ArrayListMultimap.create();
  private int sectionId;

  private int appendComment(final Commentable commentable, final Integer userId,
      final String commentText) {

    Preconditions.checkNotNull(commentable, "Error: commentable argument can not be null");
    Preconditions.checkNotNull(userId, "Error: userId argument can not be null");
    Preconditions.checkNotNull(commentText, "Error: commentText argument can not be null");

    final IUser currentUser = getCurrentUser(userId);
    final int commentId = getRandom();

    final ArrayList<IComment> currentComments =
        comments.containsKey(commentable) ? comments.get(commentable) : new ArrayList<IComment>();
    currentComments.add(new CComment(commentId, currentUser, null, commentText));
    comments.put(commentable, currentComments);

    return commentId;
  }

  private final void deleteComment(final Commentable commentable, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(commentable, "Error: commentable argument can not be null");
    Preconditions.checkNotNull(commentId, "Error: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "Error: userId argument can not be null");

    final ArrayList<IComment> currentComments =
        comments.containsKey(commentable) ? comments.get(commentable) : new ArrayList<IComment>();

    int index = -1;
    for (final IComment comment : currentComments) {
      if (Objects.equals(comment.getId(), commentId)) {
        if (comment.getUser().getUserId() == userId) {
          index = currentComments.indexOf(comment);
        } else {
          throw new IllegalStateException(
              "Error: the supplied user id is not the owner of the comment");
        }
      }
    }
    if (index == -1) {
      throw new IllegalArgumentException(
          "Error: comment with this id not known to the storage system");
    }

    currentComments.remove(index);
    comments.put(commentable, currentComments);
  }

  private RawTypeMember findMember(final TypeMember member, final INaviModule module) {
    for (final RawTypeMember rawMember : members.get(module)) {
      if (rawMember.getId() == member.getId()) {
        return rawMember;
      }
    }
    return null;
  }

  private RawTypeSubstitution findSubstitution(final BigInteger address, final int position,
      final int expressionId, final INaviModule module) {
    for (final RawTypeSubstitution rawSubstitution : substitutions.get(module)) {
      if (rawSubstitution.getAddress().toBigInteger().equals(address)
          && rawSubstitution.getPosition() == position
          && rawSubstitution.getExpressionId() == expressionId) {
        return rawSubstitution;
      }
    }
    return null;
  }

  private RawBaseType findType(final BaseType baseType, final INaviModule module) {
    for (final RawBaseType rawType : types.get(module.getConfiguration().getId())) {
      if (rawType.getId() == baseType.getId()) {
        return rawType;
      }
    }
    return null;
  }

  private ArrayList<IComment> getCommentInternally(final Integer commentId) {
    for (final ArrayList<IComment> commentList : comments.values()) {
      if (!commentList.isEmpty() && Iterables.getLast(commentList).getId().equals(commentId)) {
        return commentList;
      }
    }
    return null;
  }

  private IUser getCurrentUser(final Integer userId) {
    IUser currentUser = null;
    for (final IUser storedUser : users) {
      if (storedUser.getUserId() == userId) {
        currentUser = storedUser;
      }
    }
    if (currentUser == null) {
      throw new IllegalStateException("Error: The user id is not known to the storage system");
    }
    return currentUser;
  }

  private int getRandom() {
    final Random generator = new Random();
    return generator.nextInt(Integer.MAX_VALUE) == 0 ? generator.nextInt(Integer.MAX_VALUE)
        : generator.nextInt(Integer.MAX_VALUE);
  }

  @Override
  public void addDebugger(final INaviProject project, final DebuggerTemplate debugger) {}

  @Override
  public void addModule(final INaviAddressSpace addressSpace, final INaviModule module) {}

  @Override
  public void addReference(final INaviOperandTreeNode node, final IAddress address,
      final ReferenceType type) {
    references.put(address, type);
  }

  @Override
  public IUser addUser(final String userName) {
    final Random generator = new Random();
    final int userId =
        generator.nextInt(Integer.MAX_VALUE) == 0 ? generator.nextInt(Integer.MAX_VALUE)
            : generator.nextInt(Integer.MAX_VALUE);

    final CUser user = new CUser(userId, userName);

    users.add(user);

    return user;
  }

  @Override
  public Integer appendFunctionComment(final INaviFunction function, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    return appendComment(new Commentable(function, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendFunctionNodeComment(final INaviFunctionNode node, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: functionNode argument can not be null");
    return appendComment(new Commentable(node, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public Integer appendGlobalCodeNodeComment(final INaviCodeNode node, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: codeNode argument can not be null");
    return appendComment(new Commentable(node, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendGlobalEdgeComment(final INaviEdge edge, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
    return appendComment(new Commentable(edge, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendGlobalInstructionComment(final INaviInstruction instruction,
      final String commentText, final Integer userId) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    return appendComment(new Commentable(instruction, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendGroupNodeComment(final INaviGroupNode node, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: groupNode argument can not be null");
    return appendComment(new Commentable(node, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendLocalCodeNodeComment(final INaviCodeNode node, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: codeNode argument can not be null");
    return appendComment(new Commentable(node, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public Integer appendLocalEdgeComment(final INaviEdge edge, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
    return appendComment(new Commentable(edge, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public Integer appendLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final String commentText, final Integer userId) {
    Preconditions.checkNotNull(codeNode, "Error: codeNode argument can not be null");
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    return appendComment(new Commentable(instruction, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public Integer appendSectionComment(final int moduleId, final int sectionId,
      final String commentText, final Integer userId) throws CouldntSaveDataException {
    return appendComment(new Commentable(sectionId, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public Integer appendTextNodeComment(final INaviTextNode node, final String commentText,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: groupNode argument can not be null");
    return appendComment(new Commentable(node, CommentScope.GLOBAL), userId, commentText);
  }

  @Override
  public Integer appendTypeInstanceComment(final int moduleId, final int instanceId,
      final String commentText, final Integer userId) throws CouldntSaveDataException {
    return appendComment(new Commentable(instanceId, CommentScope.LOCAL), userId, commentText);
  }

  @Override
  public void assignDebugger(final CAddressSpace addressSpace, final DebuggerTemplate debugger) {}

  @Override
  public void assignDebugger(final INaviModule module, final DebuggerTemplate debugger) {}

  @Override
  public CAddressSpace createAddressSpace(final INaviProject project, final String name) {
    return new CAddressSpace(1,
        name,
        "",
        new Date(),
        new Date(),
        new HashMap<INaviModule, IAddress>(),
        null,
        this,
        new MockProject());
  }

  @Override
  public DebuggerTemplate createDebuggerTemplate(final String name, final String host,
      final int port) {
    return new DebuggerTemplate(1, name, host, port, this);
  }

  @Override
  public CModule createModule(final INaviRawModule rawModule) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public CProject createProject(final String name) {
    return new CProject(1,
        name,
        "",
        new Date(),
        new Date(),
        0,
        new ArrayList<DebuggerTemplate>(),
        this);
  }

  @Override
  public int createSection(final int moduleId,
      final String name,
      final Integer commentId,
      final BigInteger startAddress,
      final BigInteger endAddress,
      final SectionPermission permission,
      final byte[] data) throws CouldntSaveDataException {

    INaviModule sectionModule = null;
    for (final INaviModule module : modules) {
      if (module.getConfiguration().getId() == moduleId) {
        sectionModule = module;
      }
    }

    final Section section = new Section(sectionId++,
        name,
        CommentManager.get(this),
        sectionModule,
        new CAddress(startAddress),
        new CAddress(endAddress),
        permission,
        data);
    sections.put(moduleId, section);
    return section.getId();
  }

  @Override
  public CTag createTag(final CTag parent, final String name, final String description,
      final TagType type) {
    final CTag newTag = new CTag(tagIdCounter, name, description, type, this);

    tagIdCounter++;

    return newTag;
  }

  @Override
  public TraceList createTrace(final INaviModule module, final String name,
      final String description) {
    return new TraceList(traceListCounter++, name, description, this);
  }


  @Override
  public TraceList createTrace(final INaviProject project, final String name,
      final String description) {
    return new TraceList(1, name, description, this);
  }

  @Override
  public int createType(final int moduleId,
      final String name,
      final int size,
      final Integer previousPointerTypeId,
      final boolean signed,
      final BaseTypeCategory category) {
    final RawBaseType rawBaseType =
        new RawBaseType(++typeId, name, size, previousPointerTypeId, signed, category);
    types.put(moduleId, rawBaseType);
    return rawBaseType.getId();
  }

  @Override
  public int createTypeInstance(final int moduleId,
      final String name,
      final Integer commentId,
      final int typeId,
      final int sectionId,
      final long sectionOffset) throws CouldntSaveDataException {
    final RawTypeInstance rawTypeInstance = new RawTypeInstance(moduleId,
        typeInstanceCounter++,
        name,
        commentId,
        typeId,
        sectionId,
        sectionOffset);
    typeInstances.put(moduleId, rawTypeInstance);
    return rawTypeInstance.getId();
  }

  @Override
  public void createTypeInstanceReference(final int moduleId, final long address,
      final int position, final int expressionId, final int typeInstanceId)
      throws CouldntSaveDataException {
    // TODO(jannewger): implement this method and change test implementations in
    // TypeInstanceContainerBackendTest accordingly.
  }

  @Override
  public int createTypeMember(final INaviModule module,
      final int containingTypeId,
      final int baseTypeId,
      final String name,
      final Optional<Integer> position,
      final Optional<Integer> numberelements,
      Optional<Integer> argumentIndex) {
    final int id = ++typeId;
    members.put(module, new RawTypeMember(id,
        name,
        baseTypeId,
        containingTypeId,
        position.orNull(),
        null,
        numberelements.orNull()));
    return id;
  }

  @Override
  public void createTypeSubstitution(final int treeNodeId,
      final int baseTypeId,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address,
      final INaviModule module) {
    substitutions.put(module, new RawTypeSubstitution(address,
        position,
        treeNodeId,
        baseTypeId,
        memberPath.toArray(new Integer[0]),
        offset));
  }

  @Override
  public CView createView(final INaviModule module, final INaviView view, final String name,
      final String description) {
    int counter = 100;
    for (INaviEdge edge : view.getGraph().getEdges()) {
      edge.setId(counter++);
    }
    for (INaviViewNode node : view.getGraph().getNodes()) {
      node.setId(counter++);
    }
    return new CView(viewCounter++,
        module,
        name,
        description,
        view.getType(),
        view.getGraphType(),
        new Date(),
        new Date(),
        view.getNodeCount(),
        view.getEdgeCount(),
        new HashSet<CTag>(),
        new HashSet<CTag>(),
        false,
        this);
  }

  @Override
  public CView createView(final INaviProject project, final INaviView view, final String name,
      final String description) {
    return new CView(1,
        project,
        name,
        description,
        ViewType.NonNative,
        GraphType.MIXED_GRAPH,
        new Date(),
        new Date(),
        0,
        0,
        new HashSet<CTag>(),
        new HashSet<CTag>(),
        false,
        this);
  }

  @Override
  public void deleteAddressSpace(final INaviAddressSpace addressSpace) {}

  @Override
  public void deleteDebugger(final DebuggerTemplate debugger) {}

  @Override
  public void deleteFunctionComment(final INaviFunction function, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(function, "Error: codeNode argument can not be null");
    deleteComment(new Commentable(function, CommentScope.GLOBAL), commentId, userId);
  }

  @Override
  public void deleteFunctionNodeComment(final INaviFunctionNode function, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    deleteComment(new Commentable(function, CommentScope.LOCAL), commentId, userId);
  }

  @Override
  public void deleteGlobalCodeNodeComment(final INaviCodeNode node, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: codeNode argument can not be null");
    deleteComment(new Commentable(node, CommentScope.GLOBAL), commentId, userId);
  }

  @Override
  public void deleteGlobalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(edge, "Error: codeNode argument can not be null");
    deleteComment(new Commentable(edge, CommentScope.GLOBAL), commentId, userId);
  }

  @Override
  public void deleteGlobalInstructionComment(final INaviInstruction instruction,
      final Integer commentId, final Integer userId) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    deleteComment(new Commentable(instruction, CommentScope.GLOBAL), commentId, userId);
  }

  @Override
  public void deleteGroupNodeComment(final INaviGroupNode node, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: grouNode argument can not be null");
    deleteComment(new Commentable(node, CommentScope.GLOBAL), commentId, userId);
  }

  @Override
  public void deleteLocalCodeNodeComment(final INaviCodeNode node, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    deleteComment(new Commentable(node, CommentScope.LOCAL), commentId, userId);
  }

  @Override
  public void deleteLocalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
    deleteComment(new Commentable(edge, CommentScope.LOCAL), commentId, userId);
  }

  @Override
  public void deleteLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final Integer commentId, final Integer userId) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    deleteComment(new Commentable(instruction, CommentScope.LOCAL), commentId, userId);
  }

  @Override
  public void deleteMember(final TypeMember member, final INaviModule module) {
    final RawTypeMember rawMember = findMember(member, module);
    if (rawMember != null) {
      members.remove(module, rawMember);
    }
  }

  @Override
  public void deleteModule(final INaviModule module) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void deleteProject(final INaviProject project) {}

  @Override
  public void deleteRawModule(final INaviRawModule module) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void deleteReference(final COperandTreeNode operandTreeNode, final IAddress target,
      final ReferenceType type) {
    if (references.containsKey(target)) {
      references.remove(target);
    }
  }

  @Override
  public void deleteSection(final Section section) {
    final Section currentSection = Iterables.find(
        sections.get(section.getModule().getConfiguration().getId()), new Predicate<Section>() {
          @Override
          public boolean apply(final Section currentSection) {
            return section.getId() == currentSection.getId();
          }
        });
    sections.remove(section.getModule().getConfiguration().getId(), currentSection);
  }

  @Override
  public void deleteSectionComment(final int moduleId, final int sectionId, final Integer commentId,
      final Integer userId) throws CouldntDeleteException {}

  @Override
  public void deleteTag(final ITreeNode<CTag> tag) {
    // deletion can not be modeled.
  }

  @Override
  public void deleteTagSubtree(final ITreeNode<CTag> tag) {
    // deletion can not be modeled.
  }

  @Override
  public void deleteTextNodeComment(final INaviTextNode textNode, final Integer commentId,
      final Integer userId) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteTrace(final TraceList trace) {
    // deletion of traces can not be modeled in the database.
  }

  @Override
  public void deleteType(final BaseType baseType, final INaviModule module) {

    final RawBaseType rawBaseType = new RawBaseType(baseType.getId(),
        baseType.getName(),
        baseType.getBitSize(),
        baseType.pointsTo() != null ? baseType.pointsTo().getId() : null,
        baseType.isSigned(),
        baseType.getCategory());

    types.remove(module.getConfiguration().getId(), rawBaseType);
  }

  @Override
  public void deleteTypeInstance(final int moduleId, final int typeInstanceId)
      throws CouldntDeleteException {}

  @Override
  public void deleteTypeInstanceComment(final int moduleId, final int instanceId,
      final Integer commentId, final Integer userId) throws CouldntDeleteException {
    throw new IllegalStateException("Not implemented");
  }

  @Override
  public void deleteTypeInstanceReference(final int moduleId, final BigInteger address,
      final int position, final int expressionid) throws CouldntDeleteException {
    // TODO(jannewger): implement this method and change test implementations in
    // TypeInstanceContainerBackendTest accordingly.
  }

  @Override
  public void deleteTypeSubstitution(final INaviModule module,
      final TypeSubstitution typeSubstitution) {
    final RawTypeSubstitution rawSubstitution = findSubstitution(
        typeSubstitution.getAddress().toBigInteger(), typeSubstitution.getPosition(),
        typeSubstitution.getOperandTreeNode().getId(), module);
    if (rawSubstitution != null) {
      substitutions.remove(module, rawSubstitution);
    }
  }

  @Override
  public void deleteUser(final IUser user) {
    users.remove(user);
  }

  @Override
  public void deleteView(final INaviView view) {
    // deletion in the database can not be modeled.
  }

  @Override
  public void editFunctionComment(final INaviFunction function, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editFunctionNodeComment(final INaviFunctionNode functionNode, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editGlobalCodeNodeComment(final INaviCodeNode codeNode, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editGlobalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editGlobalInstructionComment(final INaviInstruction instruction,
      final Integer commentId, final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editGroupNodeComment(final INaviGroupNode groupNode, final Integer id,
      final Integer userId, final String newComment) {}

  @Override
  public void editLocalCodeNodeComment(final INaviCodeNode codeNode, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editLocalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId, final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final Integer commentId, final Integer userId,
      final String newCommentText) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editSectionComment(final int moduleId, final int sectionId, final Integer commentId,
      final Integer userId, final String commentText) throws CouldntSaveDataException {}

  @Override
  public void editTextNodeComment(final INaviTextNode textNode, final Integer commentId,
      final Integer userId, final String newComment) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void editTypeInstanceComment(final int moduleId, final Integer commentId,
      final Integer userId, final String commentText) throws CouldntSaveDataException {}

  @Override
  public IUser editUserName(final IUser user, final String userName) {

    Preconditions.checkNotNull(user, "Error: user argument can not be null");
    Preconditions.checkNotNull(userName, "Error: userName argument can not be null");

    users.remove(user);
    final CUser newUser = new CUser(user.getUserId(), userName);
    users.add(newUser);
    return newUser;
  }

  @Override
  public ResultSet executeQuery(final String query) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviModule findModule(final int moduleId) {
    for (final INaviModule module : modules) {
      if (module.getConfiguration().getId() == moduleId) {
        return module;
      }
    }
    return null;
  }

  @Override
  public INaviProject findProject(final int projectId) {
    return null;
  }

  @Override
  public CConnection getConnection() {
    // TODO(timkornau)
    return null;
  }

  @Override
  public DatabaseVersion getDatabaseVersion() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviView> getDerivedViews(final INaviView view) {
    return new ArrayList<INaviView>();
  }

  @Override
  public Date getModificationDate(final CAddressSpace addressSpace) {
    return new Date();
  }

  @Override
  public Date getModificationDate(final INaviModule module) {
    return new Date();
  }

  @Override
  public Date getModificationDate(final INaviProject project) {
    return new Date();
  }

  @Override
  public Date getModificationDate(final INaviView view) {
    return new Date();
  }

  @Override
  public List<INaviModule> getModules() {
    return modules;
  }

  @Override
  public CTagManager getNodeTagManager() {
    return null;
  }

  @Override
  public List<INaviProject> getProjects() {
    return null;
  }

  @Override
  public List<INaviView> getViewsWithAddress(final INaviProject project,
      final List<UnrelocatedAddress> address, final boolean all) {
    return null;
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final INaviModule module,
      final List<UnrelocatedAddress> address, final boolean all) {
    return new ArrayList<INaviView>();
  }

  @Override
  public CTagManager getViewTagManager() {
    return null;
  }

  @Override
  public void initializeDatabase() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void initializeModule(final CModule module, final CModuleInitializeReporter reporter) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public CTag insertTag(final ITreeNode<CTag> parent, final String name, final String description,
      final TagType type) {
    final CTag newTag = new CTag(tagIdCounter, name, description, type, this);

    tagIdCounter++;

    return newTag;
  }

  @Override
  public boolean isExporterDatabaseFormatValid() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public boolean isInitialized() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public List<CAddressSpace> loadAddressSpaces(final INaviProject iNaviProject) {
    return Lists.newArrayList(new CAddressSpace(1,
        "",
        "",
        new Date(),
        new Date(),
        new HashMap<INaviModule, IAddress>(),
        null,
        this,
        new MockProject()));
  }

  @Override
  public CCallgraph loadCallgraph(final CModule module, final int id,
      final List<INaviFunction> functions) {
    return new CCallgraph(new ArrayList<ICallgraphNode>(), new ArrayList<ICallgraphEdge>());
  }

  @Override
  public List<ICallgraphView> loadCallgraphViews(final CModule module) {
    return new ArrayList<ICallgraphView>();
  }

  @Override
  public List<ICallgraphView> loadCallgraphViews(final CProject project) {
    return new ArrayList<ICallgraphView>();
  }

  @Override
  public ArrayList<IComment> loadCommentById(final Integer commentId) {
    return getCommentInternally(commentId);
  }

  @Override
  public byte[] loadData(final CModule module) {
    final byte[] bytes = {(byte) 0x90};
    return bytes;
  }

  @Override
  public DebuggerTemplateManager loadDebuggers() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviModule module,
      final Integer viewId) {
    return new ImmutableNaviViewConfiguration(viewId,
        "DB MODULE VIEW NAME",
        "DB MODULE VIEW DESCRIPTION",
        ViewType.NonNative,
        new Date(),
        new Date(),
        false,
        1,
        1);
  }

  @Override
  public ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviProject project,
      final Integer viewId) {
    return new ImmutableNaviViewConfiguration(viewId,
        "DB PROJECT VIEW NAME",
        "DB PROJECT VIEW DESCRIPTION",
        ViewType.NonNative,
        new Date(),
        new Date(),
        false,
        1,
        1);
  }

  @Override
  public ImmutableList<IFlowgraphView> loadFlowgraphs(final CModule module) {
    return new ImmutableList.Builder<IFlowgraphView>().build();
  }

  @Override
  public List<IFlowgraphView> loadFlowgraphs(final CProject project) {
    return new ArrayList<IFlowgraphView>();
  }

  @Override
  public INaviFunction loadFunction(final INaviModule module, final IAddress functionAddress) {
    return new MockFunction(this, functionAddress, module);
  }

  @Override
  public List<INaviFunction> loadFunctions(final INaviModule module,
      final List<IFlowgraphView> views) {
    final INaviFunction function = new CFunction(module,
        new MockView(),
        new CAddress(0x123),
        "",
        "",
        "",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null,
        this);

    functions.add(function);

    return Lists.newArrayList(function);
  }

  @Override
  public List<INaviView> loadMixedgraphs(final CModule module) {
    return new ArrayList<INaviView>();
  }

  @Override
  public List<INaviView> loadMixedgraphs(final CProject project) {
    return new ArrayList<INaviView>();
  }

  @Override
  public List<INaviModule> loadModules() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public List<Pair<IAddress, INaviModule>> loadModules(final CAddressSpace addressSpace) {
    return new ArrayList<Pair<IAddress, INaviModule>>();
  }

  @Override
  public HashMap<Integer, ArrayList<IComment>> loadMultipleCommentsById(
      final Collection<Integer> commentIds) {
    return new HashMap<Integer, ArrayList<IComment>>();
  }

  @Override
  public ICallgraphView loadNativeCallgraph(final CModule module) {
    return MockCreator.createView(this, module);
  }

  @Override
  public ImmutableList<IFlowgraphView> loadNativeFlowgraphs(final CModule module) {
    final IFlowgraphView view = MockCreator.createView(this, module, ViewType.Native);

    flowGraphViews.add(view);

    return new ImmutableList.Builder<IFlowgraphView>().add(view).build();
  }

  @Override
  public List<INaviProject> loadProjects() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public List<INaviRawModule> loadRawModules() {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public Map<Section, Integer> loadSections(final INaviModule module) {
    final HashMap<Section, Integer> sectionsMap = Maps.newHashMap();
    for (final Section section : sections.get(module.getConfiguration().getId())) {
      sectionsMap.put(section, null);
    }
    return sectionsMap;
  }

  @Override
  public HashMap<String, String> loadSettings(final CView view) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public CTagManager loadTagManager(final TagType type) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public List<TraceList> loadTraces(final CModule module) {
    return new ArrayList<TraceList>();
  }

  @Override
  public List<TraceList> loadTraces(final CProject project) {
    return new ArrayList<TraceList>();
  }

  @Override
  public RawBaseType loadType(final INaviModule module, final int baseTypeId)
      throws CouldntLoadDataException {
    final List<RawBaseType> rawTypes = types.get(module.getConfiguration().getId());
    if (rawTypes != null) {
      for (final RawBaseType rawType : rawTypes) {
        if (rawType.getId() == baseTypeId) {
          return rawType;
        }
      }
    }
    return null;
  }

  @Override
  public RawTypeInstance loadTypeInstance(final INaviModule module, final Integer typeInstanceId)
      throws CouldntLoadDataException {
    final RawTypeInstance instance = new RawTypeInstance(module.getConfiguration().getId(),
        typeInstanceId,
        "TEST_INSTANCE",
        null,
        module.getTypeManager().getTypes().get(0).getId(),
        0,
        11143);
    typeInstances.put(typeInstanceId, instance);
    return instance;
  }

  @Override
  public RawTypeInstanceReference loadTypeInstanceReference(final INaviModule module,
      final Integer typeInstanceId, final BigInteger address, final Integer position,
      final Integer expressionId) {
    throw new IllegalStateException("Error: Not yet implemented.");
  }

  @Override
  public List<RawTypeInstanceReference> loadTypeInstanceReferences(final INaviModule module)
      throws CouldntLoadDataException {
    return Lists.<RawTypeInstanceReference>newArrayList();
  }

  @Override
  public List<RawTypeInstance> loadTypeInstances(final INaviModule module)
      throws CouldntLoadDataException {
    return typeInstances.get(module.getConfiguration().getId());
  }

  @Override
  public RawTypeMember loadTypeMember(final INaviModule module, final int memberId)
      throws CouldntLoadDataException {
    final List<RawTypeMember> rawMembers = members.get(module);
    if (rawMembers != null) {
      for (final RawTypeMember rawMember : rawMembers) {
        if (rawMember.getId() == memberId) {
          return rawMember;
        }
      }
    }
    return null;
  }

  @Override
  public List<RawTypeMember> loadTypeMembers(final INaviModule module) {
    return Lists.newArrayList();
  }

  @Override
  public List<RawBaseType> loadTypes(final INaviModule module) {
    return types.get(module.getConfiguration().getId());
  }

  @Override
  public RawTypeSubstitution loadTypeSubstitution(final INaviModule module,
      final BigInteger address, final int position, final int expressionId)
      throws CouldntLoadDataException {
    return findSubstitution(address, position, expressionId, module);
  }

  @Override
  public List<RawTypeSubstitution> loadTypeSubstitutions(final INaviModule module) {
    return Lists.newArrayList();
  }

  @Override
  public List<IUser> loadUsers() {
    return users;
  }

  @Override
  public MutableDirectedGraph<INaviViewNode, INaviEdge> loadView(final INaviView view) {
    if (view.isLoaded()) {
      return (MutableDirectedGraph<INaviViewNode, INaviEdge>) view.getGraph();
    }

    return new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
        new ArrayList<INaviEdge>());
  }

  @Override
  public ImmutableBiMap<INaviView, INaviFunction> loadViewFunctionMapping(
      final List<IFlowgraphView> flowgraphs, final List<INaviFunction> functions,
      final CModule module) {

    return new ImmutableBiMap.Builder<INaviView, INaviFunction>().put(flowGraphViews.get(0),
        functions.get(0)).build();
  }

  @Override
  public void moveTag(final ITreeNode<CTag> parent, final ITreeNode<CTag> child,
      final TagType type) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String readSetting(final CModule module, final String key) {
    return "result_read_setting";
  }

  @Override
  public String readSetting(final CProject project, final String key) {
    return "result_read_setting";
  }

  @Override
  public void removeDebugger(final INaviProject project, final DebuggerTemplate debugger) {
    // deletion of project debuggers can not be modeled.
  }

  @Override
  public void removeModule(final INaviAddressSpace addressSpace, final INaviModule module) {
    // deletion can not be modeled.
  }

  @Override
  public void removeTag(final INaviView view, final CTag tag) {
    // deletion of view tags can not modeled.
  }

  @Override
  public void removeTagFromNode(final INaviViewNode node, final int tagId) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void forwardFunction(final INaviFunction source, final INaviFunction target) {
    return;
  }

  @Override
  public void save(final TraceList list) {
    // saving can not be modeled.
  }

  @Override
  public void save(final CView view) {
    // database saving can not be modeled.
  }

  @Override
  public void saveData(final INaviModule module, final byte[] data) {
    Preconditions.checkNotNull(module, "Module argument can not be null");
    Preconditions.checkNotNull(data, "Data argument can not be null");
    return;
  }

  @Override
  public void saveSettings(final CView view, final Map<String, String> settings) {}

  @Override
  public void saveTagToNode(final INaviViewNode node, final int tagId) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setDescription(final CAddressSpace addressSpace, final String description) {}

  @Override
  public void setDescription(final CTag tag, final String description) {}

  @Override
  public void setDescription(final TraceList traceList, final String comment) {}

  @Override
  public void setDescription(final INaviFunction function, final String comment) {}

  @Override
  public void setDescription(final INaviModule module, final String value) {}

  @Override
  public void setDescription(final INaviProject project, final String description) {}

  @Override
  public void setDescription(final INaviView view, final String description) {}

  @Override
  public void setFileBase(final INaviModule module, final IAddress addr) {}

  @Override
  public void setGlobalReplacement(final INaviOperandTreeNode operandTreeNode,
      final String replacement) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setHost(final DebuggerTemplate debugger, final String host) {}

  @Override
  public void setImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
      final IAddress address) {}

  @Override
  public void setImageBase(final INaviModule module, final IAddress addr) {}

  @Override
  public void setModules(final List<INaviModule> modules2) {
    modules.addAll(modules2);
  }

  @Override
  public void setName(final CAddressSpace addressSpace, final String name) {}

  @Override
  public void setName(final DebuggerTemplate debugger, final String name) {}

  @Override
  public void setName(final CTag tag, final String name) {}

  @Override
  public void setName(final TraceList traceList, final String name) {}

  @Override
  public void setName(final INaviFunction address, final String name) {}

  @Override
  public void setName(final INaviModule module, final String name) {}

  @Override
  public void setName(final INaviProject project, final String name) {}

  @Override
  public void setName(final INaviView view, final String name) {}

  @Override
  public void setPort(final DebuggerTemplate debugger, final int port) {}

  @Override
  public void setReplacement(final COperandTreeNode operandTreeNode, final String string) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setSectionName(final int moduleId, final int sectionId, final String name)
      throws CouldntSaveDataException {

    Section oldSection = null;

    for (final Section section : sections.get(moduleId)) {
      if (section.getId() == sectionId) {
        oldSection = section;
        break;
      }
    }
    if (oldSection != null) {
      final Section newSection = new Section(oldSection.getId(),
          name,
          CommentManager.get(this),
          oldSection.getModule(),
          oldSection.getStartAddress(),
          oldSection.getEndAddress(),
          oldSection.getSectionPermission(),
          oldSection.getData());
      sections.remove(moduleId, oldSection);
      sections.put(moduleId, newSection);
    }
  }

  @Override
  public void setStared(final INaviModule module, final boolean isStared) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setStared(final INaviView view, final boolean isStared) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setTypeInstanceName(final int moduleId, final int id, final String name)
      throws CouldntSaveDataException {
    final List<RawTypeInstance> rawInstances = typeInstances.get(moduleId);
    final RawTypeInstance rawInstance =
        Iterables.find(rawInstances, new Predicate<RawTypeInstance>() {
          @Override
          public boolean apply(final RawTypeInstance instance) {
            return instance.getId() == id;
          }
        });
    final RawTypeInstance newRawInstance = new RawTypeInstance(moduleId,
        id,
        name,
        rawInstance.getCommentId(),
        rawInstance.getTypeId(),
        rawInstance.getSectionId(),
        rawInstance.getSectionOffset());
    typeInstances.remove(moduleId, rawInstance);
    typeInstances.put(moduleId, newRawInstance);
  }

  @Override
  public void tagView(final INaviView view, final CTag tag) {
    // view tagging in the database can not be modeled.
  }

  @Override
  public void updateDatabase() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void updateMember(final TypeMember member,
      final String newName,
      final BaseType newBaseType,
      final Optional<Integer> newOffset,
      final Optional<Integer> newNumberOfElements,
      final Optional<Integer> newArgumentIndex,
      final INaviModule module) {
    final RawTypeMember rawMember = findMember(member, module);
    if (rawMember != null) {
      members.remove(module, rawMember);
      members.put(module, new RawTypeMember(rawMember.getId(),
          newName,
          newBaseType.getId(),
          rawMember.getParentId(),
          newOffset.orNull(),
          newArgumentIndex.orNull(),
          newNumberOfElements.orNull()));
    } else {
      throw new IllegalStateException("Trying to update non-existing member.");
    }
  }

  @Override
  public void updateMemberOffsets(final List<Integer> updatedMembers, final int delta,
      final List<Integer> implicitlyUpdatedMembers, final int implicitDelta,
      final INaviModule module) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned, final INaviModule module) {
    final RawBaseType rawType = findType(baseType, module);
    if (rawType != null) {
      types.remove(module.getConfiguration().getId(), rawType);
      types.put(module.getConfiguration().getId(), new RawBaseType(rawType.getId(),
          name,
          size,
          rawType.getPointerId(),
          isSigned,
          rawType.getCategory()));
    } else {
      throw new IllegalStateException("Trying to update non-existing type.");
    }
  }

  @Override
  public void updateTypeSubstitution(final TypeSubstitution substitution, final BaseType baseType,
      final List<Integer> memberPath, final int offset, final INaviModule module) {
    final RawTypeSubstitution rawSubstitution = findSubstitution(
        substitution.getAddress().toBigInteger(), substitution.getPosition(),
        substitution.getOperandTreeNode().getId(), module);
    if (rawSubstitution != null) {
      substitutions.remove(module, rawSubstitution);
      substitutions.put(module, new RawTypeSubstitution(substitution.getAddress(),
          substitution.getPosition(),
          substitution.getOperandTreeNode().getId(),
          baseType.getId(),
          Arrays.copyOf(memberPath.toArray(), memberPath.size(), Integer[].class),
          offset));
    } else {
      throw new IllegalStateException("Trying to update non-existing type substitution.");
    }
  }

  @Override
  public void writeSetting(final CModule module, final String key, final String value) {
    Preconditions.checkNotNull(module, "Module argument can not be null");
    Preconditions.checkNotNull(key, "Key argument can not be null");
    Preconditions.checkNotNull(value, "Value argument can not be null");
  }

  @Override
  public void writeSetting(final CProject project, final String key, final String value) {
    throw new RuntimeException("Not yet implemented");
  }

  private class Commentable {
    public Commentable(final Object commentable, final CommentScope scope) {
      Preconditions.checkNotNull(commentable, "Error: commentable argument can not be null");
      Preconditions.checkNotNull(scope, "Error: scope argument can not be null");
    }
  }

  @Override
  public void close() {}

  @Override
  public void addListener(SQLProviderListener listener) {}

  @Override
  public void removeListener(SQLProviderListener listener) {}
}
