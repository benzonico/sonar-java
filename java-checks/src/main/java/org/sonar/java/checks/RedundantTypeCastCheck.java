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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.model.AbstractTypedTree;
import org.sonar.java.resolve.Type;
import org.sonar.java.resolve.Types;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeCastTree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;
import java.util.Set;

@Rule(
    key = "S1905",
    priority = Priority.MINOR,
    tags = {})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class RedundantTypeCastCheck extends SubscriptionBaseVisitor {

  private Set<Tree> excluded = Sets.newHashSet();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.TYPE_CAST, Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(Tree.Kind.METHOD_INVOCATION)) {
      MethodInvocationTree mit = (MethodInvocationTree) tree;
      for (ExpressionTree arg : mit.arguments()) {
        if (arg.is(Tree.Kind.TYPE_CAST)) {
          excluded.add(arg);
        }
      }
    } else if (!excluded.contains(tree)) {
      TypeCastTree typeCastTree = (TypeCastTree) tree;
      Type cast = ((AbstractTypedTree) typeCastTree.type()).getSymbolType();
      Type expressionType = ((AbstractTypedTree) typeCastTree.expression()).getSymbolType();
      Types types = new Types();
      if (isRedundantNumericalCast(cast, expressionType) || isRedundantCast(cast, expressionType, types)) {
        addIssue(tree, "Remove this unnecessary cast to \"" + cast + "\".");
      }
    }
  }

  private boolean isRedundantCast(Type cast, Type expressionType, Types types) {
    return !cast.isNumerical() && !cast.isParametrized() && types.isSubtype(expressionType, cast);
  }

  private boolean isRedundantNumericalCast(Type cast, Type expressionType) {
    return cast.isNumerical() && cast == expressionType;
  }
}
