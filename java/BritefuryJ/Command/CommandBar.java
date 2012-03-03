//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import BritefuryJ.IncrementalView.IncrementalViewInComponent;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.Browser.BrowserPage;
import BritefuryJ.LSpace.Input.Keyboard.Keyboard;
import BritefuryJ.LSpace.Input.Keyboard.KeyboardInteractor;

public class CommandBar
{
	KeyboardInteractor presComponentSwitchInteractor = new KeyboardInteractor()
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
				commandBarArea.setVisible( true );
				view.getComponent().grabFocus();
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
			return event.getKeyCode() == KeyEvent.VK_ESCAPE;
		}

		@Override
		public boolean keyReleased(Keyboard keyboard, KeyEvent event)
		{
			if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
			{
				presentation.grabFocus();
				commandBarArea.setVisible( false );
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
			commandBarArea.setVisible( false );
		}
	};


	
	private PresentationComponent presentation;
	private JPanel commandBarArea;
	
	private IncrementalViewInComponent view;
	
	private AbstractCommandConsole console;
	
	
	public CommandBar(PresentationComponent presentation, AbstractCommandConsole console, PageController pageController)
	{
		this.presentation = presentation;
		this.console = console;
		
		
		console.setListener( consoleListener );

		
		view = new IncrementalViewInComponent( console.getSubject(), console.getBrowserContext(), null, pageController );


		JPanel commandBarBorder = new JPanel( new BorderLayout() );
		commandBarBorder.add( view.getComponent(), BorderLayout.CENTER );
		commandBarBorder.setBorder( BorderFactory.createLineBorder( new Color( 0.65f, 0.65f, 0.65f ), 1 ) );
		
		commandBarArea = new JPanel( new BorderLayout( 5, 0 ) );
		commandBarArea.add( commandBarBorder, BorderLayout.CENTER );
		commandBarArea.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		commandBarArea.setVisible( false );
		
		
		presComponentSwitchInteractor.addToKeyboard( presentation.getRootElement().getKeyboard() );
		commandBarSwitchInteractor.addToKeyboard( view.getComponent().getRootElement().getKeyboard() );
	}
	
	
	public JComponent getComponent()
	{
		return commandBarArea;
	}
	
	
	public void setPage(BrowserPage page)
	{
		console.setPage( page );
	}
}
