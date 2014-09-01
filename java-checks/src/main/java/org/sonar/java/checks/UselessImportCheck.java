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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Rule(
    key = "UselessImportCheck",
    priority = Priority.MINOR,
    tags = {"unused"})
@BelongsToProfile(title = "Sonar way", priority = Priority.MINOR)
public class UselessImportCheck extends SubscriptionBaseVisitor {

  @Override
  public List<Kind> nodesToVisit() {
    return Lists.newArrayList();
  }

  @Override
  public void scanFile(JavaFileScannerContext context) {
    super.scanFile(context);
    SemanticModel semanticModel = (SemanticModel) context.getSemanticModel();
    CompilationUnitTree cut = context.getTree();
    String packageName = "";
    if (cut.packageName() != null) {
      packageName = concatenate(cut.packageName());
    }
    System.out.println(packageName);
    for (ImportTree importTree : cut.imports()) {
      if(importTree.qualifiedIdentifier().is(Kind.MEMBER_SELECT)) {
        MemberSelectExpressionTree mset = (MemberSelectExpressionTree) importTree.qualifiedIdentifier();
        if(semanticModel.getSymbol(importTree)!=null){

        System.out.println(semanticModel.getSymbol(importTree).getName() + semanticModel.getUsages(semanticModel.getSymbol(importTree)).size());
        }
        System.out.println(concatenate(mset));
      } else {
        IdentifierTree identifierTree = (IdentifierTree) importTree.qualifiedIdentifier();
        System.out.println(identifierTree.name());
      }
    }
  }


  private String concatenate(ExpressionTree tree) {
    Deque<String> pieces = new LinkedList<String>();

    ExpressionTree expr = tree;
    while (expr.is(Tree.Kind.MEMBER_SELECT)) {
      MemberSelectExpressionTree mse = (MemberSelectExpressionTree) expr;
      pieces.push(mse.identifier().name());
      pieces.push(".");
      expr = mse.expression();
    }
    if (expr.is(Tree.Kind.IDENTIFIER)) {
      IdentifierTree idt = (IdentifierTree) expr;
      pieces.push(idt.name());
    }

    StringBuilder sb = new StringBuilder();
    for (String piece : pieces) {
      sb.append(piece);
    }
    return sb.toString();
  }

}
