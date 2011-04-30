//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.geom.AffineTransform;
import java.util.Stack;

import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;

public class PointerPushInteractor extends PointerInteractor
{
	private PointerInputElement pressedElement;
	private AffineTransform pressedElementRootToLocalXform;
	private PushElementInteractor pressedInteractor;
	private int pressedButton;
	
	
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		Stack<PointerInputElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.peek();
			PointerButtonEvent elementSpaceEvent = events.peek();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( PushElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						PushElementInteractor pressInt = (PushElementInteractor)interactor;
						boolean bHandled = pressInt.buttonPress( element, elementSpaceEvent );
						if ( bHandled )
						{
							pressedElement = element;
							pressedElementRootToLocalXform = Pointer.rootToLocalTransform( elements );
							pressedInteractor = pressInt;
							pressedButton = event.getButton();
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

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		if ( pressedElement != null  &&  event.getButton() == pressedButton )
		{
			pressedInteractor.buttonRelease( pressedElement, (PointerButtonEvent)event.transformed( pressedElementRootToLocalXform ) );
			pressedElement = null;
			pressedElementRootToLocalXform = null;
			pressedInteractor = null;
			pressedButton = -1;
			return true;
		}
		return false;
	}

	
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		return pressedElement != null  &&  event.getButton() == pressedButton;
	}
}
