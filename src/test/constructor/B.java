public class B extends A {
    // public int x;
	
	public B(int x)				{ System.out.print("B(int) "); }
	public B(String x)			{ super(x); System.out.print("B(String) "); }
	public B(int x, String y)	{ super(x); System.out.print("B(int, String)  "); }
	public B(int x, int y, String z)		{ this(x, z); this.x = y; System.out.print("B(int, int, String) "); }
		
// 	public A(int x)				{ System.out.println("A(int)"); }
// 	public A(String x)			{ System.out.println("A(String)"); }
// 	public A(int x, String y)	{ System.out.println("A(int, String)"); }
// 	
}