package BritefuryJ.DocPresent;

import java.awt.Color;

public class DPBin extends DPContainer
{
	protected DPWidget child;
	protected HMetrics childHMetrics;
	protected VMetrics childVMetrics;
	protected double childScale;
	
	
	
	public DPBin()
	{
		this( null );
	}
	
	public DPBin(Color backgroundColour)
	{
		super( backgroundColour );
		
		childHMetrics = new HMetrics();
		childVMetrics = new VMetrics();
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
				childEntries.remove( entry );
				unregisterChildEntry( entry );
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
	
	
	

	protected HMetrics computeRequiredHMetrics()
	{
		if ( child != null )
		{
			childHMetrics = child.computeRequiredHMetrics();
		}
		else
		{
			childHMetrics = new HMetrics();
		}
		
		return childHMetrics;
	}

	protected VMetrics computeRequiredVMetrics()
	{
		if ( child != null )
		{
			childVMetrics = child.computeRequiredVMetrics();
		}
		else
		{
			childVMetrics = new VMetrics();
		}
		
		return childVMetrics;
	}
	
	
	
	protected HMetrics onAllocateX(double width)
	{
		if ( child != null )
		{
			return allocateChildX( child, 0.0, width );
		}
		else
		{
			return new HMetrics();
		}
	}

	protected VMetrics onAllocateY(double height)
	{
		if ( child != null )
		{
			return allocateChildY( child, 0.0, height );
		}
		else
		{
			return new VMetrics();
		}
	}
	
	
	
	protected void onChildResizeRequest(DPWidget child)
	{
		queueResize();
	}
	
	
	
	//
	// Focus navigation methods
	//
	
	protected DPWidget[] horizontalNavigationList()
	{
		if ( child != null )
		{
			DPWidget[] navList = { child };
			return navList;
		}
		else
		{
			return null;
		}
	}
	
	
}
