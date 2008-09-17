public class Static
{
	private static void test() {
		System.out.println("Hello world!");
	}

	public static void main(String[] args) {
		((Static)null).test();
	}
}