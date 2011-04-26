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

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.DocPresent.Input.Keyboard.Keyboard;
import BritefuryJ.DocPresent.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.Pres.Primitive.Column;

public class CommandBar
{
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
				commandBarArea.setVisible( true );
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
		@Override
		public void finished(AbstractCommandConsole commandConsole)
		{
			presentation.grabFocus();
			commandBarArea.setVisible( false );
		}
	};


	
	private PresentationComponent presentation;
	private JPanel commandBarBorder, commandBarArea;
	
	private PresentationComponent commandBarComponent;
	private IncrementalView view;
	
	private AbstractCommandConsole console;
	
	
	public CommandBar(PresentationComponent presentation, AbstractCommandConsole console, PageController pageController)
	{
		this.presentation = presentation;
		this.console = console;
		
		
		commandBarComponent = new PresentationComponent();
		commandBarComponent.setPageController( pageController );
		
		
		commandBarBorder = new JPanel( new BorderLayout() );
		commandBarBorder.add( commandBarComponent, BorderLayout.CENTER );
		commandBarBorder.setBorder( BorderFactory.createLineBorder( new Color( 0.65f, 0.65f, 0.65f ), 1 ) );
		
		commandBarArea = new JPanel( new BorderLayout( 5, 0 ) );
		commandBarArea.add( commandBarBorder, BorderLayout.CENTER );
		commandBarArea.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		commandBarArea.setVisible( false );
		
		
		view = new IncrementalView( console.getSubject(), console.getBrowserContext(), null );
		console.setListener( consoleListener );

		DPElement column = new Column( new Object[] { view.getViewPres() } ).alignHExpand().alignVExpand().present();
		commandBarComponent.getRootElement().setChild( column );

		presComponentSwitchInteractor.addToKeyboard( presentation.getRootElement().getKeyboard() );
		commandBarSwitchInteractor.addToKeyboard( commandBarComponent.getRootElement().getKeyboard() );
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
