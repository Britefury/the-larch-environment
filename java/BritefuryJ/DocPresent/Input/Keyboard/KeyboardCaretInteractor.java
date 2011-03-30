//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input.Keyboard;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.KeyElementInteractor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelectionManager;

public class KeyboardCaretInteractor extends KeyboardInteractor
{
	private Caret caret;
	private TextSelectionManager selectionManager;
	
	
	
	public KeyboardCaretInteractor(Caret caret, TextSelectionManager selectionManager)
	{
		this.caret = caret;
		this.selectionManager = selectionManager;
	}




	public boolean keyPressed(Keyboard keyboard, KeyEvent event)
	{
		if ( handleNavigationKeyPress( event ) )
		{
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				return false;
			}
			else
			{
				if ( caret.isValid() )
				{
					if ( sendKeyPressEvent( event ) )
					{
						return true;
					}
					
					DPContentLeafEditable leaf = caret.getElement();
					if ( leaf.isEditable() )
					{
						leaf.onContentKeyPress( caret, event );
					}
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}


	public boolean keyReleased(Keyboard keyboard, KeyEvent event)
	{
		if ( isNavigationKey( event ) )
		{
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				return false;
			}
			else
			{
				if ( caret.isValid() )
				{
					if ( sendKeyReleaseEvent( event ) )
					{
						return true;
					}

					DPContentLeafEditable leaf = caret.getElement();
					if ( leaf.isEditable() )
					{
						leaf.onContentKeyRelease( caret, event );
					}
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}


	public boolean keyTyped(Keyboard keyboard, KeyEvent event)
	{
		int modifiers = getKeyModifiers( event );
		
		boolean bCtrl = ( modifiers & Modifier.KEYS_MASK )  ==  Modifier.CTRL;
		boolean bAlt = ( modifiers & Modifier.KEYS_MASK )  ==  Modifier.ALT;

		if ( isNavigationKey( event ) )
		{
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				return false;
			}
			else
			{
				if ( caret.isValid()  &&  !bCtrl  &&  !bAlt )
				{
					if ( sendKeyTypedEvent( event ) )
					{
						return true;
					}

					DPContentLeafEditable leaf = caret.getElement();
					if ( leaf.isEditable() )
					{
						leaf.onContentKeyTyped( caret, event );
					}
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}
	
	
	
	private boolean handleNavigationKeyPress(KeyEvent event)
	{
		int modifiers = getKeyModifiers( event );
		if ( isNavigationKey( event ) )
		{
			if ( caret.isValid() )
			{
				Marker prevPos = caret.getMarker().copy();
				if ( event.getKeyCode() == KeyEvent.VK_LEFT )
				{
					caret.moveLeft();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_RIGHT )
				{
					caret.moveRight();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_UP )
				{
					caret.moveUp();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_DOWN )
				{
					caret.moveDown();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_HOME )
				{
					caret.moveToHome();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_END )
				{
					caret.moveToEnd();
				}
				
				if ( !caret.getMarker().equals( prevPos ) )
				{
					selectionManager.onCaretMove( caret, prevPos, ( modifiers & Modifier.SHIFT ) != 0 );
				}
				
				caret.ensureVisible();
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	
	
	private boolean sendKeyPressEvent(KeyEvent event)
	{
		DPElement element = caret.getElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						if ( keyInt.keyPressed( element, event ) )
						{
							return true;
						}
					}
				}
			}
			
			element = element.getParent();
		}
		
		return false;
	}

	private boolean sendKeyReleaseEvent(KeyEvent event)
	{
		DPElement element = caret.getElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						if ( keyInt.keyReleased( element, event ) )
						{
							return true;
						}
					}
				}
			}
			
			element = element.getParent();
		}
		
		return false;
	}

	private boolean sendKeyTypedEvent(KeyEvent event)
	{
		DPElement element = caret.getElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						if ( keyInt.keyTyped( element, event ) )
						{
							return true;
						}
					}
				}
			}
			
			element = element.getParent();
		}
		
		return false;
	}




	private static boolean isNavigationKey(KeyEvent event)
	{
		int modifiers = getKeyModifiers( event );
		int keyMods = modifiers & Modifier.KEYS_MASK;
		if  ( keyMods == Modifier.SHIFT  ||  keyMods == 0 )
		{
			int keyCode = event.getKeyCode();
			return keyCode == KeyEvent.VK_LEFT  ||  keyCode == KeyEvent.VK_RIGHT  ||  keyCode == KeyEvent.VK_UP  ||  keyCode == KeyEvent.VK_DOWN  ||
				keyCode == KeyEvent.VK_HOME  ||  keyCode == KeyEvent.VK_END;
		}
		else
		{
			return false;
		}
	}
	
	private static boolean isModifierKey(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		return keyCode == KeyEvent.VK_CONTROL  ||  keyCode == KeyEvent.VK_SHIFT  ||  keyCode == KeyEvent.VK_ALT  ||  keyCode == KeyEvent.VK_ALT_GRAPH;
	}
	


	private static int getKeyModifiers(InputEvent e)
	{
		int modifiers = 0;
		
		if ( e.isControlDown() )
		{
			modifiers |= Modifier.CTRL;
		}
		
		if ( e.isShiftDown() )
		{
			modifiers |= Modifier.SHIFT;
		}
		
		if ( e.isAltDown() )
		{
			modifiers |= Modifier.ALT;
		}
		
		if ( e.isAltGraphDown() )
		{
			modifiers |= Modifier.ALT_GRAPH;
		}
		
		return modifiers;
	}
}
