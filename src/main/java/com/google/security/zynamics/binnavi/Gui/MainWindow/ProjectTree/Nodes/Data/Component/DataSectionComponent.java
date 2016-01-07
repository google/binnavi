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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions.OpenInLastWindowAndZoomToAddressAction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceAddress;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplayCoordinate;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplayEventListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataChangedListener;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataProvider;
import com.google.security.zynamics.zylib.gui.JHexPanel.IMenuCreator;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * Provides a viewer for the sections list of a single module. This component contains a hex view to
 * display the raw bytes as well as a table which contains the corresponding type instances.
 */
public class DataSectionComponent extends JPanel {

  private static final int SELECTED_INSTANCE_HIGHLIGHT_LEVEL = 4;
  private static final int DEFAULT_INSTANCE_HIGHLIGHT_LEVEL = 5;

  // We need to declare these members as volatile since assignments can occur from an arbitrary
  // thread during module load.
  private volatile HexViewDataAdapter hexViewData;
  private volatile Section currentSection;
  private volatile SectionComboBox sections;
  private final JHexView hexView = new JHexView();
  private final INaviModule module;
  private JCheckBox virtualAddresses;
  private final IViewContainer container;
  private TypeInstanceAddressTableCellRenderer addressRenderer =
      new TypeInstanceAddressTableCellRenderer();

  private final IViewContainerListener internalViewContainerListener =
      new InternalViewContainerListener();

  private final TypeInstanceTableDatamodel typeDataModel;
  private final CodeDisplay typeDisplay;

  public DataSectionComponent(final INaviModule module, final IViewContainer container) {
    // TODO(jannewger): there should probably be a "data provider" argument to
    // the constructor: if we use this component while debugging, we need a
    // different implementation than if we are viewing static section content!
    // For now we only deal with static section data via HexViewDataAdapter.
    super(new BorderLayout(5, 5));
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
    this.container = Preconditions.checkNotNull(container, "Error: type argument can not be null");
    this.container.addListener(internalViewContainerListener);

    hexView.setDefinitionStatus(DefinitionStatus.DEFINED);
    hexView.setEnabled(true);
    hexView.setMenuCreator(new TypeInstanceMenu());
    // Note: We need to account for the fact that this component is created
    // prior to the module being loaded. Thus, we need to fill the models of the
    // hexview and table component later in moduleLoaded().

    typeDataModel = new TypeInstanceTableDatamodel();
    typeDisplay = new CodeDisplay(typeDataModel);
    // Register the JCodeDisplay with the typeDataModel so that it can be refreshed when the
    // underlying data changes ansynchronously.
    typeDataModel.registerCodeDisplayToUpdate(typeDisplay);

    // Register a custom Mouselistener for the typeDisplays so that a popup can be displayed.
    typeDisplay.addMouseListener(new TypeInstanceTableMouseListener());
    typeDisplay.addCaretChangedListener(new InstancesTableSelectionListener());
    add(typeDisplay, BorderLayout.CENTER);

    final JPanel panel = createOptionsPanel();
    add(panel, BorderLayout.NORTH);
    add(hexView, BorderLayout.WEST);

    addressRenderer = new TypeInstanceAddressTableCellRenderer();
  }

  /**
   * This function initializes the highlighting for a {@link JHexView hex view} given the
   * {@link TypeInstance type instances} of a {@link Section section}.
   *
   * @param hexView The {@link JHexView} in which the highlighting will be set.
   * @param useVirtualAddresses Determines whether to the hex view should use virtual addresses or
   *        offsets.
   * @param instances The {@link TypeInstance type instances} for which to set the highlighting.
   */
  private static void initializeHighlighting(final JHexView hexView,
      final boolean useVirtualAddresses, final Collection<TypeInstance> instances) {
    hexView.uncolorizeAll(DEFAULT_INSTANCE_HIGHLIGHT_LEVEL);
    for (final TypeInstance instance : instances) {
      hexView.colorize(DEFAULT_INSTANCE_HIGHLIGHT_LEVEL,
          useVirtualAddresses ? instance.getAddress().getVirtualAddress()
              : instance.getAddress().getOffset(), instance.getBaseType().getByteSize(),
          Color.BLACK, Color.YELLOW);
    }
  }

  private final JPanel createOptionsPanel() {
    final JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    optionsPanel.setBorder(new TitledBorder("Options"));
    optionsPanel.add(createVirtualAddressCheckBox());
    optionsPanel.add(createSectionSelectionPanel());
    return optionsPanel;
  }

