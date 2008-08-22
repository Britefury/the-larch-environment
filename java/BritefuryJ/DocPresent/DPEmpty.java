package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;

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
