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

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.java.test.ImportsResolutionCases.ImportInnerClass;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ImportResolutionTest {


  private static Result result;
  //test inner imports
  private ImportInnerClass importInnerClass;

  @BeforeClass
  public static void setUp() throws Exception {
    result = Result.createFor("ImportResolution");

  }

  @Test
  public void extends_should_point_to_correct_symbol() {
    assertThat(result.symbol("Class2").kind == Symbol.TYP);
    Symbol.TypeSymbol class1 = (Symbol.TypeSymbol) result.symbol("Class1");
    Symbol.TypeSymbol class2 = (Symbol.TypeSymbol) result.symbol("Class2");
    assertThat(class2.getSuperclass().symbol).isEqualTo(class1);
    assertThat(class1.getSuperclass()).isNotNull();
    assertThat(class1.getSuperclass().symbol.name).isEqualTo("Collection");
    Symbol.TypeSymbol interface1 = (Symbol.TypeSymbol) result.symbol("Interface1");
    assertThat(interface1.getInterfaces()).isNotEmpty();
    assertThat(interface1.getInterfaces().get(0).symbol.name).isEqualTo("List");
  }

  @Test
  public void import_on_inner_type_should_be_resolved() throws Exception {
    assertThat(result.symbol("annotationTree").type.symbol.name).isEqualTo("AnnotationTreeImpl");
    assertThat(result.symbol("annotationTree").type.symbol.owner().name).isEqualTo("JavaTree");
  }

  @Test
  public void import_static_var_should_be_resolved() throws Exception {
    Symbol http_ok = result.symbol("HTTP_OK");
    assertThat(http_ok.owner().name).isEqualTo("HttpURLConnection");
    assertThat(http_ok.owner().type.symbol.name).isEqualTo("HttpURLConnection");
    assertThat(http_ok.kind).isEqualTo(Symbol.VAR);
    assertThat(http_ok.type.tag).isEqualTo(Type.INT);
  }

  @Test
  public void import_static_method_should_be_resolved() throws Exception {
    Symbol reverse = result.symbol("reverse");
    assertThat(reverse.owner().name).isEqualTo("Collections");
    assertThat(reverse.owner().type.symbol.name).isEqualTo("Collections");
    assertThat(reverse.kind).isEqualTo(Symbol.MTH);
  }

  @Test
  public void import_static_method_should_be_resolved_when_refering_to_multiple_symbols() throws Exception {
    Symbol sort = result.symbol("sort");
    assertThat(sort.owner().name).isEqualTo("Collections");
    assertThat(sort.owner().type.symbol.name).isEqualTo("Collections");
    assertThat(sort.kind).isEqualTo(Symbol.MTH);
    assertThat(sort.type.tag).isEqualTo(Type.METHOD);
    assertThat(result.reference(45, 7).name).isEqualTo("sort");
    assertThat(result.reference(45, 7)).isEqualTo(sort);
    assertThat(result.reference(46, 7).name).isEqualTo("sort");
    assertThat(result.reference(46, 7)).isEqualTo(sort);
  }

  @Test
  public void package_should_be_resolved() {
    assertThat(result.symbol("sym")).isNotNull();
    assertThat(result.symbol("sym").kind).isEqualTo(Symbol.PCK);
    assertThat(result.symbol("sym").owner().kind).isEqualTo(Symbol.PCK);
    //default package name is empty
    assertThat(result.symbol("sym").owner().name).isEmpty();
  }

  @Test
  public void types_from_same_package_should_be_resolved() {
    Result result1 = Result.createForJavaFile("src/test/java/org/sonar/java/resolve/BytecodeCompleterTest");
    Symbol.TypeSymbol thisTest = (Symbol.TypeSymbol) result1.symbol("BytecodeCompleterTest");
    List<Symbol> symbols = thisTest.members().lookup("bytecodeCompleterPackageVisibility");
    assertThat(symbols).hasSize(1);
    Symbol.VariableSymbol symbol = (Symbol.VariableSymbol) symbols.get(0);
    assertThat(symbol.type.symbol.name).isEqualTo("BytecodeCompleterPackageVisibility");
    assertThat(symbol.type.symbol.owner().name).isEqualTo(thisTest.owner().name);
  }

  @Test
  public void star_imports_should_be_resolved() {
    Symbol sort = result.symbol("file");
    assertThat(sort.type.symbol.name).isEqualTo("File");
    assertThat(sort.type.symbol.owner().name).isEqualTo("java.io");
  }

  @Test
  public void star_imports_on_type_should_be_resolved() {
    Result result1 = Result.createForJavaFile("src/test/java/org/sonar/java/resolve/ImportResolutionTest");
    Symbol importInnerClassSymbol = result1.symbol("importInnerClass");
    assertThat(importInnerClassSymbol.type.symbol.name).isEqualTo("ImportInnerClass");
    assertThat(importInnerClassSymbol.type.symbol.owner().name).isEqualTo("ImportsResolutionCases");
    assertThat(importInnerClassSymbol.type.symbol.owner().owner().name).isEqualTo("org.sonar.java.test");
  }

  @Test
  public void import_static_on_demand_should_be_resolved() throws Exception {
    Symbol http_accepted = result.reference(41, 10);
    assertThat(http_accepted.name).isEqualTo("HTTP_ACCEPTED");
    assertThat(http_accepted.owner().name).isEqualTo("HttpURLConnection");
    assertThat(http_accepted.owner().type.symbol.name).isEqualTo("HttpURLConnection");
    assertThat(http_accepted.kind).isEqualTo(Symbol.VAR);
    assertThat(http_accepted.type.tag).isEqualTo(Type.INT);
  }

  @Test
  public void imports_from_java_lang() {
    Symbol iterable = result.symbol("iterable");
    assertThat(iterable.type.symbol.name).isEqualTo("Iterable");
    assertThat(iterable.type.symbol.owner().name).isEqualTo("java.lang");
  }


  @Test
  public void only_one_symbol_per_class_should_be_created() throws Exception {
    Result result1 = Result.createForJavaFile("src/test/java/org/sonar/java/resolve/BytecodeCompleterTest");
    Symbol.TypeSymbol thisTest = (Symbol.TypeSymbol) result1.symbol("BytecodeCompleterTest");
    List<Symbol> symbols = thisTest.members().lookup("bytecodeCompleterPackageVisibility");
    assertThat(symbols).hasSize(1);
    Symbol.TypeSymbol symbol = ((Symbol.VariableSymbol) symbols.get(0)).type.symbol;
    symbols = symbol.members().lookup("bytecodeCompleterTest");
    assertThat(symbols).hasSize(1);
    Symbol.TypeSymbol test = ((Symbol.VariableSymbol) symbols.get(0)).type.symbol;
    assertThat(test).isEqualTo(thisTest);
  }

}
