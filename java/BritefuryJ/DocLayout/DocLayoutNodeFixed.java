package BritefuryJ.DocLayout;

public class DocLayoutNodeFixed extends DocLayoutNode
{
	private HMetrics hmetrics;
	private VMetrics vmetrics;
	
	
	public DocLayoutNodeFixed(HMetrics hmetrics, VMetrics vmetrics)
	{
		this.hmetrics = hmetrics;
		this.vmetrics = vmetrics;
	}
	
	
	void setMetrics(HMetrics hmetrics, VMetrics vmetrics)
	{
		this.hmetrics = hmetrics;
		this.vmetrics = vmetrics;
		requestRelayout();
	}
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return hmetrics;
	}

	protected HMetrics computePreferredHMetrics()
	{
		return hmetrics;
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return vmetrics;
	}

	protected VMetrics computePreferredVMetrics()
	{
		return vmetrics;
	}
}
