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
package org.sonar.java.ast.parser;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.apache.commons.lang.StringUtils;
import org.sonar.java.model.AbstractTypedTree;
import org.sonar.java.model.JavaTree;
import org.sonar.java.model.JavaTreeMaker;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.java.resolve.Symbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.Tree;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrinterVisitor extends BaseTreeVisitor {

  private static final int INDENT_SPACES = 2;

  private final StringBuilder sb;
  private final SemanticModel semanticModel;
  private final Map<IdentifierTree, Symbol> idents = new HashMap<IdentifierTree, Symbol>();
  private int indentLevel;

  public PrinterVisitor() {
    semanticModel = null;
    sb = new StringBuilder();
    indentLevel = 0;
  }

  public PrinterVisitor(SemanticModel semanticModel) {
    sb = new StringBuilder();
    indentLevel = 0;
    this.semanticModel =semanticModel;
  }

  public static String print(Tree tree) {
   return print(tree, null);
  }
  public static String print(Tree tree, @Nullable SemanticModel semanticModel) {
    PrinterVisitor pv = new PrinterVisitor(semanticModel);
    pv.scan(tree);
    return pv.sb.toString();
  }

  public static String print(List<? extends Tree> trees) {
    StringBuilder result = new StringBuilder();
    for (Tree tree : trees) {
      result.append(print(tree));
    }
    return result.toString();
  }

  private StringBuilder indent() {
    return sb.append(StringUtils.leftPad("", INDENT_SPACES * indentLevel));
  }

  @Override
  protected void scan(List<? extends Tree> trees) {
    if (!trees.isEmpty()) {
      sb.deleteCharAt(sb.length() - 1);
      sb.append(" : [\n");
      super.scan(trees);
      indent().append("]\n");
    }
  }

  @Override
  protected void scan(@Nullable Tree tree) {
    if (tree != null) {
      Symbol sym = null;
      try {
        Method getSymbol = null;
        for (Method method : tree.getClass().getMethods()) {
          if (method.getName().equals("getSymbol")) {
            getSymbol = tree.getClass().getMethod("getSymbol");
          }
        }
        if (getSymbol != null) {
          sym = (Symbol) getSymbol.invoke(tree);
        }
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }

      Tree.Kind kind = ((JavaTree) tree).getKind();
      String nodeName = ((JavaTree) tree).getClass().getSimpleName();
      if (kind != null) {
        nodeName = kind.getAssociatedInterface().getSimpleName();
      }
      indent().append(nodeName);
      int line = -1;
      AstNode node = ((JavaTree) tree).getAstNode();
      if (node != null && node.hasToken()) {
        line = node.getTokenLine();
        sb.append(" ").append(line);
      }
      if(idents.get(tree) != null) {
        Preconditions.checkState(sym==null);
        sym = idents.get(tree);
      }
      if(tree instanceof AbstractTypedTree) {
        sb.append(" ").append(((AbstractTypedTree) tree).getType2());
      }

      if (sym != null) {
        //No forward reference possible... Need another visitor to build this info ?
        for (IdentifierTree identifierTree : semanticModel.getUsages(sym)) {
          idents.put(identifierTree, sym);
        }
        sb.append(" ").append(sym.getName());
        int refLine = ((JavaTree)semanticModel.getTree(sym)).getTokenLine();
        if(refLine!=line) {
          sb.append(" ref#").append(refLine);
        }
      }
      sb.append("\n");
    }
    indentLevel++;
    super.scan(tree);
    indentLevel--;
  }

  public static void main(String[] args) {
    final Parser p = JavaParser.createParser(Charsets.UTF_8, true);
    final JavaTreeMaker maker = new JavaTreeMaker();
    CompilationUnitTree cut = maker.compilationUnit(p.parse(new File("/home/benzonico/Development/SonarSource/tmp/a.java")));
    SemanticModel semanticModel = SemanticModel.createFor(cut, Lists.newArrayList(new File("/home/benzonico/Development/SonarSource/tmp/A.class")));
    String print = PrinterVisitor.print(cut, semanticModel);
    System.out.println(print);
  }

}
