//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Marker.Marker;

public class SelectionManager
{
	private SelectionPoint initialPoint = null;
	private PresentationComponent.RootElement rootElement;
	
	
	public SelectionManager(PresentationComponent.RootElement rootElement)
	{
		this.rootElement = rootElement;
	}
	
	
	
	public void moveSelection(SelectionPoint point)
	{
		initialPoint = point;
		setSelection( point, point );
	}
	
	public void dragSelection(SelectionPoint from, SelectionPoint to)
	{
		if ( initialPoint == null  ||  !initialPoint.isValid() )
		{
			if ( from != null  &&  from.isValid() )
			{
				initialPoint = from;
			}
			else
			{
				initialPoint = null;
			}
		}
		setSelection( initialPoint, to );
	}
	
	public void dragSelection(SelectionPoint to)
	{
		setSelection( initialPoint, to );
	}
	

	
	
	public void selectElement(DPElement element)
	{
		Marker start = element.markerAtStart();
		Marker end = element.markerAtEnd();
		if ( start != null  &&  start.isValid()  &&  end != null  &&  end.isValid() )
		{
			initialPoint = new TextSelectionPoint( start );

			setSelection( initialPoint, new TextSelectionPoint( end ) );
		}
		else
		{
			initialPoint = null;
		}
	}
	
	
	
	private void setSelection(SelectionPoint posA, SelectionPoint posB)
	{
		if ( posA != null  &&  posB != null )
		{
			Selection selection = posA.createSelectionTo( posB );
			rootElement.setSelection( selection );
		}
	}
}
