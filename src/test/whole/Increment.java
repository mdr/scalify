
public class Increment {
    public static void main(String[] args) {
		char c = 'a';
		while (c <= 'z') {
			System.out.print(c);
			c++;
			c += 1;
			c = (char)(c + 1);
		}
		System.out.println("");
	}
}
