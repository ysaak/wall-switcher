package ysaak.screenchanger.data;

import javafx.stage.Screen;

public class ScreenDto implements Comparable<ScreenDto> {
	private int id;

	private final double x;
	private final double y;
	private final double width;
	private final double height;
	
	public ScreenDto(int id, double x, double y, double width, double height) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public ScreenDto(int id, Screen screen) {
		this(id, screen.getBounds().getMinX(),
				screen.getBounds().getMinY(), 
				screen.getBounds().getWidth(), 
				screen.getBounds().getHeight());
	}

	public int getId() {
		return id;
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
	
	@Override
	public int compareTo(ScreenDto s) {
		double xw = x + width;
		double yh = y + height;
		
		double xwS = s.getX() + s.getWidth();
		double yhS = s.getY() + s.getHeight();
		
		if (xw == xwS && yh == yhS)
			return 0;
		
		if (xw <= xwS && yh <= yhS) 
			return -1;
		
		return 1;
	}
}
