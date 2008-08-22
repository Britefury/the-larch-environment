package BritefuryJ.DocPresent;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;


public class DPBin extends DPContainer
{
	protected DPWidget child;
	protected double childScale;
	
	
	
	public DPBin()
	{
		this( null );
	}
	
	public DPBin(Color backgroundColour)
	{
		super( backgroundColour );
		
		childScale = 1.0;
	}
	
	
	
	public DPWidget getChild()
	{
		return child;
	}
	
	public void setChild(DPWidget child)
	{
		if ( child != this.child )
		{
			if ( child != null )
			{
				child.unparent();
			}
			
			if ( this.child != null )
			{
				ChildEntry entry = childToEntry.get( this.child );
				unregisterChildEntry( entry );
				childEntries.remove( entry );
			}
			
			this.child = child;
			
			if ( this.child != null )
			{
				ChildEntry entry = new ChildEntry( this.child );
				childEntries.add( entry );
				registerChildEntry( entry );				
			}
			
			queueResize();
		}
	}
	
	
	public double getChildScale()
	{
		return childScale;
	}
	
	public void setChildScale(double scale)
	{
		childScale = scale;
		queueResize();
	}
	
	
	protected void removeChild(DPWidget child)
	{
		assert child == this.child;
		setChild( null );
	}
	
	
	

	protected HMetrics computeMinimumHMetrics()
	{
		if ( child != null )
		{
			return child.refreshMinimumHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		if ( child != null )
		{
			return child.refreshPreferredHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected VMetrics computeMinimumVMetrics()
	{
		if ( child != null )
		{
			return child.refreshMinimumVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}

	protected VMetrics computePreferredVMetrics()
	{
		if ( child != null )
		{
			return child.refreshPreferredVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}
	
	
	
	
	protected void allocateContentsX(double width)
	{
		if ( child != null )
		{
			allocateChildX( child, 0.0, width );
		}
	}

	protected void allocateContentsY(double height)
	{
		if ( child != null )
		{
			allocateChildY( child, 0.0, height );
		}
	}
	
	
	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		if ( child != null )
		{
			DPWidget[] navList = { child };
			return Arrays.asList( navList );
		}
		else
		{
			return null;
		}
	}
	
	
}
