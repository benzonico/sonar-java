/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.sonar.plugins.java.api.tree.IdentifierTree;

import java.util.Arrays;
import java.util.Map.Entry;

/**
 * Predefined symbols.
 */
public class Symbols {

  static final JavaSymbol.PackageJavaSymbol rootPackage;
  static final JavaSymbol.PackageJavaSymbol defaultPackage;

  /**
   * Owns all predefined symbols (builtin types, operators).
   */
  static final JavaSymbol.TypeJavaSymbol predefClass;

  /**
   * Type, which can't be modelled for the moment.
   */
  static final JavaType.ClassJavaType unknownType;
  public static final JavaSymbol.TypeJavaSymbol unknownSymbol;

  final JavaSymbol.TypeJavaSymbol arrayClass;

  static final JavaSymbol.TypeJavaSymbol methodClass;
  static final JavaSymbol.TypeJavaSymbol noSymbol;

  // builtin types
  static final JavaType byteType;
  static final JavaType charType;
  static final JavaType shortType;
  static final JavaType intType;
  static final JavaType longType;
  static final JavaType floatType;
  static final JavaType doubleType;
  static final JavaType booleanType;
  static final JavaType nullType;
  static final JavaType voidType;

  final BiMap<JavaType, JavaType> boxedTypes;

  // predefined types

  /**
   * {@link java.lang.Object}
   */
  final JavaType objectType;

  final JavaType cloneableType;
  final JavaType serializableType;
  final JavaType classType;
  final JavaType stringType;

  /**
   * {@link java.lang.annotation.Annotation}
   */
  final JavaType annotationType;

  /**
   * {@link java.lang.Enum}
   */
  final JavaType enumType;

  static {
    rootPackage = new JavaSymbol.PackageJavaSymbol("", null);
    unknownSymbol = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, "!unknownSymbol!", rootPackage) {
      @Override
      public void addUsage(IdentifierTree tree) {
        // noop
      }

      @Override
      public boolean isTypeSymbol() {
        return false;
      }

      @Override
      public boolean isUnknown() {
        return true;
      }
    };
    unknownSymbol.members = new Scope(unknownSymbol) {
      @Override
      public void enter(JavaSymbol symbol) {
        // noop
      }

    };
    unknownType = new JavaType.ClassJavaType(unknownSymbol) {
      @Override
      public String toString() {
        return "!unknown!";
      }
    };
    unknownType.tag = JavaType.UNKNOWN;
    unknownType.supertype = null;
    unknownType.interfaces = ImmutableList.of();
    unknownSymbol.type = unknownType;
    // TODO should have type "noType":
    noSymbol = new JavaSymbol.TypeJavaSymbol(0, "", rootPackage);

