package Britefury.DocPresent;

public class HMetricsTypeset extends HMetrics
{
	public double advance;
	
	
	
	public HMetricsTypeset()
	{
		super();
		advance = 0.0;
	}
	
	public HMetricsTypeset(double width, double advance)
	{
		super( width );
		this.advance = advance;
	}
	
	
	public HMetrics scaled(double scale)
	{
		return new HMetricsTypeset( width * scale, advance * scale );
	}
	
	
	public String toString()
	{
		return "HMetricsTypeset( width=" + String.valueOf( width ) + ", advance=" + String.valueOf( advance ) + " )";
	}
}
