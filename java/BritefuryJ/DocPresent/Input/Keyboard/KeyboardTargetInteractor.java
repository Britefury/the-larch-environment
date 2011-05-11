//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input.Keyboard;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.KeyElementInteractor;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Selection.SelectionManager;
import BritefuryJ.DocPresent.Selection.SelectionPoint;
import BritefuryJ.DocPresent.Target.Target;

public class KeyboardTargetInteractor extends KeyboardInteractor
{
	PresentationComponent.RootElement root;
	
	
	
	public KeyboardTargetInteractor(PresentationComponent.RootElement rootElement)
	{
		root = rootElement;
	}

	
	
	private Target getTarget()
	{
		return root.getTarget();
	}
	
	private Selection getSelection()
	{
		return root.getSelection();
	}
	
	private SelectionManager getSelectionManager()
	{
		return root.getSelectionManager();
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
				Selection selection = getSelection();
				if ( selection != null )
				{
					if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE  ||  event.getKeyCode() == KeyEvent.VK_DELETE )
					{
						root.deleteSelection();
						return true;
					}
				}

				Target target = getTarget();
				if ( target.isValid() )
				{
					if ( sendKeyPressEvent( event ) )
					{
						return true;
					}
					
					return target.onContentKeyPress( event );
				}
			}
		}
		
		return false;
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
				Target target = getTarget();
				if ( target.isValid() )
				{
					if ( sendKeyReleaseEvent( event ) )
					{
						return true;
					}

					return target.onContentKeyRelease( event );
				}
			}
		}
		
		return false;
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
			else if ( !bCtrl  &&  !bAlt)
			{
				Selection selection = getSelection();
				if ( selection != null )
				{
					if ( !Character.isISOControl( event.getKeyChar() )  ||  event.getKeyChar() == '\n' )
					{
						String str = String.valueOf( event.getKeyChar() );
						if ( str.length() > 0 )
						{
							root.replaceSelectionWithText( str );
							return true;
						}
					}
				}
				
				Target target = getTarget();
				if ( target.isValid() )
				{
					if ( sendKeyTypedEvent( event ) )
					{
						return true;
					}

					return target.onContentKeyTyped( event );
				}
				else
				{
					return false;
				}
			}
		}
		
		return false;
	}
	
	
	
	private boolean handleNavigationKeyPress(KeyEvent event)
	{
		if ( isNavigationKey( event ) )
		{
			Target target = getTarget();
			if ( target.isValid() )
			{
				SelectionPoint prevPoint = target.createSelectionPoint();
				
				if ( event.getKeyCode() == KeyEvent.VK_LEFT )
				{
					target.moveLeft();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_RIGHT )
				{
					target.moveRight();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_UP )
				{
					target.moveUp();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_DOWN )
				{
					target.moveDown();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_HOME )
				{
					target.moveToHome();
				}
				else if ( event.getKeyCode() == KeyEvent.VK_END )
				{
					target.moveToEnd();
				}
				
				root.setTarget( target );
				if ( ( getKeyModifiers( event ) & Modifier.SHIFT ) != 0 )
				{
					getSelectionManager().dragSelection( prevPoint, target.createSelectionPoint() );
				}
				else
				{
					getSelectionManager().moveSelection( target.createSelectionPoint() );
				}
				
				target.ensureVisible();
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
		Target target = getTarget();
		DPElement element = target.getKeyboardInputElement();
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
		Target target = getTarget();
		DPElement element = target.getKeyboardInputElement();
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
		Target target = getTarget();
		DPElement element = target.getKeyboardInputElement();
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
