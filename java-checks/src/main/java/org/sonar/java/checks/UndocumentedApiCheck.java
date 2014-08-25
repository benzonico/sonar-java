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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.ast.visitors.AccessorVisitorST;
import org.sonar.java.ast.visitors.PublicApiChecker;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Rule(key = "UndocumentedApi", priority = Priority.MAJOR,
    tags = {"convention"})
public class UndocumentedApiCheck extends SubscriptionBaseVisitor {

  private static final String DEFAULT_FOR_CLASSES = "**";

  @RuleProperty(
      key = "forClasses",
      defaultValue = DEFAULT_FOR_CLASSES)
  public String forClasses = DEFAULT_FOR_CLASSES;

  private WildcardPattern[] patterns;
  private Deque<ClassTree> classTrees = Lists.newLinkedList();
  private AccessorVisitorST accessorVisitorST;
  private PublicApiChecker publicApiChecker;
  private String packageName;
  private Pattern setterPattern = Pattern.compile("set[A-Z].*");
  private Pattern getterPattern = Pattern.compile("(get|is)[A-Z].*");

  @Override
  public List<Tree.Kind> nodesToVisit() {
    List<Tree.Kind> kinds = Lists.newArrayList(PublicApiChecker.API_KINDS);
    kinds.add(Tree.Kind.COMPILATION_UNIT);
    return kinds;
  }

  @Override
  public void scanFile(JavaFileScannerContext context) {
    classTrees.clear();
    accessorVisitorST = new AccessorVisitorST();
    publicApiChecker = new PublicApiChecker();
    packageName = "";
    super.scanFile(context);
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(PublicApiChecker.CLASS_KINDS)) {
      classTrees.push((ClassTree) tree);
    } else if (tree.is(Tree.Kind.COMPILATION_UNIT)) {
      CompilationUnitTree cut = (CompilationUnitTree) tree;
      if (cut.packageName() != null) {
        packageName = concatenate(cut.packageName());
      }
    }

    if (!isExcluded(tree)) {
      String javadoc = publicApiChecker.getApiJavadoc(tree);

      if (javadoc == null) {
        addIssue(tree, "Document this public " + getType(tree) + ".");
      } else {
        List<String> undocumentedParameters = getUndocumentedParameters(javadoc, getParameters(tree));
        if (!undocumentedParameters.isEmpty()) {
          addIssue(tree, "Document the parameter(s): " + Joiner.on(", ").join(undocumentedParameters));
        }
        if (hasNonVoidReturnType(tree) && !hasReturnJavadoc(javadoc)) {
          addIssue(tree, "Document this method return value.");
        }
      }
    }
  }

  private String getType(Tree tree) {
    String result = "";
    if (tree.is(Tree.Kind.CLASS)) {
      result = "class";
    } else if (tree.is(Tree.Kind.INTERFACE)) {
      result = "interface";
    } else if (tree.is(Tree.Kind.ENUM)) {
      result = "enum";
    } else if (tree.is(Tree.Kind.ANNOTATION_TYPE)) {
      result = "annotation";
    } else if (tree.is(Tree.Kind.CONSTRUCTOR)) {
      result = "constructor";
    } else if (tree.is(Tree.Kind.METHOD)) {
      result = "method";
    } else if (tree.is(Tree.Kind.VARIABLE)) {
      result = "field";
    }
    return result;
  }


  @Override
  public void leaveNode(Tree tree) {
    if (tree.is(PublicApiChecker.CLASS_KINDS)) {
      classTrees.pop();
    }
  }

  private boolean isExcluded(Tree tree) {
    return isAccessor(tree) || !isPublicApi(tree) || !isMatchingPattern();
  }

  private boolean isAccessor(Tree tree) {
    if (!classTrees.isEmpty() && !classTrees.peek().is(Tree.Kind.INTERFACE) && tree.is(Tree.Kind.METHOD)) {
      MethodTree methodTree = (MethodTree) tree;
      String name = methodTree.simpleName().name();
      return (setterPattern.matcher(name).matches() && methodTree.parameters().size() == 1) ||
          (getterPattern.matcher(name).matches() && methodTree.parameters().isEmpty());
    }
    //return tree.is(Tree.Kind.METHOD) && accessorVisitorST.isAccessor(classTrees.peek(), (MethodTree) tree);
    return false;
  }

  private boolean isPublicApi(Tree tree) {
    return publicApiChecker.isPublicApi(classTrees.peek(), tree);
  }

  private boolean isMatchingPattern() {
    return WildcardPattern.match(getPatterns(), className());
  }

  private String className() {
    String className = packageName;
    IdentifierTree identifierTree = classTrees.peek().simpleName();
    if (identifierTree != null) {
      className += "/" + identifierTree.name();
    }
    return className;
  }

  private WildcardPattern[] getPatterns() {
    if (patterns == null) {
      patterns = PatternUtils.createPatterns(forClasses);
    }
    return patterns;
  }

  private List<String> getUndocumentedParameters(String javadoc, List<String> parameters) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();

    for (String parameter : parameters) {
      if (!hasParamJavadoc(javadoc, parameter)) {
        builder.add(parameter);
      }
    }

    return builder.build();
  }

  private List<String> getParameters(Tree tree) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    if (tree.is(PublicApiChecker.METHOD_KINDS)) {
      MethodTree methodTree = (MethodTree) tree;
      for (VariableTree variableTree : methodTree.parameters()) {
        builder.add(variableTree.simpleName().name());
      }
      for (Tree typeParam : methodTree.typeParameters()) {
        //FIXME : type param is not implemented.
        builder.add("<" + ((JavaTree) typeParam).getToken() + ">");
      }
    }
    return builder.build();
  }

  private static boolean hasParamJavadoc(String comment, String parameter) {
    return comment.matches("(?s).*@param\\s++" + parameter + ".*");
  }

  private boolean hasNonVoidReturnType(Tree tree) {
    if (tree.is(Tree.Kind.METHOD)) {
      Tree returnType = ((MethodTree) tree).returnType();
      return returnType == null || !(returnType.is(Tree.Kind.PRIMITIVE_TYPE) && "void".equals(((PrimitiveTypeTree) returnType).keyword().text()));
    }
    return false;
  }

  private boolean hasReturnJavadoc(String comment) {
    return comment.contains("@return");
  }

  private String concatenate(ExpressionTree tree) {
    Deque<String> pieces = new LinkedList<String>();

    ExpressionTree expr = tree;
    while (expr.is(Tree.Kind.MEMBER_SELECT)) {
      MemberSelectExpressionTree mse = (MemberSelectExpressionTree) expr;
      pieces.push(mse.identifier().name());
      pieces.push("/");
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
