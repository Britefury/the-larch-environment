package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.BorderStyleSheet;


public class DPBorder extends DPBin
{
	public DPBorder()
	{
		this( BorderStyleSheet.defaultStyleSheet );
	}

	public DPBorder(BorderStyleSheet styleSheet)
	{
		super( styleSheet );
	}
	
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		BorderStyleSheet style = getStyleSheet();
		
		if ( child != null )
		{
			return child.refreshMinimumHMetrics().border( style.getLeftMargin(), style.getRightMargin() );
		}
		else
		{
			return new HMetrics().border( style.getLeftMargin(), style.getRightMargin() );
		}
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		BorderStyleSheet style = getStyleSheet();
		
		if ( child != null )
		{
			return child.refreshPreferredHMetrics().border( style.getLeftMargin(), style.getRightMargin() );
		}
		else
		{
			return new HMetrics().border( style.getLeftMargin(), style.getRightMargin() );
		}
	}
	
	protected VMetrics computeMinimumVMetrics()
	{
		BorderStyleSheet style = getStyleSheet();
		
		if ( child != null )
		{
			return child.refreshMinimumVMetrics().border( style.getTopMargin(), style.getBottomMargin() );
		}
		else
		{
			return new VMetrics().border( style.getTopMargin(), style.getBottomMargin() );
		}
	}

	protected VMetrics computePreferredVMetrics()
	{
		BorderStyleSheet style = getStyleSheet();
		
		if ( child != null )
		{
			return child.refreshPreferredVMetrics().border( style.getTopMargin(), style.getBottomMargin() );
		}
		else
		{
			return new VMetrics().border( style.getTopMargin(), style.getBottomMargin() );
		}
	}
	
	
	
	
	protected void allocateContentsX(double width)
	{
		if ( child != null )
		{
			allocateChildX( child, getStyleSheet().getLeftMargin(), width );
		}
	}

	protected void allocateContentsY(double height)
	{
		if ( child != null )
		{
			allocateChildY( child, getStyleSheet().getTopMargin(), height );
		}
	}
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//
	
	protected BorderStyleSheet getStyleSheet()
	{
		return (BorderStyleSheet)styleSheet;
	}
}
