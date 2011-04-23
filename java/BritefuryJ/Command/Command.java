//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

public class Command
{
	public interface CommandAction
	{
		void commandAction(Command command);
	}
	
	private String charSequence;
	private String name;
	private int charIndices[];
	private CommandAction action;
	
	
	public Command(String charSequence, String name, CommandAction action)
	{
		this.charSequence = charSequence;
		this.name = name;
		charIndices = computeIndices( charSequence, name );
		this.action = action;
	}
	
	
	public String getCharSequence()
	{
		return charSequence;
	}

	public String getName()
	{
		return name;
	}
	
	public int[] getCharIndices()
	{
		return charIndices;
	}
	
	
	protected void execute()
	{
		action.commandAction( this );
	}
	
	
	
	private static int[] computeIndices(String charSequence, String name)
	{
		int currentIndex = 0;
		int indices[] = new int[charSequence.length()];
		int j = 0;
		for (int i = 0; i < charSequence.length(); i++)
		{
			char c = charSequence.charAt( i );
			
			int index = name.indexOf( c, currentIndex );
			if ( index == -1 )
			{
				indices = null;
				break;
			}
			else
			{
				indices[j++] = index;
				currentIndex = index + 1;
			}
		}
		
		return indices;
	}
}
