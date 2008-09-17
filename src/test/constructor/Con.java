public class Con {
	public static void main(String[] args) {
	    new DD(1,2,3); pln();
	}
	
	public static void p(String msg) {
	    System.out.print(msg + " ");
	}
	public static void pln() {
        System.out.println();
	}
}

class AA {
	public int x;
	public int y;
	
	public AA()					{ Con.p("AA()"); }
	public AA(int x)				{ Con.p("AA(1)"); }
	public AA(String x)			{ } 
	public AA(int x, String y)	{ }
}

class BB extends AA {
	public BB(int x, String z)	{ Con.p("BB(2)"); }
}

class CC extends BB {
	public CC()                      { super(5, "bob"); Con.p("CC()"); }
	public CC(int x, int y, int z)   { super(x, String.valueOf(z)); Con.p("CC(3)"); }
}

class DD extends CC {
    public DD(int x, int y, int z)           { this(x, y, z, 55); Con.p("DD(3)"); }
    public DD(int x, int y, int z, int q)    { super(x, y, z); }
}