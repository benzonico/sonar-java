class A{
  int a;
  int[] b;
  A(int a) {
    a = a;
    this.a = this.a;
    this.a = a;
    b[0] = b[0]; //false negative
  }
}