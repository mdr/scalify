public class BreakLoop
{
	public static void main(String[] args) {
		int i = 0;
		
		while (i < 10) {
			int j = 0;
			while (j < 5) {
				if (j > 1)
					System.out.println("j > 1");
				if (i > 1)
					break;
				j++;
			}
			if (i > 3)
				break;
			i++;
		}
	}
}
