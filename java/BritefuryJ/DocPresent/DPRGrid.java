//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeGridRow;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRGrid;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;

public class DPRGrid extends DPContainerSequence
{
	private int numColumns;

	
	
	public DPRGrid()
	{
		this( TableStyleParams.defaultStyleParams);
	}
	
	public DPRGrid(TableStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeRGrid( this );
	}
	
	
	
	private void refreshSize()
	{
		numColumns = 0;
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		for (DPElement child: gridLayout.getLeaves())
		{
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				numColumns = Math.max( numColumns, rowLayout.getLeaves().size() );
			}
			else
			{
				numColumns = Math.max( numColumns, 1 );
			}
		}
	}
	
	
	public int width()
	{
		refreshSize();
		return numColumns;
	}
	
	
	



	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams) styleParams).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams) styleParams).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams) styleParams).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams) styleParams).getRowExpand();
	}
}
