package Britefury.DocPresent;

import java.awt.Color;

public class Bin extends Container
{
	protected Widget child;
	protected HMetrics childHMetrics;
	protected VMetrics childVMetrics;
	protected double childScale;
	
	
	
	public Bin()
	{
		this( null );
	}
	
	public Bin(Color backgroundColour)
	{
		super( backgroundColour );
		
		childHMetrics = new HMetrics();
		childVMetrics = new VMetrics();
		childScale = 1.0;
	}
	
	
	
	public Widget getChild()
	{
		return child;
	}
	
	public void setChild(Widget child)
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
	
	
	protected void removeChild(Widget child)
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
	
	
	
	protected void onAllocateX(double width)
	{
		if ( child != null )
		{
			allocateChildX( child, 0.0, width );
		}
	}

	protected void onAllocateY(double height)
	{
		if ( child != null )
		{
			allocateChildY( child, 0.0, height );
		}
	}
	
	
	
	protected void onChildResizeRequest(Widget child)
	{
		queueResize();
	}
	
	
	
	//
	// Focus navigation methods
	//
	
	protected Widget[] horizontalNavigationList()
	{
		if ( child != null )
		{
			Widget[] navList = { child };
			return navList;
		}
		else
		{
			return null;
		}
	}
	
	
}
