package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.AbstractBoxStyleSheet;



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

	
	
	HMetrics childrenHMetrics;
	VMetrics childrenVMetrics;

	
	
	public DPAbstractBox()
	{
		this( AbstractBoxStyleSheet.defaultStyleSheet );
	}

	public DPAbstractBox(AbstractBoxStyleSheet styleSheet)
	{
		super( styleSheet );
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




	public double getSpacing()
	{
		return ((AbstractBoxStyleSheet)styleSheet).getSpacing();
	}

	public boolean getExpand()
	{
		return ((AbstractBoxStyleSheet)styleSheet).getExpand();
	}

	public double getPadding()
	{
		return ((AbstractBoxStyleSheet)styleSheet).getPadding();
	}
}
