public class InnerClasses {
	public int outerInt;
	public final int getOuterInt() { return outerInt; }
	Member x;
	
	public class Member {
	    public int innerInt;
	    public Member() { innerInt = 2; }
		public final InnerClasses struct() { return Struct.this; }
		public final int position() { return outerInt + innerInt + getOuterInt(); }
	}
	
	public InnerClasses() {
	    x = new Member();
	    outerInt = 5;
	    System.out.println(x.struct().outerInt + x.position());
	}
	
	public static void main(String[] args) {
		InnerClasses s = new Struct();
	}
}