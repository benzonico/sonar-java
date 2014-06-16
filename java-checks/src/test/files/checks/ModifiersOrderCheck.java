class Foo {
  public static void main(String[] args) {  // Compliant
  }

  static public void main(String[] args) {  // Non-Compliant
  }

  public int a;

  public
    final// Non-Compliant
      static void  main(String[] args) {}
@Nullable @Deprecated final static method(){}
@Nullable
  public
    static
        @Deprecated
          final
          method(){}
}
