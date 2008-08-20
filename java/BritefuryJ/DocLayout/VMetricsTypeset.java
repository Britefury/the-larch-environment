package BritefuryJ.DocLayout;

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


	public VMetricsTypeset scaled(double scale)
	{
		return new VMetricsTypeset( ascent * scale, descent * scale, vspacing * scale );
	}
	
	public VMetricsTypeset minSpacing(double spacing)
	{
		if ( spacing > vspacing )
		{
			return new VMetricsTypeset( ascent, descent, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public VMetrics offsetLength(double deltaLength)
	{
		return new VMetricsTypeset( ascent + deltaLength *0.5, descent + deltaLength * 0.5, vspacing );
	}
	
	public VMetricsTypeset withHeight(double height)
	{
		double deltaHeight = height - this.height;
		return new VMetricsTypeset( ascent + deltaHeight * 0.5, descent + deltaHeight * 0.5, vspacing );
	}

	
	public boolean isTypeset()
	{
		return true;
	}
	

	public String toString()
	{
		return "VMetricsTypeset( ascent=" + String.valueOf( ascent ) + ", descent=" + String.valueOf( descent ) + ", height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
