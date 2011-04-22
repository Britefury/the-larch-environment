//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Input.Keyboard.Keyboard;
import BritefuryJ.DocPresent.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.Pres.Primitive.Column;

public class CommandBar
{
	private PresentationComponent presentation;
	
	private PresentationComponent commandBarComponent;
	private IncrementalView view;
	
	
	KeyboardInteractor presComponentSwitchInteractor = new KeyboardInteractor()
	{
		@Override
		public boolean keyPressed(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				return true;
			}
			return false;
		}

		@Override
		public boolean keyReleased(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				commandBarComponent.grabFocus();
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


	KeyboardInteractor commandBarSwitchInteractor = new KeyboardInteractor()
	{
		@Override
		public boolean keyPressed(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				return true;
			}
			return false;
		}

		@Override
		public boolean keyReleased(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				presentation.grabFocus();
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


	
	public CommandBar(PresentationComponent presentation, CommandConsoleInterface console, PageController pageController)
	{
		this.presentation = presentation;
		
		
		commandBarComponent = new PresentationComponent();
		commandBarComponent.setPageController( pageController );
		
		view = new IncrementalView( console.getSubject(), console.getBrowserContext(), null );

		DPElement viewElement = view.getViewElement();
		DPElement column = new Column( new Object[] { viewElement } ).present();
		commandBarComponent.getRootElement().setChild( column.alignHExpand().alignVExpand() );

		presComponentSwitchInteractor.addToKeyboard( presentation.getRootElement().getKeyboard() );
		commandBarSwitchInteractor.addToKeyboard( commandBarComponent.getRootElement().getKeyboard() );
	}
	
	
	public PresentationComponent getComponent()
	{
		return commandBarComponent;
	}
}
