//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
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


	protected static class BoxParentPacking extends ParentPacking
	{
		public int packFlags;
		public double padding;
		
		public BoxParentPacking(boolean bExpand, double padding)
		{
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


	
	
	protected void childListModified()
	{
	}






	protected int[] getChildrenPackFlags(List<DPWidget> nodes)
	{
		int[] chm = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = ((BoxParentPacking)nodes.get( i ).getParentPacking()).packFlags;
		}
		return chm;
	}

	protected int[] getChildrenPackFlags()
	{
		return getChildrenPackFlags( registeredChildren );
	}



	protected double getChildPadding(int index)
	{
		return ((BoxParentPacking)registeredChildren.get( index ).getParentPacking()).padding;
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
