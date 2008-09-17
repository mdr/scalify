import static java.lang.System.out;

public class Generics {
    public static void main(String[] args) {
		Box<Integer> integerBox = new Box<Integer>();
		integerBox.add(new Integer(10));
		Integer someInteger = integerBox.get();
		out.println(someInteger);
		
		out.println(void.class);
		// out.println(Box.class);
    }
	
	// static void expurgate(Collection c) {
	//     for (Iterator i = c.iterator(); i.hasNext(); )
	//       if (((String) i.next()).length() == 4)
	//         i.remove();
	// }
}

class Box<T> {
    private T t; // T stands for "Type"          

    public void add(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }
}