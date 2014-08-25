import java.lang.Override;

//All identifier ending with public are expected to be public API per publicApiVisitorST
class A {

  //Constructors
  public A(){}
  A(int param){}
}

/**
 * Documented Class.
 */
public class documentedClassPublic {
  //constructors

  //fields
  int var1;
  /**
   * Documented variable.
   */
  public int documentedVarPublic;
  //Not documentation
  public static int var2Public;
  public final int var3Public;
  public static final int var3;

  //methods
  void method(){}

  /**
   * Documented Method.
   */
  public void documentedMethodInClassPublic(){}
  public static void method2Public(){}

  /**
   * Constructor documented.
   * @param param param
   */
  public documentedClassPublic(int param){}
}
public class undocumentedClassPublic {

}

enum B{}

/**
 * Documented Enum.
 */
public enum documentedEnumPublic{
  A;
}
public enum undocumentedEnumPublic{
  A;
}

interface interfaze{
  String[] undocumentedMethodPublic();

  /**
   * Documented method in interface.
   */
  String[][] documentedDbleArrayMethodPublic();

  @Override
  String method();
}
/**
 * Documented Class.
 */
public interface documentedInterfacePublic {
  void methodPublic();

  /**
   * Documentation.
   */
  java.lang.String documentedMethodPublic();

  /**
   * Documented.
   * @return a map.
   */
  Map<String,String> documentedGetPublic();

  /**
   * Documented method.
   */
  java.util.Map<String,String>[] documentedGetPublic();
}
public interface undocumentedInterfacePublic {

}

@interface annot{}
/**
 * Documented Annotation.
 */
public @interface documentedAnnotationPublic {
  String fooPublic();
}
public @interface undocumentedAnnotationPublic {

}

/**
 * Documented Class.
 */
@MyAnnotation()
public @TypeAnnot class documentedClassWithAnnotationPublic {

}