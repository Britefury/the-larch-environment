//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.util.Stack;

import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;

public class PointerClickInteractor extends PointerInteractor
{
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getLastElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						if ( clickInt.testClickEvent( element, event ) )
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getLastElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						if ( clickInt.testClickEvent( element, event ) )
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getLastElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( ClickElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ClickElementInteractor clickInt = (ClickElementInteractor)interactor;
						if ( clickInt.testClickEvent( element, event ) )
						{
							boolean bHandled = clickInt.buttonClicked( element, event );
							if ( bHandled )
							{
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
}
