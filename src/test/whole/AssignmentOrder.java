// JLS3 p415
public class AssignmentOrder
{
	public static void main(String[] args) {
		int i = 2;
		int j = (i = 3) * i;
		System.out.println(j);
		
		int a = 9;
		a += (a = 3);
		System.out.println(a);
		int b = 9;
		b = b + (b = 3);
		System.out.println(b);		
	}
}
