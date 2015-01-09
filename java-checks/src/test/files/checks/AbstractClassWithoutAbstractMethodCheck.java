public abstract class Animal {

  abstract void move();
  abstract void feed();

}

public abstract class AbstractColor {
  private int red = 0;
  private int green = 0;
  private int blue = 0;

  public int getRed()
  {
    return red;
  }
}

public interface AnimalInterface {

  void move();
  void feed();

}

public class Color {
  private int red = 0;
  private int green = 0;
  private int blue = 0;

  private Color ()
  {}

  public int getRed()
  {
    return red;
  }
}

public abstract class Lamp {

  private boolean switchLamp=false;

  public abstract void glow();

  public void flipSwitch()
  {
    switchLamp = !switchLamp;
    if (switchLamp)
    {
      glow();
    }
  }
}

public abstract class Empty {

}
abstract class A {
  abstract void foo();
  abstract void bar();
}

abstract class B extends A { //Compliant, partial implementation.
  void foo() {};
}
interface I {
  void foo();
  void bar();
}
abstract class C implements I { //compliant, partial implementation
  int i = 0;

  @Override
  public void foo() {}
}

public abstract class Parametrized<T> {
  abstract void foo();
}
