package BritefuryJ.DocPresent;

public class HMetrics
{
	public double width, advance;
	
	
	
	public HMetrics()
	{
		width = 0.0;
		advance = 0.0;
	}
	
	public HMetrics(double width)
	{
		this.width = width;
		this.advance = width;
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
	
	
	public String toString()
	{
		return "HMetrics( width=" + String.valueOf( width ) + ", advance=" + String.valueOf( advance ) + " )";
	}
}
