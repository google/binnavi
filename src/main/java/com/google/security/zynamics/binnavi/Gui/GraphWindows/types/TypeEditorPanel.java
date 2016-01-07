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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;

/**
 * The panel which holds the type editor tree control that provides an editing mechanism for the
 * displayed set of types.
 */
public class TypeEditorPanel extends JPanel implements TypeEditor {
  private final JPanel contentPanel = new JPanel();
  private final JFrame owner;
  private final TypesTree typesTree;

  private TypeEditorPanel(final JFrame owner, final TypesTree treeControl,
      final TypeManager typeManager, final String caption) {
    this.owner = owner;
    this.typesTree = treeControl;
    createControls(typeManager, caption);
  }

  /**
   * Creates a type editor panel instance that display all types held by the type manager.
   *
   * @param owner The GUI owner of the type editor panel.
   * @param typeManager The type manager that holds the current type system.
   * @return A type editor panel instance.
   */
  public static TypeEditorPanel CreateDefaultTypeEditor(final JFrame owner,
      final TypeManager typeManager) {
    return new TypeEditorPanel(owner, TypesTree.createDefaultDndTypesTree(typeManager), typeManager,
        "Type editor");
  }

  /**
   * Creates a type editor panel instance that only holds the stack frame of the given function and
   * ignores all other types.
   *
   * @param owner The GUI owner of the type editor panel.
   * @param typeManager The type manager that holds the current type system.
   * @param function The function whose stack frame should be displayed.
   * @return A type editor panel instance.
   */
  public static TypeEditorPanel CreateStackFrameEditor(final JFrame owner,
      final TypeManager typeManager, final INaviFunction function) {
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    return new TypeEditorPanel(owner, TypesTree.createStackFrameDndTypesTree(function, typeManager),
        typeManager, "Stack frame");
  }

  /**
   * Creates a type editor panel instance that only holds the prototype of the given function and
   * ignores all other types.
   *
   * @param parent The GUI parent of the type editor panel.
   * @param typeManager The {@link TypeManager type manager} that holds the current type system.
   * @param function The {@link INaviFunction function} whose prototype should be displayed.
   * @return A type editor panel instance.
   */
  public static TypeEditorPanel CreatePrototypeEditor(JFrame parent, TypeManager typeManager,
      INaviFunction function) {
    Preconditions.checkNotNull(typeManager, "Error: typeManager argument can not be null");
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    return new TypeEditorPanel(parent, TypesTree.createPrototypeDndTypesTree(function, typeManager),
        typeManager, "Prototype");
  }

  /**
   * Deletes both, selected types and members.
   */
  private class DeleteTypeOrMemberAction extends AbstractAction {

    private final DeleteTypeAction typeAction;
    private final DeleteMemberAction memberAction;

    public DeleteTypeOrMemberAction(final JFrame owner, final TypeManager typeManager,
        final TypeEditor typeEditor) {
      typeAction = new DeleteTypeAction(owner, typeManager, typeEditor);
      memberAction = new DeleteMemberAction(owner, typeManager, typeEditor);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      memberAction.actionPerformed(event);
      typeAction.actionPerformed(event);
    }
  }

  private void createControls(final TypeManager typeManager, final String caption) {
    setBounds(100, 100, 588, 529);
    setLayout(new BorderLayout());
    setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), caption));
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(new TypeEditorSearchPanel(typesTree), BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));
    {
      final JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, BorderLayout.CENTER);
      {
        typesTree.addMouseListener(new TypeEditorMouseHandler(owner, typesTree, typeManager, this));
        typesTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true),
            "delete_types_or_members");
        typesTree.getActionMap().put("delete_types_or_members",
            new DeleteTypeOrMemberAction(owner, typeManager, this));
        scrollPane.setViewportView(typesTree);
      }
    }
  }

  @Override
  public TypeMember getSelectedMember() {
    final TreePath path = typesTree.getSelectionPath();
    if (path != null && path.getLastPathComponent() instanceof TypeMemberTreeNode) {
      return ((TypeMemberTreeNode) path.getLastPathComponent()).getTypeMember();
    } else {
      return null;
    }
  }

  @Override
  public ImmutableList<TypeMember> getSelectedMembers() {
    final Builder<TypeMember> builder = ImmutableList.<TypeMember>builder();
    final TreePath[] paths = typesTree.getSelectionPaths();
    if (paths != null) {
      for (final TreePath path : typesTree.getSelectionPaths()) {
        final Object node = path.getLastPathComponent();
        if (node instanceof TypeMemberTreeNode) {
          builder.add(((TypeMemberTreeNode) node).getTypeMember());
        }
      }
    }
    return builder.build();
  }

  @Override
  public BaseType getSelectedType() {
    final TreePath path = typesTree.getSelectionPath();
    if (path != null && path.getLastPathComponent() instanceof BaseTypeTreeNode) {
      return ((BaseTypeTreeNode) path.getLastPathComponent()).getBaseType();
    } else {
      return null;
    }
  }

  @Override
  public ImmutableList<BaseType> getSelectedTypes() {
    final Builder<BaseType> builder = ImmutableList.<BaseType>builder();
    final TreePath[] paths = typesTree.getSelectionPaths();
    if (paths != null) {
      for (final TreePath path : typesTree.getSelectionPaths()) {
        final Object node = path.getLastPathComponent();
        if (node instanceof BaseTypeTreeNode) {
          builder.add(((BaseTypeTreeNode) node).getBaseType());
        }
      }
    }
    return builder.build();
  }
}
