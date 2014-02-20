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
package org.sonar.java.ast.parser.grammar.parameters;

import org.junit.Test;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class FormalParameterDeclsRestTest {

  private LexerlessGrammar g = JavaGrammar.createGrammar();

  @Test
  public void ok() {
    g.rule(JavaGrammar.VARIABLE_DECLARATOR_ID).mock();
    g.rule(JavaGrammar.FORMAL_PARAMETER_DECLS).mock();
    g.rule(JavaGrammar.ANNOTATION).mock();

    assertThat(g.rule(JavaGrammar.FORMAL_PARAMETERS_DECLS_REST))
        .matches("variableDeclaratorId")
        .matches("variableDeclaratorId , formalParameterDecls")
        .matches("... variableDeclaratorId")
        .matches("annotation ... variableDeclaratorId");
  }

  @Test
  public void realLife() {
    assertThat(g.rule(JavaGrammar.FORMAL_PARAMETERS_DECLS_REST))
        .matches("@Foo ... variableDeclaratorId");
  }
}
