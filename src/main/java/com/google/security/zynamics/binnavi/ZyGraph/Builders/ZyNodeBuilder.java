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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CGlobalEdgeCommentWrapper;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CGlobalNodeCommentWrapper;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CLocalEdgeCommentWrapper;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CLocalNodeCommentWrapper;
import com.google.security.zynamics.binnavi.config.ColorsConfigItem;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

/**
 * Builder class that is used to build parts of label contents that are shared between different
 * node types.
 */
public final class ZyNodeBuilder {
  /**
   * Default italic font for comments.
   */
  private static final Font ITALIC_FONT = new Font(GuiHelper.getMonospaceFont(), Font.ITALIC, 12);

  /**
   * Default italic+bold font for tags.
   */
  private static final Font ITALIC_BOLD_FONT = new Font(GuiHelper.getMonospaceFont(), Font.ITALIC
      | Font.BOLD, 12);

  /**
   * You are not supposed to instantiate this class.
   */
  private ZyNodeBuilder() {
  }

  /**
   * Adds the local and global comment strings to the label content.
   * 
   * @param content The label content the comment strings are added to.
   * @param node The clicked raw node.
   * @param localComment The local comment string that is added to the label content.
   * @param globalComment The global comment string that is added to the label content.
   */
  private static void addCommentLines(final ZyLabelContent content, final INaviViewNode node,
      final List<IComment> localComment, final List<IComment> globalComment) {
    Preconditions.checkNotNull(content, "IE01530: Content argument can't be null");

    final ColorsConfigItem colorSettings = ConfigManager.instance().getColorSettings();
    if (localComment != null) {
      addCommentLines(content, localComment, colorSettings.getLocalCommentColor(),
          new CLocalNodeCommentWrapper(node));
    }

    if (globalComment != null) {
      addCommentLines(content, globalComment, colorSettings.getGlobalCommentColor(),
          new CGlobalNodeCommentWrapper(node));
    }
  }

  /**
   * Creates line content objects for a single comment string. If necessary, the string comment is
   * broken into several lines.
   * 
   * @param content The label content where the line content objects are added.
   * @param comments The comment string to add to the label content.
   * @param color The color used for the comment line.
   * @param object The object to associate with the comment lines.
   */
  private static void addCommentLines(final ZyLabelContent content, final List<IComment> comments,
      final Color color, final Object object) {
    Preconditions.checkNotNull(content, "IE01180: Content argument can't be null");
    Preconditions.checkNotNull(comments, "IE01181: Comment argument can not be null");
    Preconditions.checkNotNull(color, "IE01183: Color argument can not be null");
    Preconditions.checkNotNull(object, "IE01191: Object argument can not be null");

    for (final IComment comment : comments) {
      final CommentContainer currentCommentContainer = new CommentContainer(comment);

      for (final String commentString : currentCommentContainer.getCommentingString()) {
        final ZyLineContent lineContent = new ZyLineContent(commentString, ITALIC_FONT, null);

        lineContent.setTextColor(Color.BLACK);
        lineContent.setObject(0, commentString.length(), object);

        if (commentString.equals(currentCommentContainer.getCommentingString().get(0))) {
          lineContent.setTextColor(0, currentCommentContainer.getCommentUserNameLength(),
              currentCommentContainer.getCommentColor());
        }
        content.addLineContent(lineContent);
      }
    }
  }

  /**
   * Adds the name of a tag to the content of a label.
   * 
   * @param content The label content where the line content object is added.
   * @param node The node that provides the tags.
   * @param prefix The prefix that is written in front of all tag lines.
   * @param color The color used for the tag line.
   */
  private static void addTagLines(final ZyLabelContent content, final INaviViewNode node,
      final String prefix, final Color color) {
    Preconditions.checkNotNull(content, "IE00918: Content argument can't be null");
    Preconditions.checkNotNull(node, "IE00919: Node argument can't be null");
    Preconditions.checkNotNull(color, "IE00920: Color argument can't be null");

    final Iterator<CTag> it = node.getTagsIterator();
    while (it.hasNext()) {
      final CTag tag = it.next();
      if (!"".equals(tag.getName())) {
        final ZyLineContent lineComment = new ZyLineContent(prefix + tag.getName(), null);

        lineComment.setTextColor(color);
        lineComment.setFont(ITALIC_BOLD_FONT);

        content.addLineContent(lineComment);
      }
    }
  }

  /**
   * Adds the local and global comment strings to the label content.
   * 
   * @param content The label content the comment strings are added to.
   * @param edge The clicked edge.
   * @param localComment The local comment string that is added to the label content.
   * @param globalComment The global comment string that is added to the label content.
   */
  public static void addCommentLines(final ZyLabelContent content, final INaviEdge edge,
      final List<IComment> localComment, final List<IComment> globalComment) {
    Preconditions.checkNotNull(content, "IE00914: Content argument can't be null");

    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();
    if (localComment != null) {
      addCommentLines(content, localComment, colors.getLocalCommentColor(),
          new CLocalEdgeCommentWrapper(edge));
    }

    if (globalComment != null) {
      addCommentLines(content, globalComment, colors.getGlobalCommentColor(),
          new CGlobalEdgeCommentWrapper(edge));
    }
  }

  /**
   * Adds the comment and tag lines to the label content that displays a view node.
   * 
   * @param content The label content the comment strings are added to.
   * @param node The node that provides the raw data.
   * @param localComment The local comment string that is added to the label content.
   * @param globalComment The global comment string that is added to the label content.
   */
  public static void addCommentTagLines(final ZyLabelContent content, final INaviViewNode node,
      final List<IComment> localComment, final List<IComment> globalComment) {
    // Defer argument checking to the called functions
    addCommentLines(content, node, localComment, globalComment);
    addTagLines(content, node, "|T| ", ConfigManager.instance().getColorSettings().getTagColor());
  }
}
