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
package org.sonar.java.ast.parser.grammar.expressions;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.ast.AstXmlPrinter;
import org.junit.Test;
import org.sonar.java.JavaSquid;
import org.sonar.java.ast.parser.JavaGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.parser.ParserAdapter;

import java.nio.charset.Charset;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class ExpressionTest {

  private LexerlessGrammar g = JavaGrammar.createGrammar();

  /**
   * Our grammar accepts such constructions, whereas should not.
   */
  @Test
  public void error() {
    assertThat(g.rule(JavaGrammar.EXPRESSION))
        .matches("a = b + 1 = c + 2");
  }

  @Test
  public void realLife() {
    assertThat(g.rule(JavaGrammar.EXPRESSION))
        .matches("b >> 4")
        .matches("b >>= 4")
        .matches("b >>> 4")
        .matches("b >>>= 4")

            // method call
        .matches("SomeClass.<T>method(arguments)")
        .matches("this.<T>method(arguments)")
        .matches("super.<T>method(arguments)")
        .matches("oc.new innerClass<String>()")
            // constructor call
        .matches("<T>this(arguments)")
        .matches("<T>super(arguments)")
            // Java 7: diamond
        .matches("new HashMap<>()")

            //Java 8 : constructors with annotation types
        .matches("new int @Foo [12]")
        .matches("new int[12] @Foo [13] @Foo @Bar []")

        .matches("new @Foo innerClass(\"literal\")")
        .matches("new OuterClass.@Foo innerClass(\"literal\")")
            //Java 8 : Method references
        .matches("System.out::println")
        .matches("int[]::new")
        .matches("List::new")
        .matches("List<String>::size")
        .matches("List::size")
        .matches("int[]::clone")
        .matches("T::size")
        .matches("Arrays::<String>sort")
        .matches("(foo?list.map(String::length):Collections.emptyList()) :: iterator")

            //Java 8 : Lambda expressions
        .matches("()->12")
        .matches("()->{}")
        .matches("a->a*a")
        .matches("(int a)->a*a")
        .matches("(a)->a*a")

            //Java 8 : Cast expression with bounds
        .matches("(Comparator<Map.Entry<K, V>> & Serializable) foo")
        .matches("(Callable[] & Serializable) foo")
        .matches("(Callable<Integer[]>[] & Serializable) foo")
        .matches("(Comparator<Map.Entry<K, V>>[] & Serializable) foo")
    ;


  }

}
