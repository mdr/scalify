
public class EnhancedFor {
    public static void main(String[] args) {
		average(new int[]{1,2,3,4});
	}
	
	static double average(int[] values) {
		double sum = 0.0;
		
		for (int val: values)
			sum += val;
			
		System.out.println("Average: " + (sum / values.length));
		
		for (int val: values) {
			sum *= val;
			sum *= val;
		}
		
		System.out.println("Superproduct: " + sum);
		return sum;
    }
}
