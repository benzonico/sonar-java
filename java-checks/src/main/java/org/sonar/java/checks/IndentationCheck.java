/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks;

import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.List;

@Rule(
    key = "IndentationCheck",
    priority = Priority.MAJOR,
    tags = {"convention"})
public class IndentationCheck extends SubscriptionBaseVisitor {

  private static final Kind[] BLOCK_TYPES = new Kind[]{
      Kind.CLASS,
      Kind.INTERFACE,
      Kind.ENUM,
      Kind.CLASS,
      Kind.BLOCK,
      Kind.SWITCH_STATEMENT,
      Kind.CASE_GROUP
  };

  private static final int DEFAULT_INDENTATION_LEVEL = 2;

  @RuleProperty(
      key = "indentationLevel",
      defaultValue = "" + DEFAULT_INDENTATION_LEVEL)
  public int indentationLevel = DEFAULT_INDENTATION_LEVEL;

  private int expectedLevel;
  private boolean isBlockAlreadyReported;
  private int lastCheckedLine;

  @Override
  public List<Kind> nodesToVisit() {
    return Lists.newArrayList(BLOCK_TYPES);
  }

  @Override
  public void scanFile(JavaFileScannerContext context) {
    expectedLevel = 0;
    isBlockAlreadyReported = false;
    lastCheckedLine = 0;
    super.scanFile(context);
  }

  @Override
  public void visitNode(Tree tree) {
    expectedLevel += indentationLevel;
    isBlockAlreadyReported = false;

    if (tree.is(Kind.CASE_GROUP)) {
      List<CaseLabelTree> labels = ((CaseGroupTree) tree).labels();
      if (labels.size() >= 2) {
        lastCheckedLine = ((JavaTree) labels.get(labels.size() - 2)).getAstNode().getLastToken().getLine();
      }
    }

    if(tree.is(Kind.CLASS, Kind.ENUM, Kind.INTERFACE)) {
      checkIndentation(((ClassTree) tree).members());
    }
    if(tree.is(Kind.CASE_GROUP)) {
      checkIndentation(((CaseGroupTree) tree).body());
    }
    if(tree.is(Kind.BLOCK)) {
      checkIndentation(((BlockTree) tree).body());
    }
  }

  private void checkIndentation(List<? extends Tree> trees) {
    for (Tree tree : trees) {
      if (((JavaTree)tree).getToken().getColumn() != expectedLevel && !isExcluded(tree)) {
        addIssue(tree, "Make this line start at column " + (expectedLevel + 1) + ".");
        isBlockAlreadyReported = true;
      }
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    expectedLevel -= indentationLevel;
    isBlockAlreadyReported = false;
    lastCheckedLine = ((JavaTree)tree).getLastToken().getLine();
  }

  private boolean isExcluded(Tree node) {
    return isBlockAlreadyReported || !isLineFirstStatement((JavaTree) node);// || isInAnnonymousClass(node);
  }

  private boolean isLineFirstStatement(JavaTree javaTree) {
    return lastCheckedLine != javaTree.getTokenLine();
  }

  private static boolean isInAnnonymousClass(AstNode node) {
    return node.hasAncestor(JavaGrammar.CLASS_CREATOR_REST);
  }


}
