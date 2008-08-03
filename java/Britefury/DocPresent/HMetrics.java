package Britefury.DocPresent;

public class HMetrics {
	public double width, advance;
	
	
	
	public HMetrics()
	{
		width = advance = 0.0;
	}
	
	public HMetrics(double width, double advance)
	{
		this.width = width;
		this.advance = advance;
	}
	
	
	public HMetrics scaled(double scale)
	{
		return new HMetrics( width * scale, advance * scale );
	}
}
