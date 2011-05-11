//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Selection;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Marker.Marker;

public class TextSelectionPoint extends SelectionPoint
{
	private Marker marker;
	
	
	public TextSelectionPoint(Marker marker)
	{
		this.marker = marker;
	}
	
	
	@Override
	public boolean isValid()
	{
		return marker.isValid();
	}
	
	@Override
	public Selection createSelectionTo(SelectionPoint target)
	{
		if ( target instanceof TextSelectionPoint )
		{
			Marker markerA = marker;
			Marker markerB = ((TextSelectionPoint)target).marker;
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
							return new TextSelection( elementB, markerA, markerBInSameRegion );
						}
					}
					else
					{
						return new TextSelection( elementB, markerA, markerB );
					}
				}
			}
		}
		
		return null;
	}
}
