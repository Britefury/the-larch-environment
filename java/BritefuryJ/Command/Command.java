//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.DocPresent.Border.SolidBorder;

public class Command
{
	public interface CommandAction
	{
		public void commandAction(Object context);
	}
	
	private CommandName name;
	protected CommandAction action;
	
	
	public Command(CommandName name, CommandAction action)
	{
		this.name = name;
		this.action = action;
	}
	
	public Command(String charSeq, String name, CommandAction action)
	{
		this( new CommandName( charSeq, name ), action );
	}
	
	

	public CommandName getName()
	{
		return name;
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
