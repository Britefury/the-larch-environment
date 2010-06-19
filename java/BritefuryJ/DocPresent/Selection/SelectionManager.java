//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;

public class SelectionManager
{
	private boolean bMouseDragInProgress;
	private Marker initialMarkerMouse = null, initialMarkerCaret = null;
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
				initialMarkerCaret = null;
			}
			else
			{
				if ( selection.isEmpty() )
				{
					initialMarkerCaret = prevPos.copy();
				}
				
				setSelection( initialMarkerCaret, c.getMarker().copy() );
			}
		}
	}
	
	
	public void mouseSelectionReset()
	{
		initialMarkerMouse = null;
		bMouseDragInProgress = false;
	}
	
	public void mouseSelectionBegin(Marker pos)
	{
		selection.clear();
		bMouseDragInProgress = true;
		initialMarkerMouse = pos.copy();
	}
	
	public void mouseSelectionDrag(Marker pos)
	{
		if ( bMouseDragInProgress )
		{
			setSelection( initialMarkerMouse, pos.copy() );
		}
	}
	
	public void selectElement(DPElement element)
	{
		setSelection( element.markerAtStart(), element.markerAtEnd() );
	}
	
	
	
	private void setSelection(Marker markerA, Marker markerB)
	{
		if ( markerA.isValid()  &&  markerB.isValid()  &&  !markerA.equals( markerB ) )
		{
			DPRegion regionA = markerA.getElement().getRegion();
			if ( regionA != null )
			{
				DPContentLeafEditable elementB = markerB.getElement();
				DPRegion regionB = elementB.getRegion();
				
				if ( regionB != regionA )
				{
					int order = Marker.markerOrder( markerA, markerB );
					DPRegion.SharableSelectionFilter filter = regionA.sharableSelectionFilter();
					
					Marker markerBInSameRegion = null;
					if ( order == 1 )
					{
						DPContentLeafEditable leaf = (DPContentLeafEditable)elementB.getPreviousEditableLeaf( filter, filter );
						if ( leaf != null )
						{
							markerBInSameRegion = leaf.markerAtEnd();
						}
					}
					else
					{
						DPContentLeafEditable leaf = (DPContentLeafEditable)elementB.getNextEditableLeaf( filter, filter );
						if ( leaf != null )
						{
							markerBInSameRegion = leaf.markerAtStart();
						}
					}
					
					if ( markerBInSameRegion != null )
					{
						selection.setSelection( markerA, markerBInSameRegion );
					}
				}
				else
				{
					selection.setSelection( markerA, markerB );
				}
			}
			else
			{
				selection.clear();
			}
		}
		else
		{
			selection.clear();
		}
	}
	
	
	
	public boolean isMouseDragInProgress()
	{
		return bMouseDragInProgress;
	}
}
