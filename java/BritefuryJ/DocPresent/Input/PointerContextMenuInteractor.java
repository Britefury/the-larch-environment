//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.util.Stack;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.ContextMenuElementInteractor;

public class PointerContextMenuInteractor extends PointerInteractor
{
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		int button = event.getButton();
		int modifiers = pointer.getModifiers();
		if ( button == 3  &&  modifiers == Modifier.BUTTON3 )
		{
			VPopupMenu menu = new VPopupMenu();
			
			handleContextButton( pointer, menu );
			
			if ( !menu.isEmpty() )
			{
				menu.popupAtMousePosition( pointer.getComponent().getRootElement() );
				return true;
			}
		}

		return false;
	}
	
	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		return false;
	}
	
	
	private static void handleContextButton(Pointer pointer, PopupMenu menu)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getFirstElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			boolean bElementHandled = false;
			PointerInputElement element = elements.pop();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( ContextMenuElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						ContextMenuElementInteractor menuInt = (ContextMenuElementInteractor)interactor;
						boolean bHandled = menuInt.contextMenu( element, menu );
						bElementHandled = bElementHandled || bHandled;
					}
				}
			}
			
			if ( bElementHandled )
			{
				break;
			}
		}
	}
}
