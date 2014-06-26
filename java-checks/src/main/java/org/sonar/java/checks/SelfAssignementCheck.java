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

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

@Rule(
    key = SelfAssignementCheck.RULE_KEY,
    priority = Priority.MAJOR)
public class SelfAssignementCheck extends BaseTreeVisitor implements JavaFileScanner {

  public static final String RULE_KEY = "S1656";
  private final RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, RULE_KEY);

  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (isAssignementWithTwoIdentifiers(tree) && identifiersAreEquals(tree)) {
      context.addIssue(tree, ruleKey, "Remove or correct this useless self-assignment");
    }
    super.visitAssignmentExpression(tree);
  }

  private boolean isAssignementWithTwoIdentifiers(AssignmentExpressionTree tree) {
    return tree.is(Kind.ASSIGNMENT) && tree.variable().is(Kind.IDENTIFIER) && tree.expression().is(Kind.IDENTIFIER);
  }

  private boolean identifiersAreEquals(AssignmentExpressionTree tree) {
    return ((IdentifierTree) tree.variable()).name().equals(((IdentifierTree) tree.expression()).name());
  }
}
