package BritefuryJ.DocPresent;

import java.awt.Color;
import java.util.List;



abstract public class DPAbstractBox extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}


	protected static class BoxChildEntry extends DPContainer.ChildEntry
	{
		public int packFlags;
		public double padding;
		
		public BoxChildEntry(DPWidget child, boolean bExpand, double padding)
		{
			super( child );
			
			this.packFlags = Metrics.packFlags( bExpand );
			this.padding = padding;
		}
	}

	
	
	protected double spacing;
	protected boolean bExpand;
	protected double padding;
	HMetrics childrenHMetrics;
	VMetrics childrenVMetrics;

	
	
	public DPAbstractBox()
	{
		super();
	}

	public DPAbstractBox(double spacing, boolean bExpand, double padding)
	{
		this( spacing, bExpand, padding, null );
	}
	
	public DPAbstractBox(double spacing, boolean bExpand, double padding, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.bExpand = bExpand;
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

	public void extend(List<DPWidget> children)
	{
		ChildEntry[] entries = new ChildEntry[children.size()];
		
		for (int i = 0; i < children.size(); i++)
		{
			entries[i] = createChildEntryForChild( children.get( i ) );
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
	}






	protected int[] getChildrenPackFlags(List<ChildEntry> nodes)
	{
		int[] chm = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = ((BoxChildEntry)nodes.get( i )).packFlags;
		}
		return chm;
	}

	protected int[] getChildrenPackFlags()
	{
		return getChildrenPackFlags( childEntries );
	}



	protected double getChildPadding(int index)
	{
		return ((BoxChildEntry)childEntries.get( index )).padding;
	}
}
