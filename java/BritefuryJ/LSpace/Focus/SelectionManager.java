//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Focus;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.TextSelectionPoint;

public class SelectionManager
{
	private SelectionPoint initialPoint = null;
	private LSRootElement rootElement;
	
	
	public SelectionManager(LSRootElement rootElement)
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
		if ( to != null  &&  to.isValid() )
		{
			if ( from != null  &&  from.isValid() )
			{
				initialPoint = from;
				setSelection( initialPoint, to );
			}
			else
			{
				moveSelection( to );
			}
		}
		else
		{
			if ( from != null  &&  from.isValid() )
			{
				moveSelection( from );
			}
			else
			{
				initialPoint = null;
				rootElement.setSelection( null );
			}
		}
	}
	
	public void dragSelection(SelectionPoint to)
	{
		setSelection( initialPoint, to );
	}
	

	
	
	public void selectElement(LSElement element)
	{
		Marker start = Marker.atStartOf( element, false );
		Marker end = Marker.atEndOf( element, false );
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
