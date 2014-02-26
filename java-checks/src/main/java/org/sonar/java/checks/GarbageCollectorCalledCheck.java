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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1215",
  priority = Priority.CRITICAL)
@BelongsToProfile(title = "Sonar way", priority = Priority.CRITICAL)
public class GarbageCollectorCalledCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(JavaGrammar.PRIMARY);
    subscribeTo(JavaGrammar.UNARY_EXPRESSION);
    subscribeTo(JavaGrammar.UNARY_EXPRESSION_NOT_PLUS_MINUS);
  }

  @Override
  public void visitNode(AstNode node) {
    if (isGarbageCollectorCall(node)) {
      getContext().createLineViolation(this, "Don't try to be smarter than the JVM, remove this call to run the garbage collector.", node);
    }
  }

  private static boolean isGarbageCollectorCall(AstNode node) {
    return AstNodeTokensMatcher.matches(node, "System.gc()") ||
      AstNodeTokensMatcher.matches(node, "Runtime.getRuntime().gc()");
  }

}
