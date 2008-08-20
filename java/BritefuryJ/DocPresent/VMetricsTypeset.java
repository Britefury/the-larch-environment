package BritefuryJ.DocPresent;

public class VMetricsTypeset extends VMetrics
{
	public double ascent, descent;
	
	
	
	public VMetricsTypeset()
	{
		super();
		ascent = descent = 0.0;
	}
	
	public VMetricsTypeset(double ascent, double descent, double vspacing)
	{
		super( ascent + descent, vspacing );
		this.ascent = ascent;
		this.descent = descent;
	}


	public VMetrics scaled(double scale)
	{
		return new VMetricsTypeset( ascent * scale, descent * scale, vspacing * scale );
	}
	
	
	public String toString()
	{
		return "VMetricsTypeset( ascent=" + String.valueOf( ascent ) + ", descent=" + String.valueOf( descent ) + ", height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
