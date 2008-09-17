
public class Switch {
    public static void main(String[] args) {
		int q = 0;
		int i = 0;
top:	while (q < 2) {
			while (++i < 10) {
				System.out.print("\n== i = " + i + " ==>> ");
				switch (i) {
					case 1:		System.out.print("1");
					case 2:		System.out.print("2"); 
					case 3:		System.out.print("3"); break;
					case 4:
					case 5:		System.out.print("5"); break;
					case 6:		System.out.print("6");
					default:	System.out.print("D");
					case 7:		System.out.print("7"); break top;
					case 8:		System.out.print("8");
				}
			}
			System.out.println("");
			q++;
		}
		System.out.println("");
    }
}
