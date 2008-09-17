
object ScaIncOrder {
	
	def main(args: Array[String]) = {
		var nums = (0 until 10).toArray
		println(nums.map(_.toString).reduceLeft(_ + " " + _))
		
		var tmp = side1
		nums(tmp) += 1
		// nums(side1) += 1
		// println("count = " + count)
		// nums(counter) = nums(counter) + 1
		// println("count = " + count)
		println("Final values of count1 and count2: " + count1 + " " + count1)
	}
	
	var count1: Int = 0;
	var count2: Int = 0;
	
	def side1 = {
		count1 += 1
		count1
	}
	
	def side2 = {
		count2 += 1
		count2
	}
}
