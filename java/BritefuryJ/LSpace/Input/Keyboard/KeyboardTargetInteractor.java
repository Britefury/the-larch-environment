//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input.Keyboard;

import java.awt.event.KeyEvent;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.SelectionManager;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;

public class KeyboardTargetInteractor extends KeyboardInteractor
{
	LSRootElement root;
	
	
	
	public KeyboardTargetInteractor(LSRootElement rootElement)
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
				if ( selection != null  &&  selection.isEditable() )
				{
					if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE  ||  event.getKeyCode() == KeyEvent.VK_DELETE )
					{
						selection.deleteContents( getTarget() );
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
		int modifiers = Modifier.getKeyModifiersFromEvent( event );
		
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
				if ( selection != null  &&  selection.isEditable() )
				{
					if ( !Character.isISOControl( event.getKeyChar() )  ||  event.getKeyChar() == '\n' )
					{
						String str = String.valueOf( event.getKeyChar() );
						if ( str.length() > 0 )
						{
							selection.replaceContentsWithText( str, getTarget() );
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
				if ( ( Modifier.getKeyModifiersFromEvent( event ) & Modifier.SHIFT ) != 0 )
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
		LSElement element = target.getKeyboardInputElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						try
						{
							if ( keyInt.keyPressed( element, event ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( keyInt, "keyPressed", e );
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
		LSElement element = target.getKeyboardInputElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						try
						{
							if ( keyInt.keyReleased( element, event ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( keyInt, "keyReleased", e );
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
		LSElement element = target.getKeyboardInputElement();
		while ( element != null )
		{
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( KeyElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						KeyElementInteractor keyInt = (KeyElementInteractor)interactor;
						try
						{
							if ( keyInt.keyTyped( element, event ) )
							{
								return true;
							}
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( keyInt, "keyTyped", e );
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
		int modifiers = Modifier.getKeyModifiersFromEvent( event );
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
}