    byteType = initType(JavaType.BYTE, "byte");
    charType = initType(JavaType.CHAR, "char");
    shortType = initType(JavaType.SHORT, "short");
    intType = initType(JavaType.INT, "int");
    longType = initType(JavaType.LONG, "long");
    floatType = initType(JavaType.FLOAT, "float");
    doubleType = initType(JavaType.DOUBLE, "double");
    booleanType = initType(JavaType.BOOLEAN, "boolean");
    nullType = initType(JavaType.BOT, "<nulltype>");
    voidType = initType(JavaType.VOID, "void");
    predefClass = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, "", rootPackage);
    methodClass = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, "", noSymbol);

    predefClass.members = new Scope(predefClass);
    ((JavaType.ClassJavaType) predefClass.type).interfaces = ImmutableList.of();

    // builtin types
    predefClass.members.enter(byteType.symbol);
    predefClass.members.enter(charType.symbol);
    predefClass.members.enter(shortType.symbol);
    predefClass.members.enter(intType.symbol);
    predefClass.members.enter(longType.symbol);
    predefClass.members.enter(floatType.symbol);
    predefClass.members.enter(doubleType.symbol);
    predefClass.members.enter(booleanType.symbol);
    predefClass.members.enter(nullType.symbol);
    predefClass.members.enter(voidType.symbol);
    defaultPackage = new JavaSymbol.PackageJavaSymbol("", rootPackage);
  }

  private static boolean operatorsCreated = false;

  public Symbols(BytecodeCompleter bytecodeCompleter) {
    bytecodeCompleter.init(this);

    // predefined types for java lang
    JavaSymbol.PackageJavaSymbol javalang = bytecodeCompleter.enterPackage("java.lang");
    // define a star import scope to let resolve types to java.lang when needed.
    javalang.members = new Scope.StarImportScope(javalang, bytecodeCompleter);
    javalang.members.enter(javalang);

    objectType = bytecodeCompleter.loadClass("java.lang.Object").type;
    classType = bytecodeCompleter.loadClass("java.lang.Class").type;
    stringType = bytecodeCompleter.loadClass("java.lang.String").type;
    cloneableType = bytecodeCompleter.loadClass("java.lang.Cloneable").type;
    serializableType = bytecodeCompleter.loadClass("java.io.Serializable").type;
    annotationType = bytecodeCompleter.loadClass("java.lang.annotation.Annotation").type;
    enumType = bytecodeCompleter.loadClass("java.lang.Enum").type;

    // Associate boxed types
    boxedTypes = HashBiMap.create();
    boxedTypes.put(byteType, bytecodeCompleter.loadClass("java.lang.Byte").type);
    boxedTypes.put(charType, bytecodeCompleter.loadClass("java.lang.Character").type);
    boxedTypes.put(shortType, bytecodeCompleter.loadClass("java.lang.Short").type);
    boxedTypes.put(intType, bytecodeCompleter.loadClass("java.lang.Integer").type);
    boxedTypes.put(longType, bytecodeCompleter.loadClass("java.lang.Long").type);
    boxedTypes.put(floatType, bytecodeCompleter.loadClass("java.lang.Float").type);
    boxedTypes.put(doubleType, bytecodeCompleter.loadClass("java.lang.Double").type);
    boxedTypes.put(booleanType, bytecodeCompleter.loadClass("java.lang.Boolean").type);

    for (Entry<JavaType, JavaType> entry : boxedTypes.entrySet()) {
      entry.getKey().primitiveWrapperType = entry.getValue();
      entry.getValue().primitiveType = entry.getKey();
    }

    // TODO comment me
    arrayClass = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, "Array", noSymbol);
    JavaType.ClassJavaType arrayClassType = (JavaType.ClassJavaType) arrayClass.type;
    arrayClassType.supertype = objectType;
    arrayClassType.interfaces = ImmutableList.of(cloneableType, serializableType);
    arrayClass.members = new Scope(arrayClass);
    arrayClass.members().enter(new JavaSymbol.VariableJavaSymbol(Flags.PUBLIC | Flags.FINAL, "length", intType, arrayClass));
    // TODO arrayClass implements clone() method

    enterOperators();
  }

  /**
   * Registers builtin types as symbols, so that they can be found as an usual identifiers.
   */
  private static JavaType initType(int tag, String name) {
    JavaSymbol.TypeJavaSymbol symbol = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, name, rootPackage);
    symbol.members = new Scope(symbol);
    ((JavaType.ClassJavaType) symbol.type).interfaces = ImmutableList.of();
    symbol.type.tag = tag;
    return symbol.type;
  }

  /**
   * Registers operators as methods, so that they can be found as an usual methods.
   */
  private void enterOperators() {
    if(operatorsCreated) {
      return;
    }
    operatorsCreated = true;
    for (String op : new String[] {"+", "-", "*", "/", "%"}) {
      for (JavaType type : Arrays.asList(doubleType, floatType, longType, intType)) {
        enterBinop(op, type, type, type);
      }
    }
    for (String op : new String[] {"&", "|", "^"}) {
      for (JavaType type : Arrays.asList(booleanType, longType, intType)) {
        enterBinop(op, type, type, type);
      }
    }
    for (String op : new String[] {"<<", ">>", ">>>"}) {
      enterBinop(op, longType, longType, longType);
      enterBinop(op, intType, longType, intType);
      enterBinop(op, longType, intType, longType);
      enterBinop(op, intType, intType, intType);
    }
    for (String op : new String[] {"<", ">", ">=", "<="}) {
      for (JavaType type : Arrays.asList(doubleType, floatType, longType, intType)) {
        enterBinop(op, type, type, booleanType);
      }
    }
    for (String op : new String[] {"==", "!="}) {
      for (JavaType type : Arrays.asList(objectType, booleanType, doubleType, floatType, longType, intType)) {
        enterBinop(op, type, type, booleanType);
      }
    }
    enterBinop("&&", booleanType, booleanType, booleanType);
    enterBinop("||", booleanType, booleanType, booleanType);

    // string concatenation
    for (JavaType type : Arrays.asList(nullType, objectType, booleanType, doubleType, floatType, longType, intType)) {
      enterBinop("+", stringType, type, stringType);
      enterBinop("+", type, stringType, stringType);
    }
    enterBinop("+", stringType, stringType, stringType);
  }

  private static void enterBinop(String name, JavaType left, JavaType right, JavaType result) {
    JavaType type = new JavaType.MethodJavaType(ImmutableList.of(left, right), result, ImmutableList.<JavaType>of(), methodClass);
    JavaSymbol symbol = new JavaSymbol.MethodJavaSymbol(Flags.PUBLIC | Flags.STATIC, name, type, predefClass);
    predefClass.members.enter(symbol);
  }

  public JavaType getPrimitiveFromDescriptor(char descriptor) {
    switch (descriptor) {
      case 'S':
        return shortType;
      case 'I':
        return intType;
      case 'C':
        return charType;
      case 'Z':
        return booleanType;
      case 'B':
        return byteType;
      case 'J':
        return longType;
      case 'F':
        return floatType;
      case 'D':
        return doubleType;
      case 'V':
        return voidType;
      default:
        throw new IllegalStateException("Descriptor '" + descriptor + "' cannot be mapped to a primitive type");
    }
  }
}
