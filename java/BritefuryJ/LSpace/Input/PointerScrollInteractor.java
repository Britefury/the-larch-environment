//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.List;
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
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( ScrollElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ScrollElementInteractor scrollInt = (ScrollElementInteractor)interactor;
						try
						{
							if ( scrollInt.scroll( element, elementSpaceEvent ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( scrollInt, "scroll", e );
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
