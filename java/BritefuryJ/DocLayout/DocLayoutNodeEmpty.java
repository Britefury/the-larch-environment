package BritefuryJ.DocLayout;

public class DocLayoutNodeEmpty extends DocLayoutNode
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
