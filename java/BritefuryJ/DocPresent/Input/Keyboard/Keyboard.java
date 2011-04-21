//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input.Keyboard;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.Utils.PriorityList;

public class Keyboard
{
	private static int TARGET_INTERACTOR_PRIORITY = -100;

	
	
	protected PriorityList<KeyboardInteractor> interactors = new PriorityList<KeyboardInteractor>();



	public Keyboard(PresentationComponent.RootElement rootElement)
	{
		interactors.add( TARGET_INTERACTOR_PRIORITY, new KeyboardTargetInteractor( rootElement ) );
	}



	protected void addInteractor(KeyboardInteractor interactor)
	{
		interactors.add( interactor );
	}
	
	protected void addInteractor(int priority, KeyboardInteractor interactor)
	{
		interactors.add( priority, interactor );
	}
	
	protected void removeInteractor(KeyboardInteractor interactor)
	{
		interactors.remove( interactor );
	}
	
	protected void interactorGrab(KeyboardInteractor interactor)
	{
		interactors.grab( interactor );
	}
	
	protected void interactorUngrab(KeyboardInteractor interactor)
	{
		interactors.ungrab( interactor );
	}
	


	public boolean keyPressed(KeyEvent event)
	{
		for (KeyboardInteractor interactor: interactors)
		{
			if ( interactor.keyPressed( this, event ) )
			{
				return true;
			}
		}

		return false;
	}


	public boolean keyReleased(KeyEvent event)
	{
		for (KeyboardInteractor interactor: interactors)
		{
			if ( interactor.keyReleased( this, event ) )
			{
				return true;
			}
		}

		return false;
	}


	public boolean keyTyped(KeyEvent event)
	{
		for (KeyboardInteractor interactor: interactors)
		{
			if ( interactor.keyTyped( this, event ) )
			{
				return true;
			}
		}

		return false;
	}
}

