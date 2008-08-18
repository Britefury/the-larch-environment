package Britefury.DocPresent;

import java.awt.Color;



abstract public class DPAbstractBox extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}


	protected static class BoxChildEntry extends DPContainer.ChildEntry
	{
		public boolean bExpand, bFill, bShrink;
		public double padding;
		
		public BoxChildEntry(DPWidget child, boolean bExpand, boolean bFill, boolean bShrink, double padding)
		{
			super( child );
			
			this.bExpand = bExpand;
			this.bFill = bFill;
			this.bShrink = bShrink;
			this.padding = padding;
		}
	}

	
	
	protected double spacing;
	protected boolean bExpand;
	protected boolean bFill;
	protected boolean bShrink;
	protected double padding;
	int numExpand;
	int numShrink;
	HMetrics childrenHMetrics;
	VMetrics childrenVMetrics;

	
	
	public DPAbstractBox()
	{
		super();
	}

	public DPAbstractBox(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		this( spacing, bExpand, bFill, bShrink, padding, null );
	}
	
	public DPAbstractBox(double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.bExpand = bExpand;
		this.bFill = bFill;
		this.bShrink = bShrink;
		this.padding = padding;
	}


	
	public double getSpacing()
	{
		return spacing;
	}

	public void setSpacing(double spacing)
	{
		this.spacing = spacing;
		queueResize();
	}

	
	public boolean getExpand()
	{
		return bExpand;
	}

	public void setExpand(boolean bExpand)
	{
		this.bExpand = bExpand;
		queueResize();
	}

	
	public boolean getFill()
	{
		return bFill;
	}

	public void setFill(boolean bFill)
	{
		this.bFill = bFill;
		queueResize();
	}

	
	public boolean getShrink()
	{
		return bShrink;
	}

	public void setShrink(boolean bShrink)
	{
		this.bShrink = bShrink;
		queueResize();
	}

	
	public double getPadding()
	{
		return padding;
	}

	public void setPadding(double padding)
	{
		this.padding = padding;
		queueResize();
	}

	
	
	
	public void append(DPWidget child)
	{
		appendChildEntry( createChildEntryForChild( child ) );
	}

	public void extend(DPWidget[] children)
	{
		ChildEntry[] entries = new ChildEntry[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			entries[i] = createChildEntryForChild( children[i] );
		}
		
		extendChildEntries( entries );
	}

	
	public void insert(int index, DPWidget child)
	{
		insertChildEntry( index, createChildEntryForChild( child ) );
	}

	public void remove(DPWidget child)
	{
		removeChildEntry( childToEntry.get( child ) );
	}

	
	protected void childListModified()
	{
		numExpand = numShrink = 0;
		
		for (ChildEntry entry: childEntries)
		{
			BoxChildEntry vboxEntry = (BoxChildEntry)entry;
			if ( vboxEntry.bExpand )
			{
				numExpand++;
			}
			if ( vboxEntry.bShrink )
			{
				numShrink++;
			}
		}
	}
}
