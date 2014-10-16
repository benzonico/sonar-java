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
package org.sonar.java.resolve;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.java.ast.visitors.SubscriptionVisitor;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.BreakStatementTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ContinueStatementTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.LabeledStatementTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class ExpressionSubscriptionVisitor extends SubscriptionVisitor {

  private final ExpressionVisitor expressionVisitor;
  private Tree withinExpression;

  public ExpressionSubscriptionVisitor(SemanticModel semanticModel, Symbols symbols, Resolve resolve) {
    this.expressionVisitor = new ExpressionVisitor(semanticModel, symbols, resolve);
  }

  private List<Tree.Kind> expressions = ImmutableList.<Tree.Kind>builder()
      .add(Tree.Kind.POSTFIX_INCREMENT)
      .add(Tree.Kind.POSTFIX_DECREMENT)
      .add(Tree.Kind.PREFIX_INCREMENT)
      .add(Tree.Kind.PREFIX_DECREMENT)
      .add(Tree.Kind.UNARY_PLUS)
      .add(Tree.Kind.UNARY_MINUS)
      .add(Tree.Kind.BITWISE_COMPLEMENT)
      .add(Tree.Kind.LOGICAL_COMPLEMENT)
      .add(Tree.Kind.MULTIPLY)
      .add(Tree.Kind.DIVIDE)
      .add(Tree.Kind.REMAINDER)
      .add(Tree.Kind.PLUS)
      .add(Tree.Kind.MINUS)
      .add(Tree.Kind.LEFT_SHIFT)
      .add(Tree.Kind.RIGHT_SHIFT)
      .add(Tree.Kind.UNSIGNED_RIGHT_SHIFT)
      .add(Tree.Kind.LESS_THAN)
      .add(Tree.Kind.GREATER_THAN)
      .add(Tree.Kind.LESS_THAN_OR_EQUAL_TO)
      .add(Tree.Kind.GREATER_THAN_OR_EQUAL_TO)
      .add(Tree.Kind.EQUAL_TO)
      .add(Tree.Kind.NOT_EQUAL_TO)
      .add(Tree.Kind.AND)
      .add(Tree.Kind.XOR)
      .add(Tree.Kind.OR)
      .add(Tree.Kind.CONDITIONAL_AND)
      .add(Tree.Kind.CONDITIONAL_OR)
      .add(Tree.Kind.CONDITIONAL_EXPRESSION)
      .add(Tree.Kind.ARRAY_ACCESS_EXPRESSION)
      .add(Tree.Kind.MEMBER_SELECT)
      .add(Tree.Kind.NEW_CLASS)
      .add(Tree.Kind.NEW_ARRAY)
      .add(Tree.Kind.METHOD_INVOCATION)
      .add(Tree.Kind.TYPE_CAST)
      .add(Tree.Kind.INSTANCE_OF)
      .add(Tree.Kind.PARENTHESIZED_EXPRESSION)
      .add(Tree.Kind.ASSIGNMENT)
      .add(Tree.Kind.MULTIPLY_ASSIGNMENT)
      .add(Tree.Kind.DIVIDE_ASSIGNMENT)
      .add(Tree.Kind.REMAINDER_ASSIGNMENT)
      .add(Tree.Kind.PLUS_ASSIGNMENT)
      .add(Tree.Kind.MINUS_ASSIGNMENT)
      .add(Tree.Kind.LEFT_SHIFT_ASSIGNMENT)
      .add(Tree.Kind.RIGHT_SHIFT_ASSIGNMENT)
      .add(Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT)
      .add(Tree.Kind.AND_ASSIGNMENT)
      .add(Tree.Kind.XOR_ASSIGNMENT)
      .add(Tree.Kind.OR_ASSIGNMENT)
      .add(Tree.Kind.INT_LITERAL)
      .add(Tree.Kind.LONG_LITERAL)
      .add(Tree.Kind.FLOAT_LITERAL)
      .add(Tree.Kind.DOUBLE_LITERAL)
      .add(Tree.Kind.BOOLEAN_LITERAL)
      .add(Tree.Kind.CHAR_LITERAL)
      .add(Tree.Kind.STRING_LITERAL)
      .add(Tree.Kind.NULL_LITERAL)
      .add(Tree.Kind.IDENTIFIER)
      .add(Tree.Kind.ARRAY_TYPE)
      .add(Tree.Kind.ANNOTATION)
      .add(Tree.Kind.LAMBDA_EXPRESSION)
      .add(Tree.Kind.PRIMITIVE_TYPE)
      .build();

  private List<Tree> excludedLabelIds = Lists.newArrayList();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    ArrayList<Tree.Kind> kinds = Lists.newArrayList(Tree.Kind.values());
    kinds.remove(Tree.Kind.IDENTIFIER);
    kinds.remove(Tree.Kind.MEMBER_SELECT);
    return kinds;
  }

  public void visitExpressions(CompilationUnitTree tree) {
    withinExpression = null;
    scanTree(tree);
  }

  @Override
  public void visitNode(Tree tree) {
    IdentifierTree label = null;
    if (tree.is(Tree.Kind.LABELED_STATEMENT)) {
      label = ((LabeledStatementTree) tree).label();
      tree = ((LabeledStatementTree) tree).statement();
    } else if (tree.is(Tree.Kind.CONTINUE_STATEMENT)) {
      label = ((ContinueStatementTree) tree).label();
    } else if (tree.is(Tree.Kind.BREAK_STATEMENT)) {
      label = ((BreakStatementTree) tree).label();
    }
    if (label != null) {
      excludedLabelIds.add(label);
    }

    if (withinExpression == null && expressions.contains(((JavaTree) tree).getKind()) && !excludedLabelIds.contains(tree)) {
      withinExpression = tree;
      tree.accept(expressionVisitor);
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (withinExpression == tree) {
      withinExpression = null;
    }
  }
}
