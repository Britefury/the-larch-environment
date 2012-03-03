//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.Stack;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;

public class PointerContextMenuInteractor extends PointerInteractor
{
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		if ( event.getButton() == 3  &&  ( pointer.getModifiers() & Modifier.KEYS_MASK  ) ==  0 )
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
		return event.getButton() == 3  &&  ( pointer.getModifiers() & Modifier.KEYS_MASK  ) ==  0;
	}
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		return event.getButton() == 3  &&  ( pointer.getModifiers() & Modifier.KEYS_MASK  ) ==  0;
	}
	
	
	private static void handleContextButton(Pointer pointer, PopupMenu menu)
	{
		Stack<PointerInputElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		
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
