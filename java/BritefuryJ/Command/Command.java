//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.Browser.Location;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.Shortcut.Shortcut;

public class Command
{
	public interface CommandAction
	{
		public void commandAction(Object context, PageController pageController);
	}
	
	
	private static class HyperlinkAction implements CommandAction
	{
		private Location target;
		
		public HyperlinkAction(Location target)
		{
			this.target = target;
		}

		
		@Override
		public void commandAction(Object context, PageController pageController)
		{
			pageController.openLocation( target, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
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
	
	

	public Command(CommandName name, Location targetLocation, Shortcut shortcut)
	{
		this.name = name;
		this.action = new HyperlinkAction( targetLocation );
		this.shortcut = shortcut;
	}
	
	public Command(String annotatedName, Location targetLocation, Shortcut shortcut)
	{
		this( new CommandName( annotatedName ), targetLocation, shortcut );
	}
	
	
	public Command(CommandName name, Location targetLocation)
	{
		this.name = name;
		this.action = new HyperlinkAction( targetLocation );
	}
	
	public Command(String annotatedName, Location targetLocation)
	{
		this( new CommandName( annotatedName ), targetLocation );
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
