//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.Stack;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerScrollEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.ScrollElementInteractor;

public class PointerScrollInteractor extends PointerInteractor
{
	public boolean scroll(Pointer pointer, PointerScrollEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerScrollEvent> events = Pointer.eventStack( event, elements );
		
		while ( !elements.isEmpty() )
		{
			LSElement element = elements.peek();
			PointerScrollEvent elementSpaceEvent = events.peek();
			
			if ( element.isRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( ScrollElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ScrollElementInteractor scrollInt = (ScrollElementInteractor)interactor;
						if ( scrollInt.scroll( element, elementSpaceEvent ) )
						{
							return true;
						}
					}
				}
			}
			
			elements.pop();
			events.pop();
		}
		
		return false;
	}
}