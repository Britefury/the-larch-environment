package BritefuryJ.DocPresent;

public class VMetrics
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

	public VMetrics scaled(double scale)
	{
		return new VMetrics( height * scale, vspacing * scale );
	}
	
	
	public String toString()
	{
		return "VMetrics( height=" + String.valueOf( height ) + ", vspacing=" + String.valueOf( vspacing ) + " )";
	}
}
