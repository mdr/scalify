
public class IncOrder {
    public static void main(String[] args) {
		// JPL4 p205
		int i = 16;
		System.out.println(++i + " " + i++ + " " + i);
		
		// int a = 5;
		// a = ++a + a++;
		// System.out.println(a);
		// 
		// a = 3;
		// a = 5 + a++;
		// System.out.println(a);
		
		int[] nums = { 0,1,2,3,4,5,6,7,8,9 };
		nums[side3(side1(), side2())] += 1;
		System.out.println("count1,2,3 = " + count1 + " " + count2 +" " + count3);
		nums[side2()] += 1;
		System.out.println("count1,2,3 = " + count1 + " " + count2 +" " + count3);
		nums[side1()] = nums[side1()] + 1;
		System.out.println("count1,2,3 = " + count1 + " " + count2 +" " + count3);
    }

	static int count1 = 0;
	static int count2 = 0;
	static int count3 = 0;
	
	public static int side1() {
		count1 += 1;
		return count1;
	}
	
	public static int side2() {
		count2 += 1;
		return count2;
	}
	
	public static int side3(int x, int y) {
		count3 += 1;
		return (x + y + count3);
	}
	
}
