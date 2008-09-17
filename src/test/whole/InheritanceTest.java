import static java.lang.System.out;

public class InheritanceTest {
	
    public static void main(String[] args) {
		new Foo();
		new Foo("Quux");
		new Bar();
		new Bar("Zaphod Beeblebrox");
    }
}

class Foo {	
	public Foo() {
		out.println("Foo()");
	}
	
	public Foo(String Bar) {
		out.println("Foo(" + Bar + ")");
	}
}

class Bar extends Foo {
	public Bar() {
		super();
	}
	
	public Bar(String Baz) {
		super(Baz);
		out.println("Bar(" + Baz + ")");
	}
}