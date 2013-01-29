//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
		
		while ( !elements.isEmpty() )
		{
			LSElement element = elements.pop();
			
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
							if ( clickInt.testClickEvent( element, event ) )
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
		}
		
		return false;
	}

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			LSElement element = elements.pop();
			
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
							if ( clickInt.testClickEvent( element, event ) )
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
		}
		
		return false;
	}

	
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			LSElement element = elements.pop();
			
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
							clickAccepted = clickInt.testClickEvent( element, event );
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( clickInt, "testClickEvent", e );
						}

						if ( clickAccepted )
						{
							try
							{
								if ( clickInt.buttonClicked( element, event ) )
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
		}
		
		return false;
	}
}
