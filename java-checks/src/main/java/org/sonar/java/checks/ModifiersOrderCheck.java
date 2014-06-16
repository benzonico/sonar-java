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
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.ast.api.JavaKeyword;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;

@Rule(
    key = "ModifiersOrderCheck",
    priority = Priority.MINOR,
    tags = {"convention"})
@BelongsToProfile(title = "Sonar way", priority = Priority.MINOR)
public class ModifiersOrderCheck extends SquidCheck<LexerlessGrammar> {

  private static final AstNodeType[] EXPECTED_ORDER = new AstNodeType[]{
      JavaGrammar.ANNOTATION,
      JavaKeyword.PUBLIC,
      JavaKeyword.PROTECTED,
      JavaKeyword.PRIVATE,
      JavaKeyword.ABSTRACT,
      JavaKeyword.STATIC,
      JavaKeyword.FINAL,
      JavaKeyword.TRANSIENT,
      JavaKeyword.VOLATILE,
      JavaKeyword.SYNCHRONIZED,
      JavaKeyword.NATIVE,
      JavaKeyword.STRICTFP
  };

  @Override
  public void init() {
    subscribeTo(JavaGrammar.MODIFIER);
  }

  @Override
  public void visitNode(AstNode node) {
    if (isFirstModifer(node)) {
      List<AstNode> modifiers = getModifiers(node);
      AstNode badlyOrderedNode = firstBadlyOrderedNode(modifiers);
      if (badlyOrderedNode != null) {
        getContext().createLineViolation(this, "Reorder the modifiers to comply with the Java Language Specification.", badlyOrderedNode);
      }
    }
  }

  private static boolean isFirstModifer(AstNode node) {
    return node.getPreviousSibling() == null;
  }

  private static List<AstNode> getModifiers(AstNode node) {
    ImmutableList.Builder<AstNode> builder = ImmutableList.builder();
    builder.add(node);

    for (AstNode nextSibling = node.getNextSibling(); nextSibling != null && nextSibling.is(JavaGrammar.MODIFIER); nextSibling = nextSibling.getNextSibling()) {
      builder.add(nextSibling);
    }

    return builder.build();
  }

  private static AstNode firstBadlyOrderedNode(List<AstNode> modifiers) {
    int expected = 0;
    int index = 0;
    AstNode modifier = null;
    while (index < modifiers.size() && expected < EXPECTED_ORDER.length) {
      modifier = modifiers.get(index);
      System.out.println(index+" "+expected+" --  "+modifier);
      if (modifier.getFirstChild().is(EXPECTED_ORDER[expected])) {
        index++;
        modifier = null;
      } else {
        expected++;
      }
    }
    if (modifier == null) {
      return null;
    } else {
      return modifiers.get(index - expected>EXPECTED_ORDER.length ? 0 : 1);
    }
  }

}
