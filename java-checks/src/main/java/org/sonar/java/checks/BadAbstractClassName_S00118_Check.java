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
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.regex.Pattern;

@Rule(
    key = BadAbstractClassName_S00118_Check.RULE_KEY,
    priority = Priority.MAJOR,
    tags = {"convention"})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("20min")
public class BadAbstractClassName_S00118_Check extends BaseTreeVisitor implements JavaFileScanner {

  public static final String RULE_KEY = "S00118";
  private final RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, RULE_KEY);

  private static final String DEFAULT_FORMAT = "^Abstract[A-Z][a-zA-Z0-9]*$";

  @RuleProperty(
      key = "format",
      defaultValue = "" + DEFAULT_FORMAT)
  public String format = DEFAULT_FORMAT;

  private Pattern pattern = null;
  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    if (pattern == null) {
      pattern = Pattern.compile(format, Pattern.DOTALL);
    }
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitClass(ClassTree tree) {
    if (tree.is(Tree.Kind.CLASS) && tree.simpleName() != null) {
      if (pattern.matcher(tree.simpleName().name()).matches()) {
        if (!isAbstract(tree)) {
          context.addIssue(tree, ruleKey, "Make this class abstract or rename it, since it matches the regular expression '" + format + "'.");
        }
      } else {
        if (isAbstract(tree)) {
          context.addIssue(tree, ruleKey, "Rename this abstract class name to match the regular expression '" + format + "'.");
        }
      }
    }
    super.visitClass(tree);
  }

  private boolean isAbstract(ClassTree tree) {
    for (Modifier modifier : tree.modifiers().modifiers()) {
      if (modifier == Modifier.ABSTRACT) {
        return true;
      }
    }
    return false;
  }

}
