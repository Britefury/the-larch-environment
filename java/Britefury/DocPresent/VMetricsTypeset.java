package Britefury.DocPresent;

public class VMetricsTypeset extends VMetrics
{
	public double ascent, descent, baselineOffset;
	
	
	
	public VMetricsTypeset()
	{
		super();
		ascent = descent = baselineOffset = 0.0;
	}
	
	public VMetricsTypeset(double ascent, double descent, double vspacing)
	{
		super( ascent + descent, vspacing );
		this.ascent = ascent;
		this.descent = descent;
		baselineOffset = 0.0;
		this.vspacing = vspacing;
	}

	public VMetricsTypeset(double ascent, double descent, double baselineOffset, double vspacing)
	{
		super( ascent + descent, vspacing );
		this.ascent = ascent;
		this.descent = descent;
		this.baselineOffset = baselineOffset;
		this.vspacing = vspacing;
	}


	public VMetrics scaled(double scale)
	{
		return new VMetricsTypeset( ascent * scale, descent * scale, baselineOffset * scale, vspacing * scale );
	}
	
	
	public String toString()
	{
		return "VMetricsTypeset( ascent=" + String.valueOf( ascent ) + ", descent=" + String.valueOf( descent ) + ", height=" + String.valueOf( height ) + ", baselineOffset=" + String.valueOf( baselineOffset ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}

}
