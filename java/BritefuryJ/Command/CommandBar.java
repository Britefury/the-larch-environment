//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Input.Keyboard.Keyboard;
import BritefuryJ.LSpace.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Projection.Subject;

public class CommandBar
{
	KeyboardInteractor commandBarSwitchInteractor = new KeyboardInteractor()
	{
		@Override
		public boolean keyPressed(Keyboard keyboard, KeyEvent event)
		{
			return event.getKeyCode() == KeyEvent.VK_ESCAPE;
		}

		@Override
		public boolean keyReleased(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				if ( popup == null )
				{
					double width = presentation.getRootElement().getAllocWidth();
					Pres space = new SpaceBin( width, 1.0, LSSpaceBin.SizeConstraint.FIXED, LSSpaceBin.SizeConstraint.LARGER, console ).alignHExpand();
					popup = space.popup( presentation.getRootElement(), Anchor.TOP_LEFT, Anchor.TOP_LEFT, false, true );
					commandBarSwitchInteractor.addToKeyboard( popup.getPresentationComponent().getRootElement().getKeyboard() );
					popup.getPresentationComponent().grabFocus();
				}
				else
				{
					presentation.grabFocus();
					popup.closePopup();
					popup = null;
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean keyTyped(Keyboard keyboard, KeyEvent event)
		{
			return false;
		}
	};
	private CommandConsoleListener consoleListener = new CommandConsoleListener()
	{
		public void finished(AbstractCommandConsole commandConsole)
		{
			presentation.grabFocus();
			if ( popup != null )
			{
				popup.closePopup();
				popup = null;
			}
		}
	};


	
	private PresentationComponent presentation;
	
	private AbstractCommandConsole console;
	
	private PresentationPopupWindow popup = null;
	
	
	public CommandBar(PresentationComponent presentation, AbstractCommandConsole console, PageController pageController)
	{
		this.presentation = presentation;
		this.console = console;
		
		
		console.setListener( consoleListener );

		
		commandBarSwitchInteractor.addToKeyboard( presentation.getRootElement().getKeyboard() );
	}
	
	
	public void pageChanged(Subject subject)
	{
		console.pageChanged( subject );
	}
}
