package Britefury.DocPresent;

import java.awt.Color;



public class DPHBox extends DPAbstractHBox
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND };
	
	
	protected static class VBoxChildEntry extends DPAbstractBox.BoxChildEntry
	{
		public Alignment alignment;
		
		public VBoxChildEntry(DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
		{
			super( child, bExpand, bFill, bShrink, padding );
			
			this.alignment = alignment;
		}
	}
	
	
	Alignment alignment;
	
	
	
	
	
	public DPHBox()
	{
		this( Alignment.CENTRE, 0.0, false, false, false, 0.0, null );
	}
	
	public DPHBox(Alignment alignment, double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		this( alignment, spacing, bExpand, bFill, bShrink, padding, null );
	}
	
	public DPHBox(Alignment alignment, double spacing, boolean bExpand, boolean bFill, boolean bShrink, double padding, Color backgroundColour)
	{
		super( spacing, bExpand, bFill, bShrink, padding, backgroundColour );
		
		this.alignment = alignment;
	}
	
	
	
	
	public Alignment getAlignment()
	{
		return alignment;
	}

	public void setAlignment(Alignment alignment)
	{
		this.alignment = alignment;
		queueResize();
	}

	

	public void append(DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		appendChildEntry( new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding ) );
	}

	
	public void insert(int index, DPWidget child, Alignment alignment, boolean bExpand, boolean bFill, boolean bShrink, double padding)
	{
		insertChildEntry( index, new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding ) );
	}
	
	
	
	protected VBoxChildEntry createChildEntryForChild(DPWidget child)
	{
		return new VBoxChildEntry( child, alignment, bExpand, bFill, bShrink, padding );
	}
	
	
	
	protected VMetrics computeRequiredVMetrics()
	{
		if ( childEntries.size() == 0 )
		{
			childrenVMetrics = new VMetrics();
		}
		else
		{
			VMetrics[] childVMetrics = new VMetrics[childEntries.size()];
			for (int i = 0; i < childVMetrics.length; i++)
			{
				childVMetrics[i] = childEntries.get( i ).child.getRequiredVMetrics();
			}
			
			VMetrics vm = new VMetrics();
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				vm.height = vm.height > chm.height  ?  vm.height : chm.height;
				vm.vspacing = vm.vspacing > chm.vspacing  ?  vm.vspacing : chm.vspacing;
			}
			
			childrenVMetrics = vm;
		}
		
		return childrenVMetrics;
	}



	protected VMetrics onAllocateY(double allocation)
	{
		for (ChildEntry baseEntry: childEntries)
		{
			VBoxChildEntry entry = (VBoxChildEntry)baseEntry;
			double childAlloc = entry.child.vmetrics.height < allocation  ?  entry.child.vmetrics.height : allocation;
			if ( entry.alignment == Alignment.TOP )
			{
				allocateChildY( entry.child, 0.0, childAlloc );
			}
			else if ( entry.alignment == Alignment.CENTRE )
			{
				allocateChildY( entry.child, ( allocation - childAlloc )  *  0.5, childAlloc );
			}
			else if ( entry.alignment == Alignment.BOTTOM )
			{
				allocateChildY( entry.child, allocation - childAlloc, childAlloc );
			}
			else if ( entry.alignment == Alignment.EXPAND )
			{
				allocateChildY( entry.child, 0.0, allocation );
			}
		}
		
		return childrenVMetrics;
	}
}
