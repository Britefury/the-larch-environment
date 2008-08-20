package BritefuryJ.DocPresent;

public class DPEmpty extends DPWidget
{
	protected HMetrics computeRequiredHMetrics()
	{
		return new HMetrics();
	}

	protected VMetrics computeRequiredVMetrics()
	{
		return new VMetrics();
	}

	
	protected HMetrics onAllocateX(double allocation)
	{
		return new HMetrics();
	}

	protected VMetrics onAllocateY(double allocation)
	{
		return new VMetrics();
	}
}
