//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.List;
import java.util.Stack;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;

public class PointerClickInteractor extends PointerInteractor
{
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );

		while ( !elements.isEmpty() )
		{
			LSElement element = elements.peek();
			PointerButtonEvent elementSpaceEvent = events.peek();
			
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						try
						{
							if ( clickInt.testClickEvent( element, elementSpaceEvent ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( clickInt, "testClickEvent", e );
						}
					}
				}
			}

			elements.pop();
			events.pop();
		}
		
		return false;
	}

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );

		while ( !elements.isEmpty() )
		{
			LSElement element = elements.peek();
			PointerButtonEvent elementSpaceEvent = events.peek();
			
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						try
						{
							if ( clickInt.testClickEvent( element, elementSpaceEvent ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( clickInt, "testClickEvent", e );
						}
					}
				}
			}

			elements.pop();
			events.pop();
		}
		
		return false;
	}

	
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonClickedEvent> events = Pointer.eventStack( event, elements );

		while ( !elements.isEmpty() )
		{
			LSElement element = elements.peek();
			PointerButtonClickedEvent elementSpaceEvent = events.peek();
			
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						boolean clickAccepted = false;
						try
						{
							clickAccepted = clickInt.testClickEvent( element, elementSpaceEvent );
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( clickInt, "testClickEvent", e );
						}

						if ( clickAccepted )
						{
							try
							{
								if ( clickInt.buttonClicked( element, elementSpaceEvent ) )
								{
									return true;
								}
							}
							catch (Throwable e)
							{
								element.notifyExceptionDuringElementInteractor( clickInt, "buttonClicked", e );
							}
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
