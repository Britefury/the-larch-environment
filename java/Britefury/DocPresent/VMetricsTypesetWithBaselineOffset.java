package Britefury.DocPresent;


public class VMetricsTypesetWithBaselineOffset extends VMetricsTypeset
{
	public double baselineOffset;
	
	
	
	public VMetricsTypesetWithBaselineOffset()
	{
		super();
		baselineOffset = 0.0;
	}
	
	public VMetricsTypesetWithBaselineOffset(double ascent, double descent, double baselineOffset, double vspacing)
	{
		super( ascent, descent, vspacing );
		this.baselineOffset = baselineOffset;
	}


	public VMetrics scaled(double scale)
	{
		return new VMetricsTypesetWithBaselineOffset( ascent * scale, descent * scale, baselineOffset * scale, vspacing * scale );
	}
	
	
	public String toString()
	{
		return "VMetricsTypesetWithOffset( ascent=" + String.valueOf( ascent ) + ", descent=" + String.valueOf( descent ) + ", height=" + String.valueOf( height ) + ", baselineOffset=" + String.valueOf( baselineOffset ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
