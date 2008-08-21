package BritefuryJ.DocPresent;

public class VMetrics extends Metrics
{
	public double height, vspacing;
	
	
	
	public VMetrics()
	{
		height = vspacing = 0.0;
	}
	
	public VMetrics(double height, double vspacing)
	{
		this.height = height;
		this.vspacing = vspacing;
	}

	
	public double getLength()
	{
		return height;
	}
	
	public double getTotalLength()
	{
		return height + vspacing;
	}
	
	public VMetrics scaled(double scale)
	{
		return new VMetrics( height * scale, vspacing * scale );
	}
	
	public VMetrics minSpacing(double spacing)
	{
		if ( spacing > vspacing )
		{
			return new VMetrics( height, spacing );
		}
		else
		{
			return this;
		}
	}
	
	public VMetrics offsetLength(double deltaLength)
	{
		return new VMetrics( height + deltaLength, vspacing );
	}
	
	public VMetrics withHeight(double height)
	{
		return new VMetrics( height, vspacing );
	}
	
	
	
	public boolean isTypeset()
	{
		return false;
	}
	
	
	public String toString()
	{
		return "VMetrics( height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
