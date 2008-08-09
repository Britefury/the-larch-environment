package Britefury.DocPresent;

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

	
	protected void onAllocateX(double allocation)
	{
	}

	protected void onAllocateY(double allocation)
	{
	}
}
