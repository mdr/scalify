public class SimpleBreakContinue
{
	public static void main(String[] args) {
		int i = 0;

		System.out.println("Why was 6 afraid of 7?");
		System.out.print("Because... ");
		while (i++ < 10) {
			if (i < 7)
				continue;
				
			if (i > 2)
				System.out.print(i);
			
			if (i > 8)
				break;
		}
		System.out.println("!");
	}
}
