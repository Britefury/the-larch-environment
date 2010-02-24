//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;

public class SelectionManager
{
	private boolean bMouseDragInProgress;
	private Marker initialMarker;
	private Selection selection;
	
	
	public SelectionManager(Selection selection)
	{
		this.selection = selection;
	}
	
	
	
	public void onCaretMove(Caret c, Marker prevPos, boolean bDragSelection)
	{
		if ( !bMouseDragInProgress )
		{
			if ( !bDragSelection )
			{
				selection.clear();
			}
			else
			{
				if ( initialMarker == null )
				{
					initialMarker = prevPos.copy();
				}
				
				selection.setRegion( initialMarker, c.getMarker().copy() );
			}
		}
	}
	
	
	public void mouseSelectionClear()
	{
		bMouseDragInProgress = false;
	}
	
	public void mouseSelectionBegin(Marker pos)
	{
		selection.clear();
		bMouseDragInProgress = true;
		initialMarker = pos.copy();
	}
	
	public void mouseSelectionDrag(Marker pos)
	{
		if ( bMouseDragInProgress )
		{
			selection.setRegion( initialMarker, pos.copy() );
		}
	}
	
	public void mouseSelectionEnd()
	{
		if ( bMouseDragInProgress )
		{
			initialMarker = null;
		}
		bMouseDragInProgress = false;
	}
	
	
	public boolean isMouseDragInProgress()
	{
		return bMouseDragInProgress;
	}
}