class A {
  private void f(Throwable e) {
    e.printStackTrace(); // Non-Compliant
    e.printStackTrace(System.out); // Non-Compliant
    e.getMessage(); // Compliant
    a.b.c.d.printStackTrace(); // Non-Compliant
    e.printStackTrace[0]; // Compliant
  }
  void fun(MyException e) {
    e.printStackTrace(); //Non-Compliant
  }
  void fun(CustomException e) {
    e.printStackTrace(); //Compliant
  }

  static class CustomException {
    void printStackTrace(Throwable e){

    }
  }
  static class MyException extends Throwable {
    @Override
    void printStackTrace(){

    }
  }
}
