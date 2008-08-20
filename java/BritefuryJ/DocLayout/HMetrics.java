package BritefuryJ.DocLayout;

public class HMetrics extends Metrics
{
	public double width, hspacing;
	
	
	
	public HMetrics()
	{
		width = 0.0;
		hspacing = 0.0;
	}
	
	public HMetrics(double width)
	{
		this.width = width;
		this.hspacing = 0.0;
	}
	
	public HMetrics(double width, double hspacing)
	{
		this.width = width;
		this.hspacing = hspacing;
	}
	
	
	public double getLength()
	{
		return width;
	}
	
	public double getTotalLength()
	{
		return width + hspacing;
	}
	
	public HMetrics scaled(double scale)
	{
		return new HMetrics( width * scale, hspacing * scale );
	}
	
	
	public HMetrics minSpacing(double spacing)
	{
		if ( spacing > hspacing )
		{
			return new HMetrics( width, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public HMetrics offsetLength(double deltaLength)
	{
		return new HMetrics( width + deltaLength, hspacing );
	}
	
	public HMetrics withWidth(double width)
	{
		return new HMetrics( width, hspacing );
	}
	
	
	
	
	
	
	
	public String toString()
	{
		return "HMetrics( width=" + String.valueOf( width ) + ", hspacing=" + String.valueOf( hspacing ) + " )";
	}
}
