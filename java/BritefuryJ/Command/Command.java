//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Graphics.SolidBorder;

public class Command
{
	public interface CommandAction
	{
		public void commandAction(Object context);
	}
	
	private CommandName name;
	protected CommandAction action;
	protected ArrayList<Shortcut> shortcuts = new ArrayList<Shortcut>();
	
	
	public Command(CommandName name, CommandAction action, List<Shortcut> shortcuts)
	{
		this.name = name;
		this.action = action;
		this.shortcuts.addAll( shortcuts );
	}
	
	public Command(String charSeq, String name, CommandAction action, List<Shortcut> shortcuts)
	{
		this( new CommandName( charSeq, name ), action, shortcuts );
	}
	
	public Command(String annotatedName, CommandAction action, List<Shortcut> shortcuts)
	{
		this( new CommandName( annotatedName ), action, shortcuts );
	}
	
	
	public Command(CommandName name, CommandAction action, Shortcut shortcut)
	{
		this.name = name;
		this.action = action;
		this.shortcuts.add( shortcut );
	}
	
	public Command(String charSeq, String name, CommandAction action, Shortcut shortcut)
	{
		this( new CommandName( charSeq, name ), action, shortcut );
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
	
	public Command(String charSeq, String name, CommandAction action)
	{
		this( new CommandName( charSeq, name ), action );
	}
	
	public Command(String annotatedName, CommandAction action)
	{
		this( new CommandName( annotatedName ), action );
	}
	
	

	public CommandName getName()
	{
		return name;
	}
	
	
	public List<Shortcut> getShortcuts()
	{
		return shortcuts;
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
