//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import BritefuryJ.LSpace.PageController;

public class BoundCommand
{
	private Command command;
	private Object binding;
	

	protected  BoundCommand(Command command, Object binding)
	{
		this.command = command;
		this.binding = binding;
	}

	
	
	public Command getCommand()
	{
		return command;
	}


	protected void execute(PageController pageController)
	{
		command.action.commandAction( binding, pageController );
	}
}
