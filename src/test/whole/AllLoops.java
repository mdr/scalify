public class AllLoops {
	public static void main(String[] args) {
		int i = 0;
		
		// For loop
		for (i = 0; i < 5; i++)
			System.out.print(i);
			
		// While loop
		while (i++ < 10)
			System.out.print(i);
			
		System.out.println("");
		i = 0;
			
		// Do/While loop
		do {
			System.out.print(i);
		} while (++i < 5);
		
		System.out.println("");
	}
}
