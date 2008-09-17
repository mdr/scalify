
public class A {
	public int x;
	public int y;
	
	public A()					{ System.out.print("A() "); }
	public A(int x)				{ this.x = (x * 2); this.y = x ; System.out.print("A(int) "); }
	public A(String x)			{ System.out.print("A(String) "); }
	public A(int x, String y)	{ System.out.print("A(int, String) "); }
	
	public void go() {
		System.out.println(x);
		System.out.println(y);
	}
}
