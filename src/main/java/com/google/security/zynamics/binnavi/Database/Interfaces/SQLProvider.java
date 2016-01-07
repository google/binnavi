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
package com.google.security.zynamics.binnavi.Database.Interfaces;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.DatabaseVersion;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTree;
import com.google.security.zynamics.binnavi.Database.NodeParser.OperandTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
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
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
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
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface that must be implemented by all classes that provide database access.
 */
public interface SQLProvider extends AddressSpaceConfigurationBackend, AddressSpaceContentBackend {

  /**
   * Adds a debugger template for use with a project.
   *
   * @param project The project the debugger template is added to.
   * @param debugger The debugger template to add to the project.
   *
   * @throws CouldntSaveDataException Thrown if the change could not be stored in the database.
   */
  void addDebugger(INaviProject project, DebuggerTemplate debugger)
      throws CouldntSaveDataException;

  /**
   * Add a {@link SQLProviderListener listener} to the list of listeners getting informed about
   * changes in the {@link SQLProvider provider}.
   *
   * @param listener {@link SQLProviderListener} to receive notifications about the state of the
   *        provider.
   */
  void addListener(final SQLProviderListener listener);

  /**
   * Adds a outgoing reference to an expression tree operand.
   *
   * @param node The operand expression the reference is added to.
   * @param address The target address of the reference.
   * @param type The type of the reference.
   *
   * @throws CouldntSaveDataException Thrown if the reference could not be created.
   */
  void addReference(INaviOperandTreeNode node, final IAddress address, final ReferenceType type)
      throws CouldntSaveDataException;

  /**
   * Adds a user to the database.
   *
   * @param userName The userName of the new User.
   * @return The user as created in the database..
   * @throws CouldntSaveDataException
   */
  IUser addUser(String userName) throws CouldntSaveDataException;

