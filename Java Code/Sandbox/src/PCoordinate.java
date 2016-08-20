
public class PCoordinate {
	private int x;
	private int y;
	
	public static final PCoordinate ORIGIN = new PCoordinate(0,0);
	
	public PCoordinate(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public static void increaseByOne(final PCoordinate input, PCoordinate input2) {
		input.setX(input.getX() + 1);
		input.setY(input.getY() + 1);
		System.out.println(PCoordinate.multiply(input, PCoordinate.ORIGIN));
	}
	
	public void multiply(PCoordinate input) {
		this.setX(this.getX() * input.getX());
		this.setY(this.getY() * input.getY());
	}
	
	public static PCoordinate multiply(PCoordinate first, PCoordinate second) {
		PCoordinate pCoordinate = new PCoordinate(first.getX() * second.getX(), first.getY() * second.getY());
		return pCoordinate;
	}
	
	
}
