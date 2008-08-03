package Britefury.DocPresent;

public class VMetrics {
	public double ascent, descent, height, vspacing;
	
	
	
	public VMetrics()
	{
		ascent = descent = height = vspacing = 0.0;
	}
	
	public VMetrics(double ascent, double descent, double height, double vspacing)
	{
		this.ascent = ascent;
		this.descent = descent;
		this.height = height;
		this.vspacing = vspacing;
	}


	public VMetrics scaled(double scale)
	{
		return new VMetrics( ascent * scale, descent * scale, height * scale, vspacing * scale );
	}
}
