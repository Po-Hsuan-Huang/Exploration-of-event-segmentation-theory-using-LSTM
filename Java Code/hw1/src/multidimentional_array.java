

public class multidimentional_array {

	public static void main(String[] args) {
		
		// Rather than a multi-dimensional array, it is an array of arrays.
		int[][] grid = {
				{0,2,4,5},{2,2},{1,3,5} // Not necessarily The same length for each row.
		};
		
		System.out.println(grid[1][1]); // second row, second column.
		System.out.println(grid[0][2]);// first row, third column.
	
		String [][] text = new String[2][3]; // allocating length of the variable.
		for (int row = 0 ; row < grid.length;row ++){
			for (int col = 0; col<grid[row].length; col ++){
				System.out.println(grid[row][col]);
			};
		};
	}

}
