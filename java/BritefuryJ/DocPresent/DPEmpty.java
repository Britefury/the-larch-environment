package BritefuryJ.DocPresent;

public class DPEmpty extends DPWidget
{
	protected HMetrics computeMinimumHMetrics()
	{
		return new HMetrics();
	}

	protected HMetrics computePreferredHMetrics()
	{
		return new HMetrics();
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return new VMetrics();
	}
	
	protected VMetrics computePreferredVMetrics()
	{
		return new VMetrics();
	}
}
