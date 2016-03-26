//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.Projection.Subject;
import BritefuryJ.Shortcut.Shortcut;

public class Command
{
	public interface CommandAction
	{
		public void commandAction(Object context, PageController pageController);
	}
	
	
	private static class HyperlinkAction implements CommandAction
	{
		private Subject target;
		
		public HyperlinkAction(Subject target)
		{
			this.target = target;
		}

		
		@Override
		public void commandAction(Object context, PageController pageController)
		{
			pageController.openSubject( target, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
		}
	}
	
	
	
	private CommandName name;
	protected CommandAction action;
	protected Shortcut shortcut;
	
	
	public Command(CommandName name, CommandAction action, Shortcut shortcut)
	{
		this.name = name;
		this.action = action;
		this.shortcut = shortcut;
	}
	
	public Command(String annotatedName, CommandAction action, Shortcut shortcut)
	{
		this( new CommandName( annotatedName ), action, shortcut );
	}
	
	
	public Command(CommandName name, CommandAction action)
	{
		this.name = name;
		this.action = action;
	}
	
	public Command(String annotatedName, CommandAction action)
	{
		this( new CommandName( annotatedName ), action );
	}
	
	

	public Command(CommandName name, Subject targetSubject, Shortcut shortcut)
	{
		this.name = name;
		this.action = new HyperlinkAction( targetSubject );
		this.shortcut = shortcut;
	}
	
	public Command(String annotatedName, Subject targetSubject, Shortcut shortcut)
	{
		this( new CommandName( annotatedName ), targetSubject, shortcut );
	}
	
	
	public Command(CommandName name, Subject targetSubject)
	{
		this.name = name;
		this.action = new HyperlinkAction( targetSubject );
	}
	
	public Command(String annotatedName, Subject targetSubject)
	{
		this( new CommandName( annotatedName ), targetSubject );
	}
	
	

	public CommandName getName()
	{
		return name;
	}
	
	
	public Shortcut getShortcut()
	{
		return shortcut;
	}
	
	
	public BoundCommand bindTo(Object binding)
	{
		return new BoundCommand( this, binding );
	}
	
	
	public static SolidBorder cmdBorder(Color borderColour, Color backgroundColour)
	{
		return new SolidBorder( 1.0, 2.0, 8.0, 8.0, borderColour, backgroundColour );
	}
}
