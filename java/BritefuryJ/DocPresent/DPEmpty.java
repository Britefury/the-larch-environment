package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;

public class DPEmpty extends DPWidget
{
	public DPEmpty()
	{
		this( WidgetStyleSheet.defaultStyleSheet );
	}
	
	public DPEmpty(WidgetStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	
	
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
