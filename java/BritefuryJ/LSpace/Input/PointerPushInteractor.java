//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Stack;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;

public class PointerPushInteractor extends PointerInteractor
{
	private LSElement pressedElement;
	private AffineTransform pressedElementRootToLocalXform;
	private PushElementInteractor pressedInteractor;
	private int pressedButton;
	
	
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
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( PushElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						PushElementInteractor pressInt = (PushElementInteractor)interactor;
						boolean bHandled = false;
						try
						{
							bHandled = pressInt.buttonPress( element, elementSpaceEvent );
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( pressInt, "buttonPress", e );
						}
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
			try
			{
				pressedInteractor.buttonRelease( pressedElement, (PointerButtonEvent)event.transformed( pressedElementRootToLocalXform ) );
			}
			catch (Throwable e)
			{
				pressedElement.notifyExceptionDuringElementInteractor( pressedInteractor, "buttonRelease", e );
			}
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
