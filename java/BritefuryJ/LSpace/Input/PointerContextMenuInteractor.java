//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.List;
import java.util.Stack;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;

public class PointerContextMenuInteractor extends PointerInteractor
{
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		int button = event.getButton(), mods = pointer.getModifiers();
		if ( button == 3  &&  ( mods & Modifier.KEYS_MASK  ) == 0  ||
				button == 1  &&  ( mods & Modifier.KEYS_MASK ) == 0  &&  ( mods & Modifier.META ) != 0 )		// Detect Apple-click on Mac
		{
			VPopupMenu menu = new VPopupMenu();
			
			LSElement menuElement = handleContextButton( pointer, menu );
			
			if ( !menu.isEmpty() )
			{
				menuElement = menuElement != null  ?  menuElement  :  pointer.getComponent().getRootElement();
				menu.popupMenuAtMousePosition( menuElement );
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
	
	
	private static LSElement handleContextButton(Pointer pointer, PopupMenu menu)
	{
		// We go to the trouble of determining the inner-most element that had a context menu interactor attached
		// so that a valid fragment context can be obtained by the popup element
		LSElement menuElement = null;
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			boolean bElementHandled = false;
			LSElement element = elements.pop();
			
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( ContextMenuElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						if ( menuElement == null )
						{
							menuElement = element;
						}

						ContextMenuElementInteractor menuInt = (ContextMenuElementInteractor)interactor;
						boolean bHandled = false;
						try
						{
							bHandled = menuInt.contextMenu( element, menu );
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( menuInt, "contextMenu", e );
						}

						bElementHandled = bElementHandled || bHandled;
					}
				}
			}
			
			if ( bElementHandled )
			{
				break;
			}
		}

		return menuElement;
	}
}
