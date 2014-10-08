class A {
  int field;
}

class B { //Noncompliant
}
class C {
  I i = new I() {};
}
interface I {}
@interface annotation {}
enum E {}