  /**
   * Appends a global comment to a function.
   *
   * @param function The function where the global comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendFunctionComment(INaviFunction function, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a local comment to a function node.
   *
   * @param functionNode The function where the local comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendFunctionNodeComment(INaviFunctionNode functionNode, String commentText,
      Integer userId) throws CouldntSaveDataException;

  /**
   * Appends a global comment to a code node.
   *
   * @param codeNode The code node where the global comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendGlobalCodeNodeComment(INaviCodeNode codeNode, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a global comment to an edge.
   *
   * @param edge The edge where the global comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendGlobalEdgeComment(INaviEdge edge, String commentText, Integer userId)
      throws CouldntSaveDataException;


  /**
   * Appends a global comment to an instruction.
   *
   * @param instruction The instruction where the global comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendGlobalInstructionComment(INaviInstruction instruction, String commentText,
      Integer userId) throws CouldntSaveDataException;

  /**
   * Appends a group node comment to a group node.
   *
   * @param groupNode The group node where the comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendGroupNodeComment(INaviGroupNode groupNode, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a local comment to a code node.
   *
   * @param codeNode The code node where the local comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendLocalCodeNodeComment(INaviCodeNode codeNode, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a local comment to an edge.
   *
   * @param edge The edge where the local comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendLocalEdgeComment(INaviEdge edge, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a local comment to an instruction.
   *
   * @param codeNode The code node the instruction belongs to.
   * @param instruction The instruction where the local comment is appended.
   * @param commentText The text of the comment to be appended.
   * @param userId The id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException If the global comment could not be saved in the database.
   */
  Integer appendLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      String commentText, Integer userId) throws CouldntSaveDataException;

  /**
   * Appends a comment to the given section in the given module.
   *
   * @param moduleId The module id where the section is stored.
   * @param sectionId The section id of the section where to append the comment.
   * @param commentText The text of the comment.
   * @param userId The user id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException
   */
  Integer appendSectionComment(int moduleId, int sectionId, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a comment to a text node.
   *
   * @param textNode The text node where the comment is appended.
   * @param commentText The text of the comment.
   * @param userId The user id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException
   */
  Integer appendTextNodeComment(INaviTextNode textNode, String commentText, Integer userId)
      throws CouldntSaveDataException;

  /**
   * Appends a comment to the given type instance.
   *
   * @param moduleId The module that contains the type instance.
   * @param instanceId The id of the type instance.
   * @param commentText The text of the comment.
   * @param userId The id of the user that made the comment.
   * @return The id of the newly created comment.
   * @throws CouldntSaveDataException Thrown if the comment could not be saved in the database.
   */
  Integer appendTypeInstanceComment(int moduleId, int instanceId, String commentText,
      Integer userId) throws CouldntSaveDataException;

  /**
   * Assigns a debugger to a module. From now on that debugger template is used to debug the module.
   *
   * If the debugger argument is null, the existing debugger is removed from the module.
   *
   * @param module The module in question.
   * @param debugger The debugger that is assigned to the address space. This argument can be null.
   *
   * @throws CouldntSaveDataException Thrown if the debugger could not be assigned to the address
   *         space.
   */
  void assignDebugger(INaviModule module, DebuggerTemplate debugger)
      throws CouldntSaveDataException;


  /**
   * Close the SQLProvider.
   */
  void close();

  /**
   * Creates a new address space in a project.
   *
   * @param project The project where the address space is created.
   * @param name The name of the new address space.
   *
   * @return The created address space.
   *
   * @throws IllegalArgumentException Thrown if either of the two arguments is null or if the name
   *         argument is empty.
   * @throws CouldntSaveDataException Thrown if the new address space could not be created.
   */
  CAddressSpace createAddressSpace(INaviProject project, String name)
      throws CouldntSaveDataException;

  /**
   * Creates a new debugger in the database.
   *
   * @param name The name of the debugger.
   * @param host The host of the debugger.
   * @param port The port of the debugger.
   *
   * @return The created debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be added to the
   *         database.
   */
  DebuggerTemplate createDebuggerTemplate(String name, String host, int port)
      throws CouldntSaveDataException;

  /**
   * Creates a new module for a raw module.
   *
   * @param rawModule The raw module that backs the module.
   *
   * @return The created module.
   *
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded.
   * @throws CouldntSaveDataException Thrown if the module could not be created.
   */
  CModule createModule(INaviRawModule rawModule) throws CouldntLoadDataException,
      CouldntSaveDataException;

  /**
   * Creates a new project in the database.
   *
   * @param name The name of the project.
   *
   * @return The freshly created project.
   *
   * @throws CouldntSaveDataException Thrown if the project could not be added to the database.
   */
  CProject createProject(String name) throws CouldntSaveDataException;

  /**
   * Creates a new section in the database.
   *
   * @param moduleId The module that contains the section.
   * @param name The name of the section.
   * @param commentId The id of the associated comment.
   * @param startAddress The start address of the section.
   * @param endAddress The end address of the section.
   * @param permission The page protection permissions of the section.
   * @param data The data contained by the section.
   * @return The id of the section in the database.
   * @throws CouldntSaveDataException Thrown if the section could not be stored in the database.
   */
  int createSection(int moduleId,
      String name,
      Integer commentId,
      BigInteger startAddress,
      BigInteger endAddress,
      SectionPermission permission,
      byte[] data) throws CouldntSaveDataException;

  /**
   * Creates a new tag.
   *
   * @param parent The parent tag of the tag.
   * @param name The name of the tag.
   * @param description The description of the tag.
   * @param type The type of the tag.
   *
   * @return The new tag.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be created.
   */
  CTag createTag(CTag parent, String name, String description, TagType type)
      throws CouldntSaveDataException;

  /**
   * Creates a new module-specific debug trace.
   *
   * @param module The module the trace belongs to.
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   *
   * @return The created trace list.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be created.
   */
  TraceList createTrace(INaviModule module, String name, String description)
      throws CouldntSaveDataException;

  /**
   * Creates a new project-specific debug trace.
   *
   * @param project The project the trace belongs to.
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   *
   * @return The created trace list.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be created.
   */
  TraceList createTrace(INaviProject project, String name, String description)
      throws CouldntSaveDataException;

  /**
   * Creates a new base type in the database.
   *
   * @param moduleId The id of the module which contains the base type.
   * @param name The new base type to be created in the database.
   * @param size The size of the type in bits.
   * @param childPointerTypeId The id of the base type that corresponds to the child type according
   *        to the pointer hierarchy induced by the type system. If the new type is a value type,
   *        this value must be null.
   * @param signed Specifies if this type can represent signed values.
   * @param category The category of the base type.
   * @return Returns the type id of the added record.
   * @throws CouldntSaveDataException Thrown if the type could not be created.
   * @see BaseType
   */
  int createType(int moduleId,
      String name,
      int size,
      Integer childPointerTypeId,
      boolean signed,
      BaseTypeCategory category) throws CouldntSaveDataException;

  /**
   * Creates a new type instance in the database.
   *
   * @param moduleId The id of the module which contains the type instance.
   * @param name The name of the type instance.
   * @param commentId The associated comment id (can be null).
   * @param typeId The id of the associated base type.
   * @param sectionId The id of the section that contains the type instance.
   * @param sectionOffset The offset into the section where the instance is located.
   *
   * @return Returns the id of the created type instance.
   * @throws CouldntSaveDataException
   */
  int createTypeInstance(int moduleId,
      String name,
      Integer commentId,
      int typeId,
      int sectionId,
      long sectionOffset) throws CouldntSaveDataException;

  /**
   * Creates a type instance reference in the database.
   *
   * @param moduleId The id of the module that contains the reference.
   * @param address The address of the operand referencing the type instance.
   * @param position The position of the operand within the instruction.
   * @param expressionId The id of the corresponding expression.
   * @param typeInstanceId The id of the referred type instance.
   * @throws CouldntSaveDataException Thrown if the reference could not be written to the database.
   */
  void createTypeInstanceReference(int moduleId, long address, int position, int expressionId,
      int typeInstanceId) throws CouldntSaveDataException;

  /**
   * @param module The module that contains the member.
   * @param containingTypeId The id of the type where the member should be contained in.
   * @param baseTypeId The id of the member's base type.
   * @param name The name of the member.
   * @param offset The offset where the new member should be inserted. Can be null, in this case the
   *        member indicates an array type.
   * @param numberOfElements The number of elements this member has.
   * @param argumentIndex The index of the element in the function prototype.
   * @return The id of the added record.
   * @throws CouldntSaveDataException Thrown if the member couldn't be saved to the database.
   */
  int createTypeMember(INaviModule module,
      int containingTypeId,
      int baseTypeId,
      String name,
      Optional<Integer> offset,
      Optional<Integer> numberOfElements,
      Optional<Integer> argumentIndex) throws CouldntSaveDataException;

  /**
   * Writes a new type substitution to the database, that associates an operand tree node with the
   * nth member of a given base type.
   *
   * @param treeNodeId The database id of the operand tree node.
   * @param baseTypeId The id of the base type that is associated with the operand tree node.
   * @param memberPath The sequence of member ids to unambigiuosly address a member when dealing
   *        with unions.
   * @param position The zero-based index position of the tree node operand within its instruction.
   * @param offset The position in the list of members of the corresponding base type.
   * @param address The address of the instruction that is annotated with this type substitution.
   * @param module The module where this type substitution belongs to.
   */
  void createTypeSubstitution(int treeNodeId,
      final int baseTypeId,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address,
      final INaviModule module) throws CouldntSaveDataException;

  /**
   * Creates a new module view by copying an existing view.
   *
   * @param module The module the new view belongs to.
   * @param view The view to be copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The new view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be created.
   */
  CView createView(INaviModule module, INaviView view, String name, String description)
      throws CouldntSaveDataException;

  /**
   * Creates a new project view by copying an existing view.
   *
   * @param project The project the new view belongs to.
   * @param view The view to be copied.
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The new view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be created.
   */
  CView createView(INaviProject project, INaviView view, String name, String description)
      throws CouldntSaveDataException;

  /**
   * Deletes an address space from the database.
   *
   * @param addressSpace The address space to delete.
   *
   * @throws CouldntDeleteException Thrown if the address space could not be deleted.
   */
  void deleteAddressSpace(INaviAddressSpace addressSpace) throws CouldntDeleteException;

  /**
   * Deletes an debugger from the database.
   *
   * @param debugger The id of the debugger
   *
   * @throws CouldntDeleteException Thrown if the address space could not be deleted.
   */
  void deleteDebugger(DebuggerTemplate debugger) throws CouldntDeleteException;

  /**
   * Deletes a global comment from the list of comment associated to a function.
   *
   * @param function The function where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteFunctionComment(INaviFunction function, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a local comment from the list of comment associated to a function.
   *
   * @param functionNode The function node where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteFunctionNodeComment(INaviFunctionNode functionNode, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a global comment from the list of comment associated to a code node.
   *
   * @param codeNode The code node where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The is of the user which is currently active.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteGlobalCodeNodeComment(INaviCodeNode codeNode, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a global comment from the list of comment associated to an edge.
   *
   * @param edge The edge where the comment is deleted.
   * @param commentId The comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteGlobalEdgeComment(INaviEdge edge, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a global comment from the list of comment associated to an instruction.
   *
   * @param instruction The instruction where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteGlobalInstructionComment(INaviInstruction instruction, Integer commentId,
      Integer userId) throws CouldntDeleteException;

  /**
   * Deletes a comment from the list of comments associated to a group node.
   *
   * @param grouNode The group node where the comment is associated to.
   * @param commentId The comment id of the comment which is deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  void deleteGroupNodeComment(INaviGroupNode grouNode, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a local comment from the list of comment associated to a code node.
   *
   * @param codeNode The code node where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteLocalCodeNodeComment(INaviCodeNode codeNode, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a local comment from the list of comment associated to an edge.
   *
   * @param edge The edge where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException
   */
  void deleteLocalEdgeComment(INaviEdge edge, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a local comment from the list of comment associated to an instruction.
   *
   * @param codeNode The code node where the instruction is located in.
   * @param instruction The instruction where the comment is deleted.
   * @param commentId The id of the comment which is deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      Integer commentId, Integer userId) throws CouldntDeleteException;

  /**
   * Deletes the given member from the database.
   *
   * @param member The member to delete.
   * @param module The module that contains the member.
   * @throws CouldntDeleteException Thrown if the member could not be deleted.
   */
  void deleteMember(TypeMember member, INaviModule module) throws CouldntDeleteException;

  /**
   * Deletes a module from the database.
   *
   * @param module The module to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the module could not be deleted.
   */
  void deleteModule(INaviModule module) throws CouldntDeleteException;

  /**
   * Deletes a project from the database.
   *
   * @param project The project to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the project could not be deleted.
   */
  void deleteProject(INaviProject project) throws CouldntDeleteException;

  /**
   * Deletes a raw module from the database.
   *
   * @param module The raw module to delete.
   *
   * @throws CouldntDeleteException Thrown if the module could not be deleted.
   */
  void deleteRawModule(INaviRawModule module) throws CouldntDeleteException;

  /**
   * Deletes a reference from an operand expression.
   *
   * @param node The operand expression from which the reference is deleted.
   * @param address The target address of the reference to delete.
   * @param type The type of the reference to delete.
   *
   * @throws CouldntDeleteException Thrown if the reference could not be deleted.
   */
  void deleteReference(COperandTreeNode node, IAddress address, ReferenceType type)
      throws CouldntDeleteException;

  /**
   * Deletes a {@link Section} in the database.
   *
   * @param section The {@link Section} to be deleted.
   * @throws CouldntLoadDataException if the {@link Section} could not be deleted from the database.
   */
  void deleteSection(Section section) throws CouldntLoadDataException;

  /**
   * Deletes a section comment.
   *
   * @param moduleId The module id of the module that contains the section.
   * @param sectionId The section id of the section where to delete the comment.
   * @param commentId The id of the comment to be deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteSectionComment(int moduleId, int sectionId, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a tag from the database.
   *
   * @param tag The tag to delete.
   *
   * @throws CouldntDeleteException Thrown if the tag could not be deleted.
   */
  void deleteTag(ITreeNode<CTag> tag) throws CouldntDeleteException;

  /**
   * Deletes a tag and all of its children from the database.
   *
   * @param tag The top tag of the subtree to delete.
   *
   * @throws CouldntDeleteException Thrown if the tags could not be deleted from the database.
   */
  void deleteTagSubtree(ITreeNode<CTag> tag) throws CouldntDeleteException;

  /**
   * Deletes a text node comment from the list of comments associated to the given text node.
   *
   * @param textNode The text node the comment is associated to.
   * @param commentId The comment id of the comment which is deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteTextNodeComment(final INaviTextNode textNode, Integer commentId, final Integer userId)
      throws CouldntDeleteException;

  /**
   * Deletes a trace from the database.
   *
   * @param trace The trace to delete.
   *
   * @throws CouldntDeleteException Thrown if the trace could not be deleted.
   */
  void deleteTrace(TraceList trace) throws CouldntDeleteException;

  /**
   * Delete a base type from the database.
   *
   * @param baseType The type to be deleted.
   * @param module The module that contains the base type.
   * @throws CouldntDeleteException Thrown if the base type could not be deleted.
   */
  void deleteType(BaseType baseType, INaviModule module) throws CouldntDeleteException;

  /**
   * Delete a {@link TypeInstance} from the database.
   *
   * @param moduleId The module id of the {@link TypeInstance}.
   * @param typeInstanceId The id of the {@link TypeInstance}.
   *
   * @throws CouldntDeleteException if the {@link TypeInstance} could not be deleted from the
   *         database.
   */
  void deleteTypeInstance(int moduleId, int typeInstanceId) throws CouldntDeleteException;



  /**
   * Deletes a type instance comment.
   *
   * @param moduleId The id of the module that contains the type instance.
   * @param instanceId The id of the type instance.
   * @param commentId The id of the comment to be deleted.
   * @param userId The id of the user who owns the comment.
   * @throws CouldntDeleteException Thrown if the comment could not be deleted.
   */
  void deleteTypeInstanceComment(int moduleId, int instanceId, Integer commentId, Integer userId)
      throws CouldntDeleteException;

  /**
   * Delete a {@link TypeInstanceReference} from the database.
   *
   * @param moduleId The module id of the {@link TypeInstanceReference}.
   * @param address The address of the {@link INaviInstruction} where {@link TypeInstanceReference}
   *        is locate in.
   * @param position The position of the {@link INaviOperandTree} within the
   *        {@link INaviInstruction} where the {@link TypeInstanceReference} is located.
   * @param expressionid The id of the {@link INaviOperandTreeNode} to which the
   *        {@link TypeInstanceReference} is associated.
   *
   * @throws CouldntDeleteException if the {@link TypeInstanceReference} could not be deleted from
   *         the database.
   */
  void deleteTypeInstanceReference(int moduleId, BigInteger address, int position, int expressionid)
      throws CouldntDeleteException;

  /**
   * Deletes the given type substitution from the database.
   *
   * @param module The module that contains the type substitution.
   * @param typeSubstitution The type substitution to delete.
   *
   * @throws CouldntDeleteException Thrown if the type substitution couldn't be deleted.
   */
  void deleteTypeSubstitution(INaviModule module, TypeSubstitution typeSubstitution)
      throws CouldntDeleteException;

  /**
   * Delete a user from the database.
   *
   * @param user The user to delete from the database.
   * @throws CouldntDeleteException
   */
  void deleteUser(IUser user) throws CouldntDeleteException;

  /**
   * Deletes a view from the database.
   *
   * @param view The view to delete.
   *
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  void deleteView(INaviView view) throws CouldntDeleteException;

  /**
   * Edits a global comment associated with a function.
   *
   * @param function The function where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The id of the currently active user.
   * @param newCommentText The text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editFunctionComment(INaviFunction function, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a local comment associated with a function.
   *
   * @param functionNode The function where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The id of the currently active user.
   * @param newCommentText The comment text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editFunctionNodeComment(INaviFunctionNode functionNode, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a global comment associated with a code node.
   *
   * @param codeNode The code node where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newCommentText The new text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editGlobalCodeNodeComment(INaviCodeNode codeNode, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a global comment associated with an edge.
   *
   * @param edge The edge where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The id of the currently active user.
   * @param newCommentText The text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editGlobalEdgeComment(INaviEdge edge, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a global comment associated with an instruction.
   *
   * @param instruction The instruction where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newCommentText The text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editGlobalInstructionComment(INaviInstruction instruction, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a comment associated with a group node.
   *
   * @param groupNode The group node the comment is associated to.
   * @param commentId The id of the comment.
   * @param userId The id of the currently active user.
   * @param newComment The text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editGroupNodeComment(INaviGroupNode groupNode, Integer commentId, Integer userId,
      String newComment) throws CouldntSaveDataException;

  /**
   * Edits a local comment associated with a code node.
   *
   * @param codeNode The code node where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newCommentText The comment text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editLocalCodeNodeComment(INaviCodeNode codeNode, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a local comment associated with an edge.
   *
   * @param edge The edge where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newCommentText The comment text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editLocalEdgeComment(INaviEdge edge, Integer commentId, Integer userId,
      String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a local comment associated with an instruction
   *
   * @param codeNode The code node where the instruction is located.
   * @param instruction The function where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newCommentText The comment text to be inserted into the comment.
   * @throws CouldntSaveDataException if the edited comment could not be saved to the database.
   */
  void editLocalInstructionComment(INaviCodeNode codeNode, INaviInstruction instruction,
      Integer commentId, Integer userId, String newCommentText) throws CouldntSaveDataException;

  /**
   * Edits a comment associated with a section.
   *
   * @param moduleId The module id of the module the section is associated to.
   * @param sectionId The id of the section where to edit the comment.
   * @param commentId The id of the comment to edit.
   * @param userId The id of the currently active user.
   * @param commentText The new comment text to be saved in the comment.
   *
   * @throws CouldntSaveDataException if the changed to the comment could not be saved to the
   *         database.
   */
  void editSectionComment(int moduleId, int sectionId, Integer commentId, Integer userId,
      String commentText) throws CouldntSaveDataException;

  /**
   * Edits a comment associated with a text node.
   *
   * @param textNode The text node to which the comment is associated.
   * @param commentId The id of the comment which will be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment text to be saved in the comment.
   *
   * @throws CouldntSaveDataException if the changes to the comment could not be saved to the
   *         database.
   */
  void editTextNodeComment(INaviTextNode textNode, Integer commentId, Integer userId,
      String newComment) throws CouldntSaveDataException;

  /**
   * Edits a comment associated with a type instance.
   *
   * @param moduleId The module that contains the type instance.
   * @param commentId The id of the comment to be edited.
   * @param userId The user id of the user who owns the comment.
   * @param commentText The new comment text.
   * @throws CouldntSaveDataException if the changed to the comment could not be saved to the
   *         database.
   */
  void editTypeInstanceComment(int moduleId, Integer commentId, Integer userId, String commentText)
      throws CouldntSaveDataException;

  /**
   * Edits a user name in the database.
   *
   * @param user The user where the user name is changed.
   * @param userName The user name to change the name to.
   * @return The newly generated user object to be used from now on.
   *
   * @throws CouldntSaveDataException
   */
  IUser editUserName(IUser user, String userName) throws CouldntSaveDataException;

  /**
   * Executes a query on the database.
   *
   * @param query The query to execute.
   *
   * @return The result set of the query.
   *
   * @throws SQLException Thrown if the query could not be executed.
   */
  ResultSet executeQuery(String query) throws SQLException;

  /**
   * Locates a module by module id.
   *
   * @param moduleId The module id to search for.
   * @return {@link INaviModule} if found.
   */
  INaviModule findModule(final int moduleId);

  /**
   * Locates a project by project id.
   *
   * @param projectId The project id to search for.
   * @return {@link INaviProject} if found.
   */
  INaviProject findProject(final int projectId);

  /**
   * Returns the connection associated with this provider.
   *
   * @return The stored connection.
   */
  CConnection getConnection();

  /**
   * Returns the database version of the database.
   *
   * @return The database version.
   *
   * @throws CouldntLoadDataException Thrown if the database version could not be determined.
   */
  DatabaseVersion getDatabaseVersion() throws CouldntLoadDataException;

  /**
   * Returns the derived views of the given view.
   *
   * @param view The view whose derived views are returned.
   *
   * @return The derived views.
   *
   * @throws CouldntLoadDataException Thrown if the derived views could not be determined.
   */
  List<INaviView> getDerivedViews(INaviView view) throws CouldntLoadDataException;

  /**
   * Returns the last modification date of a given module.
   *
   * @param module The module in question.
   *
   * @return The modification date of the module.
   *
   * @throws CouldntLoadDataException Thrown if the module could not be loaded.
   */
  Date getModificationDate(INaviModule module) throws CouldntLoadDataException;

  /**
   * Returns the last modification date of a given project.
   *
   * @param project The project in question.
   *
   * @return The modification date of the project.
   *
   * @throws CouldntLoadDataException Thrown if the project could not be loaded.
   */
  Date getModificationDate(INaviProject project) throws CouldntLoadDataException;

  /**
   * Returns the last modification date of a given view.
   *
   * @param view The view in question.
   *
   * @return The modification date of the view.
   *
   * @throws CouldntLoadDataException Thrown if the view could not be loaded.
   */
  Date getModificationDate(INaviView view) throws CouldntLoadDataException;

  List<INaviModule> getModules();

  CTagManager getNodeTagManager();

  List<INaviProject> getProjects();


  /**
   * Returns a list of all project views that contain instructions at a given address.
   *
   * @param project The project to search through.
   * @param address The address of the instruction to find.
   * @param all True, to search for views that contain all addresses. False, for any addresses.
   *
   * @return A list of views.
   *
   * @throws CouldntLoadDataException Thrown if the view data could not be loaded.
   */
  List<INaviView> getViewsWithAddress(INaviProject project, List<UnrelocatedAddress> address,
      boolean all) throws CouldntLoadDataException;


  /**
   * Returns a list of all module views that contain instructions at a given address.
   *
   * @param module The module to search through.
   * @param address The address of the instruction to find.
   * @param all True, to search for views that contain all addresses. False, for any addresses.
   *
   * @return A list of views.
   *
   * @throws CouldntLoadDataException Thrown if the view data could not be loaded.
   */
  List<INaviView> getViewsWithAddresses(INaviModule module, List<UnrelocatedAddress> address,
      boolean all) throws CouldntLoadDataException;

  CTagManager getViewTagManager();

  /**
   * Initializes the tables of the database.
   *
   * @throws CouldntInitializeDatabaseException Thrown if the tables could not be created.
   * @throws CouldntLoadDataException Thrown if the state of the database could not be determined.
   */
  void initializeDatabase() throws CouldntInitializeDatabaseException, CouldntLoadDataException;

  /**
   * Initializes a module.
   *
   * @param module The module to initialize.
   * @param reporter Reports progress.
   *
   * @throws CouldntSaveDataException Thrown if the module could not be initialized.
   */
  void initializeModule(CModule module, CModuleInitializeReporter reporter)
      throws CouldntSaveDataException;

  /**
   * Inserts a tag into the database.
   *
   * @param parent The parent tag of the tag to insert.
   * @param name The name of the new tag.
   * @param description The description of the new tag-
   * @param type The type of the new tag.
   *
   * @return The new tag.
   *
   * @throws CouldntSaveDataException Thrown if the new tag could not be inserted.
   */
  CTag insertTag(ITreeNode<CTag> parent, String name, String description, TagType type)
      throws CouldntSaveDataException;

  /**
   * Checks whether the format of the exporter database tables is valid.
   *
   * @return True, if the exporter table format is valid. False, otherwise.
   *
   * @throws CouldntLoadDataException Thrown if the state of the exporter tables could not be
   *         determined.
   */
  boolean isExporterDatabaseFormatValid() throws CouldntLoadDataException;

  /**
   * Determines whether the database is in a known good state.
   *
   * @return True, if the database is in a good state. False, if the database does not contain the
   *         BinNavi tables.
   *
   * @throws CouldntLoadDataException Thrown if the state of the database could not be determined.
   * @throws InvalidDatabaseException Thrown if the database is in an invalid state.
   */
  boolean isInitialized() throws CouldntLoadDataException, InvalidDatabaseException;

  /**
   * Loads the address spaces of a project.
   *
   * @param iNaviProject The project whose address spaces will be loaded.
   *
   * @return The loaded address spaces.
   *
   * @throws CouldntLoadDataException Thrown if the address spaces could not be loaded.
   */
  List<CAddressSpace> loadAddressSpaces(INaviProject iNaviProject) throws CouldntLoadDataException;

  /**
   * Loads the call graph of a module.
   *
   * @param module The module whose call graph is loaded.
   * @param callgraphId The ID of the call graph.
   * @param functions The functions which are part of the module.
   *
   * @return The call graph of the module.
   *
   * @throws CouldntLoadDataException Thrown if the call graph could not be loaded.
   */
  CCallgraph loadCallgraph(CModule module, int callgraphId, List<INaviFunction> functions)
      throws CouldntLoadDataException;

  /**
   * Loads all custom call graph views of a given module. Note that this function only loads the
   * non-native call graph views. For the native call graph please see loadNativeCallgraph.
   *
   * @param module The module which owns the call graph views.
   *
   * @return The loaded call graph views.
   *
   * @throws CouldntLoadDataException Thrown if the call graph views could not be loaded.
   */
  List<ICallgraphView> loadCallgraphViews(CModule module) throws CouldntLoadDataException;

  /**
   * Loads the non-native call graph views of a project.
   *
   * @param project The project from where the views are loaded.
   *
   * @return A list of non-native call graph views.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  List<ICallgraphView> loadCallgraphViews(CProject project) throws CouldntLoadDataException;

  /**
   * Loads the list of comments by the id of the comment.
   *
   * @param commentId The comment id of the comments to load.
   *
   * @return The List of comments which is the list of all ancestors of the comment with the id
   *         given as argument plus the comment itself.
   * @throws CouldntLoadDataException if the comments could not be loaded from the database.
   */
  ArrayList<IComment> loadCommentById(final Integer commentId) throws CouldntLoadDataException;

  /**
   * Loads the data of a module from the database.
   *
   * @param module The module whose data is loaded.
   *
   * @return The module data loaded from the database.
   *
   * @throws CouldntLoadDataException Thrown if the module data could not be loaded.
   */
  byte[] loadData(CModule module) throws CouldntLoadDataException;

  /**
   * Loads all debugger templates of a database.
   *
   * @return The loaded debugger template manager.
   *
   * @throws CouldntLoadDataException Thrown if the debugger templates could not be loaded.
   */
  DebuggerTemplateManager loadDebuggers() throws CouldntLoadDataException;

  ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviModule module,
      final Integer viewId) throws CouldntLoadDataException;

  ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviProject project,
      final Integer viewId) throws CouldntLoadDataException;

  /**
   * Loads the custom flow graph views of a given module. Note that this function only loads the
   * non-native flow graph views. For the native flow graphs please see loadNativeFlowgraphs.
   *
   * @param module The module which owns the flow graph views.
   *
   * @return The loaded flow graph views.
   *
   * @throws CouldntLoadDataException Thrown if the flow graph views could not be loaded.
   */
  ImmutableList<IFlowgraphView> loadFlowgraphs(CModule module) throws CouldntLoadDataException;

  /**
   * Loads the flow graph views of a project.
   *
   * @param project The project whose flow graph views are loaded.
   *
   * @return The loaded flow graph views.
   *
   * @throws CouldntLoadDataException Thrown if the flow graph views could not be loaded.
   */
  List<IFlowgraphView> loadFlowgraphs(CProject project) throws CouldntLoadDataException;

  INaviFunction loadFunction(INaviModule module, IAddress functionAddress)
      throws CouldntLoadDataException;

  /**
   * Loads all the functions of a module.
   *
   * @param module The module which owns all the functions.
   * @param views The views that potentially back the function.
   *
   * @return The loaded functions.
   *
   * @throws CouldntLoadDataException Thrown if the functions could not be loaded.
   */
  List<INaviFunction> loadFunctions(INaviModule module, final List<IFlowgraphView> views)
      throws CouldntLoadDataException;

  /**
   * Loads the non-native mixed-graph views of a module.
   *
   * @param module The module from where the views are loaded.
   *
   * @return A list of non-native mixed-graph views.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  List<INaviView> loadMixedgraphs(CModule module) throws CouldntLoadDataException;

  /**
   * Loads the non-native mixed-graph views of a project.
   *
   * @param project The project from where the views are loaded.
   *
   * @return A list of non-native mixed-graph views.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  List<INaviView> loadMixedgraphs(CProject project) throws CouldntLoadDataException;

  /**
   * Loads all the modules which are available in the database.
   *
   * @return The list of loaded modules.
   *
   * @throws CouldntLoadDataException Thrown if the functions could not be loaded.
   */
  List<INaviModule> loadModules() throws CouldntLoadDataException;

  /**
   * Loads all the modules of an address space.
   *
   *  Note that the member modules of an address space are the same modules as the globally
   * available modules. To preserve synchronization between module objects used in different
   * contexts, this function must return a subset of the objects already returned in the global
   * loadModules() function.
   *
   * @param addressSpace The address space which contains the modules.
   *
   * @return The list of loaded modules.
   *
   * @throws CouldntLoadDataException Thrown if the modules could not be loaded.
   */
  List<Pair<IAddress, INaviModule>> loadModules(CAddressSpace addressSpace)
      throws CouldntLoadDataException;

  /**
   * Loads the native call graph of a module.
   *
   * @param module The module that owns the native call graph.
   *
   * @return The native call graph of the module.
   *
   * @throws CouldntLoadDataException Thrown if the native call graph could not be loaded.
   */
  ICallgraphView loadNativeCallgraph(CModule module) throws CouldntLoadDataException;

  /**
   * Loads the native flow graph views of a module.
   *
   * @param module The module from where the views are loaded.
   *
   * @return A list of non-native flow graph views.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  ImmutableList<IFlowgraphView> loadNativeFlowgraphs(CModule module)
      throws CouldntLoadDataException;

  /**
   * Loads the projects of a database.
   *
   * @return A list of projects that contains the projects stored in the database.
   *
   * @throws CouldntLoadDataException Thrown if the projects could not be loaded from the database.
   */
  List<INaviProject> loadProjects() throws CouldntLoadDataException;

  /**
   * Loads the raw modules of a database.
   *
   * @return A list of raw modules that contains the raw modules stored in the database.
   *
   * @throws CouldntLoadDataException Thrown if the raw modules could not be loaded from the
   *         database.
   */
  List<INaviRawModule> loadRawModules() throws CouldntLoadDataException;

  /**
   * Loads the sections that comprise the given module from the database.
   *
   * @param module The module for which to load the sections that represent the module.
   * @return The list of section instances.
   * @throws CouldntLoadDataException Thrown if the sections could not be loaded from the database.
   */
  Map<Section, Integer> loadSections(INaviModule module) throws CouldntLoadDataException;

  /**
   * Loads the settings of a view from the database.
   *
   * @param view The view whose settings are loaded.
   *
   * @return The settings map of the view.
   *
   * @throws CouldntLoadDataException Thrown if the settings could not be loaded.
   */
  Map<String, String> loadSettings(CView view) throws CouldntLoadDataException;

  /**
   * Loads the tag manager for tags of a given type.
   *
   * @param type The tag type for which the tag manager is loaded.
   *
   * @return The loaded tag manager.
   *
   * @throws CouldntLoadDataException Thrown if the tag manager could not be loaded.
   */
  CTagManager loadTagManager(TagType type) throws CouldntLoadDataException;

  /**
   * Loads all traces of a module.
   *
   * @param module The module whose traces are loaded.
   *
   * @return The loaded traces.
   *
   * @throws CouldntLoadDataException Thrown if loading the traces failed.
   */
  List<TraceList> loadTraces(CModule module) throws CouldntLoadDataException;


  /**
   * Loads all traces of a project.
   *
   * @param project The project whose traces are loaded.
   *
   * @return The loaded traces.
   *
   * @throws CouldntLoadDataException Thrown if loading the traces failed.
   */
  List<TraceList> loadTraces(CProject project) throws CouldntLoadDataException;

  /**
   * Loads a single {@link RawBaseType base type} from the database.
   *
   * @param module The {@link INaviModule} the {@link RawBaseType base type} is associacted to.
   * @param baseTypeId The id of the {@link RawBaseType base type} to load from the database.
   *
   * @return A {@link RawBaseType base type} loaded from the database.
   */
  RawBaseType loadType(INaviModule module, int baseTypeId) throws CouldntLoadDataException;

  /**
   * Loads a type instance for the given module and the given id from the database.
   *
   * @param module The {@link INaviModule module} whose {@link RawTypeInstance type instance} to
   *        load.
   * @param typeInstanceId The {@link Integer id} of the {@link RawTypeInstance type instance} to
   *        load.
   * @return A {@link RawTypeInstance type instance} if it is present in the database.
   *
   * @throws CouldntLoadDataException Thrown if the {@link RawTypeInstance type instance} could not
   *         be loaded from the database.
   */
  RawTypeInstance loadTypeInstance(INaviModule module, Integer typeInstanceId)
      throws CouldntLoadDataException;

  /**
   * Loads a {@link RawTypeInstanceReference type instance reference} for the given module from the
   * database.
   *
   * @param module The {@link INaviModule} whose {@link RawTypeInstanceReference reference} to load.
   * @param typeInstanceId The id of the {@link TypeInstance instance} this
   *        {@link RawTypeInstanceReference reference} belongs to.
   * @param address The {@link INaviInstruction instruction} address where the reference is
   *        attached.
   * @param position The {@link OperandTree operand tree} position in the instruction.
   * @param expressionId The {@link OperandTreeNode node} id within the {@link OperandTree operand
   *        tree}.
   */
  RawTypeInstanceReference loadTypeInstanceReference(INaviModule module, Integer typeInstanceId,
      BigInteger address, Integer position, Integer expressionId) throws CouldntLoadDataException;

  /**
   * Loads all type instance references for the given module from the database.
   *
   * @param module The module whose type instance references are loaded.
   * @return The list of type instance references for the given module.
   *
   * @throws CouldntLoadDataException if the type instance references could not be loaded from the
   *         database.
   */
  List<RawTypeInstanceReference> loadTypeInstanceReferences(INaviModule module)
      throws CouldntLoadDataException;

  /**
   * Loads all type instances for the given module from the database.
   *
   * @param module The module whose type instances should be loaded.
   * @return The list of type instances for the given module.
   *
   * @throws CouldntLoadDataException Thrown if the type instances could not be loaded from the
   *         database.
   */
  List<RawTypeInstance> loadTypeInstances(INaviModule module) throws CouldntLoadDataException;

  /**
   * Loads a single {@link RawTypeMember type member} from the database.
   *
   * @param module The {@link INaviModule} where this {@link RawTypeMember type member} is
   *        associated to.
   * @param typeMemberId The id of the {@link RawTypeMember type member} to load from the database.
   *
   * @return A {@link RawTypeMember} loaded from the database
   */
  RawTypeMember loadTypeMember(INaviModule module, int typeMemberId)
      throws CouldntLoadDataException;

  /**
   * Loads all raw type members for the given module from the database.
   *
   * @param module The module for which to load the type members.
   * @return The list of raw type members for the given module.
   * @throws CouldntLoadDataException Thrown if loading the type members failed.
   */
  List<RawTypeMember> loadTypeMembers(INaviModule module) throws CouldntLoadDataException;

  /**
   * Loads all raw base types for the given module from the database.
   *
   * @param module The module for which to load the types.
   * @return The list of raw base types for the given module.
   * @throws CouldntLoadDataException Thrown if the raw types couldn't be loaded from the database.
   */
  List<RawBaseType> loadTypes(INaviModule module) throws CouldntLoadDataException;

  /**
   * Loads a single {@link RawTypeSubstitution type substitution} from the database.
   *
   * @param module The {@link INaviModule} the {@link RawTypeSubstitution type substitution} is
   *        associated to.
   * @param address The {@link INaviInstruction instruction} address to which the
   *        {@link RawTypeSubstitution type substitution} is associated to.
   * @param position The {@link INaviOperandTree operand tree} position in the
   *        {@link INaviInstruction instruction} to which the {@link RawTypeSubstitution type
   *        substitution} is associated to.
   * @param expressionId The id of the {@link INaviOperandTreeNode operand tree node} where the
   *        {@link RawTypeSubstitution type substitution} is associated to.
   */
  RawTypeSubstitution loadTypeSubstitution(INaviModule module, BigInteger address, int position,
      int expressionId) throws CouldntLoadDataException;

  /**
   * Loads all raw type substitutions for the given module from the database.
   *
   * @param module The module for which to load the substitutions.
   * @return The list of all type substitutions for the given module.
   * @throws CouldntLoadDataException Thrown if the raw substitutions couldn't be loaded from the
   *         database.
   */
  List<RawTypeSubstitution> loadTypeSubstitutions(INaviModule module)
      throws CouldntLoadDataException;

  /**
   * Loads all users currently known to the database.
   *
   * @return The List of users in the database.
   */
  List<IUser> loadUsers() throws CouldntLoadDataException;

  /**
   * Loads the graph of a view from the database.
   *
   * @param view The view to load.
   *
   * @return The graph of the view.
   *
   * @throws CouldntLoadDataException Thrown if the graph of view could not be loaded.
   * @throws CPartialLoadException Thrown if the graph could not be loaded because not all required
   *         modules are loaded.
   */
  MutableDirectedGraph<INaviViewNode, INaviEdge> loadView(INaviView view)
      throws CouldntLoadDataException, CPartialLoadException;

  /**
   * Loads the view -> function mapping from the database.
   *
   * @param flowgraphs List of all native flow graph views of a module.
   * @param functions List of all functions of a module.
   * @param module The module from which to load the mapping.
   *
   * @return A view -> function mapping and a function -> view mapping.
   *
   * @throws CouldntLoadDataException Thrown if the mapping could not be loaded.
   */
  ImmutableBiMap<INaviView, INaviFunction> loadViewFunctionMapping(List<IFlowgraphView> flowgraphs,
      List<INaviFunction> functions, CModule module) throws CouldntLoadDataException;

  /**
   * Moves a tag.
   *
   * @param parent The new parent node of the tag.
   * @param child The tag to move.
   * @param type the type of the tag.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be moved.
   */
  void moveTag(ITreeNode<CTag> parent, ITreeNode<CTag> child, TagType type)
      throws CouldntSaveDataException;

  /**
   * Reads a module setting from the database.
   *
   * @param module The module whose setting is read.
   * @param key The name of the setting to read.
   *
   * @return The loaded setting.
   *
   * @throws CouldntLoadDataException Thrown if the setting could not be read.
   */
  String readSetting(CModule module, String key) throws CouldntLoadDataException;

  /**
   * Reads a project setting from the database.
   *
   * @param project The project whose setting is read.
   * @param key The name of the setting to read.
   *
   * @return The loaded setting.
   *
   * @throws CouldntLoadDataException Thrown if the setting could not be read.
   */
  String readSetting(CProject project, String key) throws CouldntLoadDataException;

  /**
   * Removes a debugger from a project.
   *
   * @param project The project from which the debugger is removed.
   * @param debugger The debugger to remove from the project.
   *
   * @throws CouldntSaveDataException Thrown if the debugger could not be removed from the project.
   */
  void removeDebugger(INaviProject project, DebuggerTemplate debugger)
      throws CouldntSaveDataException;

  /**
   * Remove a {@link SQLProviderListener listener} from the list of listeners getting informed about
   * changes in the {@link SQLProvider provider}.
   *
   * @param listener The {@link SQLProviderListener} to remove from the list.
   */
  void removeListener(final SQLProviderListener listener);

  /**
   * Removes a tag from a view.
   *
   * @param view The view from which the tag is removed.
   * @param tag The tag to be removed from the view.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the view.
   */
  void removeTag(INaviView view, CTag tag) throws CouldntSaveDataException;

  /**
   * Removes a tag from a node.
   *
   * @param node The node where the tag is removed.
   * @param tagId The ID of the tag to be removed from the node.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the node.
   */
  void removeTagFromNode(INaviViewNode node, int tagId) throws CouldntSaveDataException;

  /**
   * Forwards a function to another function.
   *
   * @param source The source function that is forwarded.
   * @param target The target function of the forwarding or null if formerly set forwarding should
   *        be removed.
   *
   * @throws CouldntSaveDataException Thrown if the function could not be forwarded.
   */
  void forwardFunction(INaviFunction source, INaviFunction target) throws CouldntSaveDataException;

  /**
   * Saves a trace to the database.
   *
   * @param trace The trace to save to the database.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be saved to the database.
   */
  void save(TraceList trace) throws CouldntSaveDataException;

  /**
   * Saves a view to the database.
   *
   * @param view The view to save to the database.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be saved to the database.
   */
  void save(CView view) throws CouldntSaveDataException;

  /**
   * Saves the data of a module to the database.
   *
   * @param module The module whose data is stored in the database.
   * @param data The data of the module to store in the database.
   *
   * @throws CouldntSaveDataException Thrown if the module data could not be stored.
   */
  void saveData(INaviModule module, byte[] data) throws CouldntSaveDataException;

  /**
   * Stores the settings map of a view to the database.
   *
   * @param view The view whose settings are stored.
   * @param settings The settings map to store to the database.
   *
   * @throws CouldntSaveDataException Thrown if the view settings could not be stored in the
   *         database.
   */
  void saveSettings(CView view, Map<String, String> settings) throws CouldntSaveDataException;

  /**
   * Tags a node with a given tag.
   *
   * @param node The node to be tagged.
   * @param tagId The ID of the tag that is assigned to the node.
   *
   * @throws CouldntSaveDataException Thrown if the node could not be tagged.
   */
  void saveTagToNode(INaviViewNode node, int tagId) throws CouldntSaveDataException;

  /**
   * Changes the description of a tag.
   *
   * @param tag The tag whose description is changed.
   * @param description The new description of the tag.
   *
   * @throws CouldntSaveDataException Thrown if changing the tag description failed.
   */
  void setDescription(CTag tag, String description) throws CouldntSaveDataException;

  /**
   * Changes the description of a trace.
   *
   * @param trace The trace whose description is changed.
   * @param description The new description of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the description of the trace could not be changed.
   */
  void setDescription(TraceList trace, String description) throws CouldntSaveDataException;

  /**
   * Changes the description of a function.
   *
   * @param function The function whose description is changed.
   * @param description The new description of the function.
   *
   * @throws CouldntSaveDataException Thrown if the new description could not be saved to the
   *         database.
   */
  void setDescription(INaviFunction function, String description) throws CouldntSaveDataException;

  /**
   * Changes the description of a module.
   *
   * @param module The module whose description is changed.
   * @param description The new description of the module.
   *
   * @throws CouldntSaveDataException Thrown if the description of the module could not be changed.
   */
  void setDescription(INaviModule module, String description) throws CouldntSaveDataException;

  /**
   * Changes the description of a project.
   *
   * @param project The project whose description is changed.
   * @param description The new description of the project.
   *
   * @throws CouldntSaveDataException Thrown if the description could not be changed.
   */
  void setDescription(INaviProject project, String description) throws CouldntSaveDataException;

  /**
   * Changes the description of the view.
   *
   * @param view The view whose description is changed.
   * @param description The new description of the view.
   *
   * @throws CouldntSaveDataException Thrown if the description of the view could not be changed.
   */
  void setDescription(INaviView view, String description) throws CouldntSaveDataException;

  /**
   * Changes the file base of a module.
   *
   * @param module The module whose file base is changed.
   * @param address The new file base of the module.
   *
   * @throws CouldntSaveDataException Thrown if the file base of the module could not be changed.
   */
  void setFileBase(INaviModule module, IAddress address) throws CouldntSaveDataException;

  /**
   * Changes the replacement string of an operand tree node that represents a global variable.
   *
   * @param operandTreeNode The node whose replacement string is changed.
   * @param replacement The new replacement string.
   *
   * @throws CouldntSaveDataException Thrown if the replacement string could not be updated.
   */
  void setGlobalReplacement(INaviOperandTreeNode operandTreeNode, String replacement)
      throws CouldntSaveDataException;

  /**
   * Changes the host of an existing debugger template.
   *
   * @param debugger The debugger whose host value is changed.
   * @param host The new host value of the debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the host value could not be updated.
   */
  void setHost(DebuggerTemplate debugger, String host) throws CouldntSaveDataException;

  /**
   * Changes the image base of a module.
   *
   * @param module The module whose image base is changed.
   * @param address The new image base of the module.
   *
   * @throws CouldntSaveDataException Thrown if the image base of the module could not be changed.
   */
  void setImageBase(INaviModule module, IAddress address) throws CouldntSaveDataException;

  void setModules(final List<INaviModule> modules);

  /**
   * Changes the name of an existing debugger template.
   *
   * @param debugger The debugger whose name value is changed.
   * @param name The new name value of the debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the name value could not be updated.
   */
  void setName(DebuggerTemplate debugger, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of a tag.
   *
   * @param tag The tag whose name is changed.
   * @param name The new name of the tag.
   *
   * @throws CouldntSaveDataException Thrown if changing the tag name failed.
   */
  void setName(CTag tag, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of a trace.
   *
   * @param trace The trace whose name is changed.
   * @param name The new name of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the name of the trace could not be changed.
   */
  void setName(TraceList trace, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of the function.
   *
   * @param function The function whose name is changed.
   * @param name The new name of the function.
   *
   * @throws CouldntSaveDataException Thrown if storing the new name to the database failed.
   */
  void setName(INaviFunction function, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of a module.
   *
   * @param module The module whose name is changed.
   * @param name The new name of the module.
   *
   * @throws CouldntSaveDataException Thrown if changing the name of the module changed.
   */
  void setName(INaviModule module, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of a project.
   *
   * @param project The project whose name is changed.
   * @param name The new name of the project.
   *
   * @throws CouldntSaveDataException Thrown if the name could not be changed.
   */
  void setName(INaviProject project, String name) throws CouldntSaveDataException;

  /**
   * Changes the name of the view.
   *
   * @param view The view whose description is changed.
   * @param name The new name of the view.
   *
   * @throws CouldntSaveDataException Thrown if the description of the view could not be changed.
   */
  void setName(INaviView view, String name) throws CouldntSaveDataException;

  /**
   * Changes the port of an existing debugger template.
   *
   * @param debugger The debugger whose port value is changed.
   * @param port The new port value of the debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the port value could not be updated.
   */
  void setPort(DebuggerTemplate debugger, int port) throws CouldntSaveDataException;

  /**
   * Changes the replacement string of an operand tree node.
   *
   * @param operandTreeNode The node whose replacement string is changed.
   * @param replacement The new replacement string.
   *
   * @throws CouldntSaveDataException Thrown if the replacement string could not be updated.
   */
  void setReplacement(COperandTreeNode operandTreeNode, String replacement)
      throws CouldntSaveDataException;

  /**
   * Changes the name of a given section.
   *
   * @param moduleId The id of the module that contains the given section.
   * @param sectionId The id of the section whose name should be changed.
   * @param name The new section name.
   * @throws CouldntSaveDataException Thrown if the name could not be written to the database.
   */
  void setSectionName(int moduleId, int sectionId, String name) throws CouldntSaveDataException;

  /**
   * Stars a module.
   *
   * @param module The module to star.
   * @param isStared True, to star the module. False, to unstar it.
   *
   * @throws CouldntSaveDataException Thrown if the the star state of the module could not be
   *         updated.
   */
  void setStared(INaviModule module, boolean isStared) throws CouldntSaveDataException;

  /**
   * Stars a view.
   *
   * @param view The view to star.
   * @param isStared True, to star the view. False, to unstar it.
   *
   * @throws CouldntSaveDataException Thrown if the the star state of the module could not be
   *         updated.
   */
  void setStared(INaviView view, boolean isStared) throws CouldntSaveDataException;

  /**
   * Changes the name of a {@link TypeInstance} in the database to the new name.
   *
   * @param moduleId the id of the {@link INaviModule} the {@link TypeInstance} is associated to.
   * @param id The id of the {@link TypeInstance} where the name will be changed.
   * @param name The new name of the {@link TypeInstance}.
   */
  void setTypeInstanceName(int moduleId, int id, String name) throws CouldntSaveDataException;

  /**
   * Tags a view.
   *
   * @param view The view to tag.
   * @param tag The tag to tag the view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be tagged.
   */
  void tagView(INaviView view, CTag tag) throws CouldntSaveDataException;

  /**
   * Updates a BinNavi database if necessary.
   *
   * @throws CouldntUpdateDatabaseException Thrown if the database could not be updated.
   */
  void updateDatabase() throws CouldntUpdateDatabaseException;

  /**
   * Updates the corresponding database record for the given member. For the arguments newOffset,
   * newNumberOfElements and newArgumentIndex only one can be present.
   *
   * @param member The member to be updated.
   * @param newName The new name for the member.
   * @param newBaseType The new base type of the member.
   * @param newOffset The new offset of the member.
   * @param newNumberOfElements The new number of elements of the member.
   * @param newArgumentIndex The new argument index of the member.
   * @param module The module that contains the given type member.
   * @throws CouldntSaveDataException Thrown if the changed member could not be written to the
   *         database.
   */
  void updateMember(TypeMember member,
      String newName,
      BaseType newBaseType,
      Optional<Integer> newOffset,
      Optional<Integer> newNumberOfElements,
      Optional<Integer> newArgumentIndex,
      INaviModule module) throws CouldntSaveDataException;

  /**
   * Updates the offsets of a list of members in the database.
   *
   * @param updatedMembers The ids of members whose offsets should be changed in the database.
   * @param delta The value that should be added to the existing offsets. Can be positive or
   *        negative, but must be different from zero.
   * @param implicitlyUpdatedMembers The ids of members that are implicitly affected due to
   *        re-arrangements induced by the updated members.
   * @param implicitDelta The delta of the implicitly moved members.
   * @param module The module that contains the given members.
   *
   * @throws CouldntSaveDataException Thrown if the member offsets could not be written to the
   *         database.
   */
  void updateMemberOffsets(List<Integer> updatedMembers, int delta,
      List<Integer> implicitlyUpdatedMembers, int implicitDelta, INaviModule module)
      throws CouldntSaveDataException;

  /**
   * Updates an existing type in the database.
   *
   * @param baseType The base type to be updated.
   * @param name The new name of the base type.
   * @param size The new size of the base type.
   * @param isSigned The new signedness of the base type.
   * @param module The module that contains the given base type.
   * @throws CouldntSaveDataException Thrown if the base type could not be updated.
   */
  void updateType(BaseType baseType, String name, int size, boolean isSigned, INaviModule module)
      throws CouldntSaveDataException;

  /**
   * Updates the given type substitution in the database.
   *
   * @param substitution The type substitution to updated.
   * @param baseType The new base type for the type substitution.
   * @param memberPath The sequence of members in order to resolve unambiguities when dealing with
   *        unions.
   * @param offset The new offset for the type substitution.
   * @param module The module that contains the given type substitution.
   * @throws CouldntSaveDataException Thrown if the type substitution could not be updated in the
   *         database.
   */
  void updateTypeSubstitution(TypeSubstitution substitution, BaseType baseType,
      List<Integer> memberPath, int offset, INaviModule module) throws CouldntSaveDataException;

  /**
   * Writes a module setting to the database.
   *
   * @param module The module whose setting is written.
   * @param key Name of the setting to write.
   * @param value Value of the setting to write.
   *
   * @throws CouldntSaveDataException Thrown if the setting could not be written.
   */
  void writeSetting(CModule module, String key, String value) throws CouldntSaveDataException;

  /**
   * Writes a project setting to the database.
   *
   * @param project The project whose setting is written.
   * @param key Name of the setting to write.
   * @param value Value of the setting to write.
   *
   * @throws CouldntSaveDataException Thrown if the setting could not be written.
   */
  void writeSetting(CProject project, String key, String value) throws CouldntSaveDataException;

  /**
   * Loads multiple comments from the database at once.
   *
   * @param commentIds The IDs of the comments that are loaded.
   * @return A Hashmap mapping from the comment id to the list of comments associated to it.
   *
   * @throws CouldntLoadDataException if the comments could not be loaded from the database.
   */
  public HashMap<Integer, ArrayList<IComment>> loadMultipleCommentsById(
      final Collection<Integer> commentIds) throws CouldntLoadDataException;
}