  private final JPanel createSectionSelectionPanel() {
    final JPanel section_panel = new JPanel();
    final JLabel lblSection = new JLabel("Section:");
    section_panel.add(lblSection);
    sections = new SectionComboBox();
    section_panel.add(sections);
    sections.addActionListener(new SectionSelectedListener());
    return section_panel;
  }

  private final JCheckBox createVirtualAddressCheckBox() {
    virtualAddresses = new JCheckBox("Show Virtual Addresses");
    virtualAddresses.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (virtualAddresses.isSelected()) {
          hexView.setBaseAddress(currentSection.getStartAddress().toLong());
          addressRenderer.showVirtualAddress(true);
        } else {
          hexView.setBaseAddress(0);
          addressRenderer.showVirtualAddress(false);
        }
        initializeHighlighting(hexView, virtualAddresses.isSelected(),
            module.getContent().getTypeInstanceContainer().getTypeInstances(currentSection));
      }
    });
    return virtualAddresses;
  }

  private void highlightType(final int row) {
    final TypeInstance instance = typeDataModel.getTypeAtRow(row);
    hexView.uncolorizeAll(SELECTED_INSTANCE_HIGHLIGHT_LEVEL);
    hexView.colorize(SELECTED_INSTANCE_HIGHLIGHT_LEVEL,
        instance.getAddress().getOffset() + hexView.getBaseAddress(),
        instance.getBaseType().getByteSize(), Color.GREEN, Color.BLACK);
    hexView.gotoOffset(instance.getAddress().getOffset() + hexView.getBaseAddress());
  }

  /**
   * Once the module is loaded, we can fill the component models.
   *
   *  Note that this method may be invoked from an arbitrary thread during module loading. Thus, the
   * members that are assigned in this method are declared as volatile.
   */
  private void moduleLoaded() {
    final List<Section> sectionList = module.getContent().getSections().getSections();
    if (sectionList.isEmpty()) {
      return;
    }
    currentSection = sectionList.get(0);
    hexViewData = new HexViewDataAdapter(currentSection);
    hexView.setData(hexViewData);
    final Collection<TypeInstance> instances =
        module.getContent().getTypeInstanceContainer().getTypeInstances(currentSection);
    initializeHighlighting(hexView, virtualAddresses.isSelected(), instances);
    sections.setSections(sectionList);

    typeDataModel.setTypeInstanceContainer(module.getContent().getTypeInstanceContainer());
    typeDataModel.setSection(currentSection);
  }

  private void sectionChanged(final Section section) {
    // Note: no listener concept here because a) there is exactly one event, and b) there are only
    // two consumers. Will probably change once we have debugger support!
    currentSection = section;
    hexViewData.setActiveSection(section);
    typeDataModel.setSection(section);
    initializeHighlighting(hexView, virtualAddresses.isSelected(),
        module.getContent().getTypeInstanceContainer().getTypeInstances(section));
  }

  public JHexView getHexView() {
    return hexView;
  }

  /**
   * Scrolls the instance table to the given instance and selects the corresponding section in the
   * combobox.
   *
   * @param instance The instance to scroll to.
   */
  public void scrollToInstance(final TypeInstance instance) {
    // TODO(thomasdullien): Implement this again.
  }

  /**
   * Selects the given section and scrolls the hex view component to the given address.
   *
   * @param section The section to select.
   * @param address The address to scroll to.
   */
  public void scrollToSectionAddress(final Section section, final long address) {
    sections.setSelectedItem(section);
    hexView.gotoOffset(address - section.getStartAddress().toLong());
  }

  /**
   * Uses a section instance to obtain the corresponding data and provides it to the hex view
   * component.
   */
  private class HexViewDataAdapter implements IDataProvider {
    private final ListenerProvider<IDataChangedListener> listeners =
        new ListenerProvider<IDataChangedListener>();
    private Section activeSection;

    public HexViewDataAdapter(final Section currentSection) {
      this.activeSection = currentSection;
    }

    @Override
    public void addListener(final IDataChangedListener hexView) {
      listeners.addListener(hexView);
    }

    @Override
    public byte[] getData() {
      return activeSection.getData();
    }

    @Override
    public byte[] getData(final long offset, final int length) {
      return Arrays.copyOfRange(getData(), (int) (offset - hexView.getBaseAddress()),
          (int) (offset - hexView.getBaseAddress() + length));
    }

    @Override
    public int getDataLength() {
      return activeSection.getRawSize();
    }

    @Override
    public boolean hasData(final long start, final int length) {
      return (start - hexView.getBaseAddress() + length) <= getDataLength();
    }

    @Override
    public boolean isEditable() {
      return false;
    }

    @Override
    public boolean keepTrying() {
      return true;
    }

    @Override
    public void removeListener(final IDataChangedListener listener) {
      listeners.removeListener(listener);
    }

    public void setActiveSection(final Section section) {
      activeSection = section;
      for (final IDataChangedListener listener : listeners) {
        listener.dataChanged();
      }
    }

    @Override
    public void setData(final long offset, final byte[] data) {}
  }

  /**
   * Listens on selection changed events in the type instance table and forwards them to the data
   * section component.
   */
  private class InstancesTableSelectionListener implements CodeDisplayEventListener {
    @Override
    public void caretChanged(final CodeDisplayCoordinate caret) {
      highlightType(caret.getRow());
    }
  }

  private class InternalViewContainerListener implements IViewContainerListener {

    @Override
    public void addedView(final IViewContainer container, final INaviView view) {}

    @Override
    public void closedContainer(final IViewContainer container, final List<INaviView> views) {}

    @Override
    public void deletedView(final IViewContainer container, final INaviView view) {}

    @Override
    public void loaded(final IViewContainer container) {
      if (container.getModules().contains(module)) {
        moduleLoaded();
      }
    }
  }

  /**
   * Listens for section selection events and forwards them to the data section component.
   */
  private class SectionSelectedListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      final SectionComboBox comboBox = (SectionComboBox) e.getSource();
      final Section section = (Section) comboBox.getSelectedItem();
      sectionChanged(section);
    }
  }

  /**
   * Creates a context menu for the hex view component in order to create a new type instance.
   */
  private class TypeInstanceMenu implements IMenuCreator {

    @Override
    public JPopupMenu createMenu(final long offset) {
      final JPopupMenu popupMenu = new JPopupMenu();
      final TypeInstanceContainer instanceContainer =
          module.getContent().getTypeInstanceContainer();
      final TypeInstance existingInstance = instanceContainer.getTypeInstance(
          new TypeInstanceAddress(currentSection.getStartAddress(), offset));
      if (existingInstance != null) {
        popupMenu.add(new EditTypeInstanceAction(
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, DataSectionComponent.this),
            module.getTypeManager(), existingInstance, instanceContainer));
      } else {
        popupMenu.add(new CreateTypeInstanceAction(
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, DataSectionComponent.this),
            instanceContainer, module.getTypeManager(), currentSection, offset));
      }
      popupMenu.addSeparator();
      popupMenu.add(HexViewOptionsMenu.createHexViewOptionsMenu(hexView));
      return popupMenu;
    }
  }

  /**
   * Creates a context menu for the type instance table in order to create, edit or delete
   * {@link TypeInstance} objects.
   */
  private class TypeInstanceTableMouseListener extends MouseAdapter {

    private void handleClick(final MouseEvent event) {
      final int row = typeDisplay.rowAtPoint(event.getPoint());
      if (event.isPopupTrigger()) {
        showPopupMenu(row, event);
      }
    }

    private void showPopupMenu(final int rowIndex, final MouseEvent event) {
      final JPopupMenu popupMenu = new JPopupMenu();
      final JFrame owner =
          (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, DataSectionComponent.this);
      final TypeInstanceContainer instanceContainer =
          module.getContent().getTypeInstanceContainer();
      final TypeManager typeManager = module.getTypeManager();
      popupMenu.add(
          new CreateTypeInstanceAction(owner, instanceContainer, typeManager, currentSection));
      if (rowIndex != -1) {
        final TypeInstance existingInstance = typeDataModel.getTypeAtRow(rowIndex);
        popupMenu.add(
            new EditTypeInstanceAction(owner, typeManager, existingInstance, instanceContainer));
        popupMenu.add(new DeleteTypeInstanceAction(owner, existingInstance, instanceContainer));
      }
      popupMenu.show(event.getComponent(), event.getX(), event.getY());
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
      if (event.getClickCount() == 2 && (event.getButton() == MouseEvent.BUTTON1) && (
          typeDisplay.columnAtPoint(event.getPoint()) == TypeInstanceTableDatamodel.XREFS_INDEX)) {
        final int row = typeDisplay.rowAtPoint(event.getPoint());
        final int line = typeDisplay.lineAtPoint(event.getPoint());
        final TypeInstanceReference reference = typeDataModel.getTypeInstanceReference(row, line);
        if (reference == null) {
          return;
        }
        final JFrame owner =
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, DataSectionComponent.this);
        final javax.swing.Action actionProxy =
            CActionProxy.proxy(new OpenInLastWindowAndZoomToAddressAction(owner, container,
                new INaviView[] {reference.getView()}, reference));
        actionProxy.actionPerformed(null);
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      handleClick(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      handleClick(event);
    }
  }
}
