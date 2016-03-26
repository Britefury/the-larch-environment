//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
import BritefuryJ.Util.Platform;

public class KeyboardTargetInteractor extends KeyboardInteractor
{
	private static final int NAV_NONE = -1;
	private static final int NAV_LEFT = 1;
	private static final int NAV_RIGHT = 2;
	private static final int NAV_UP = 3;
	private static final int NAV_DOWN = 4;
	private static final int NAV_HOME = 5;
	private static final int NAV_END = 6;


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
		if (getNavigationOperation(event) != NAV_NONE)
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
		int keyMods = Modifier.maskKeyModifiers(modifiers);
		boolean bCtrl = (keyMods  &  Modifier.CTRL) != 0;

		boolean bCmd;
		if (Platform.getPlatform() == Platform.MAC) {
			bCmd = (keyMods & Modifier.META) != 0;
		}
		else {
			bCmd = (keyMods & Modifier.ALT) != 0;
		}

		if (getNavigationOperation(event) != NAV_NONE)
		{
			return true;
		}
		else
		{
			if ( isModifierKey( event ) )
			{
				return false;
			}
			else if ( !bCtrl  &&  !bCmd)
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
        	int navOp = getNavigationOperation(event);
		if (navOp != NAV_NONE)
		{
			Target target = getTarget();
			if ( target.isValid() )
			{
				SelectionPoint prevPoint = target.createSelectionPoint();

				if (navOp == NAV_LEFT)
				{
					target.moveLeft();
				}
				else if (navOp == NAV_RIGHT)
				{
					target.moveRight();
				}
				else if (navOp == NAV_UP)
				{
					target.moveUp();
				}
				else if (navOp == NAV_DOWN)
				{
					target.moveDown();
				}
				else if (navOp == NAV_HOME)
				{
					target.moveToHome();
				}
				else if (navOp == NAV_END)
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


	private static int getNavigationOperation(KeyEvent event)
	{
		int modifiers = Modifier.getKeyModifiersFromEvent( event );
		int keyMods = Modifier.maskKeyModifiers(modifiers) & ~Modifier.SHIFT;   // Shift used for selecting ranges, so ignore
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT && keyMods == 0)
		{
			return NAV_LEFT;
		}
		else if (keyCode == KeyEvent.VK_RIGHT && keyMods == 0)
		{
			return NAV_RIGHT;
		}
		else if (keyCode == KeyEvent.VK_UP && keyMods == 0)
		{
			return NAV_UP;
		}
		else if (keyCode == KeyEvent.VK_DOWN && keyMods == 0)
		{
			return NAV_DOWN;
		}
		else if (keyCode == KeyEvent.VK_HOME && keyMods == 0  ||
				keyCode == KeyEvent.VK_LEFT && keyMods == Modifier.META && Platform.getPlatform() == Platform.MAC)
		{
			return NAV_HOME;
		}
		else if (keyCode == KeyEvent.VK_END && keyMods == 0  ||
				keyCode == KeyEvent.VK_RIGHT && keyMods == Modifier.META && Platform.getPlatform() == Platform.MAC)
		{
			return NAV_END;
		}
		return NAV_NONE;
	}

	private static boolean isModifierKey(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		return keyCode == KeyEvent.VK_CONTROL  ||  keyCode == KeyEvent.VK_SHIFT  ||  keyCode == KeyEvent.VK_ALT  ||  keyCode == KeyEvent.VK_ALT_GRAPH;
	}
}
