package Britefury.DocPresent;

public class HMetrics
{
	public double width;
	
	
	
	public HMetrics()
	{
		width = 0.0;
	}
	
	public HMetrics(double width)
	{
		this.width = width;
	}
	
	
	public HMetrics scaled(double scale)
	{
		return new HMetrics( width * scale );
	}
	
	
	public String toString()
	{
		return "HMetrics( width=" + String.valueOf( width ) + " )";
	}
}
