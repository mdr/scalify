public class C extends B {

	public C()                  { super(5, "bob"); System.out.print("C() "); }
	public C(float x)           { super(Float.toString(x)); System.out.print("C(float) ");  }
	public C(double x)          { super(Double.toString(x)); System.out.print("C(double) ");  }
}