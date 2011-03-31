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
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;

public class TextSelectionManager
{
	private boolean bMouseDragInProgress;
	private Marker initialMarker = null;
	private PresentationComponent.RootElement rootElement;
	
	
	public TextSelectionManager(PresentationComponent.RootElement rootElement)
	{
		this.rootElement = rootElement;
	}
	
	
	
	public void onCaretMove(Caret c, Marker prevPos, boolean bDragSelection)
	{
		if ( !bMouseDragInProgress )
		{
			if ( !bDragSelection )
			{
				clearTextSelection();
				initialMarker = null;
			}
			else
			{
				if ( !( rootElement.getSelection() instanceof TextSelection ) )
				{
					initialMarker = prevPos.copy();
				}
				
				setSelection( initialMarker, c.getMarker().copy() );
			}
		}
	}
	
	
	public void mouseSelectionReset()
	{
		bMouseDragInProgress = false;
	}
	
	public void mouseSelectionBegin(Marker pos)
	{
		clearTextSelection();
		bMouseDragInProgress = true;
		initialMarker = pos.copy();
	}
	
	public void mouseSelectionDrag(Marker pos)
	{
		if ( bMouseDragInProgress )
		{
			setSelection( initialMarker, pos.copy() );
		}
	}
	
	public void selectElement(DPElement element)
	{
		initialMarker = element.markerAtStart();
		setSelection( initialMarker, element.markerAtEnd() );
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
						DPContentLeafEditable leaf = (DPContentLeafEditable)elementB.getPreviousSelectableLeaf( filter, filter );
						if ( leaf != null )
						{
							markerBInSameRegion = leaf.markerAtEnd();
						}
					}
					else
					{
						DPContentLeafEditable leaf = (DPContentLeafEditable)elementB.getNextSelectableLeaf( filter, filter );
						if ( leaf != null )
						{
							markerBInSameRegion = leaf.markerAtStart();
						}
					}
					
					if ( markerBInSameRegion != null )
					{
						rootElement.setSelection( new TextSelection( rootElement, markerA, markerBInSameRegion ) );
					}
				}
				else
				{
					rootElement.setSelection( new TextSelection( rootElement, markerA, markerB ) );
				}
			}
			else
			{
				clearSelection();
			}
		}
		else
		{
			clearSelection();
		}
	}
	
	private void clearSelection()
	{
		rootElement.setSelection( null );
	}
	
	private void clearTextSelection()
	{
		if ( rootElement.getSelection() instanceof TextSelection )
		{
			rootElement.setSelection( null );
		}
	}
	
	
	
	public boolean isMouseDragInProgress()
	{
		return bMouseDragInProgress;
	}
}
