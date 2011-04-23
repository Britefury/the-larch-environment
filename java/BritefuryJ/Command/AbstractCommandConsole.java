//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;

public abstract class AbstractCommandConsole
{
	private CommandConsoleListener listener = null;
	
	
	
	public void setListener(CommandConsoleListener listener)
	{
		this.listener = listener;
	}
	
	
	
	protected void notifyFinished()
	{
		if ( listener != null )
		{
			listener.finished( this );
		}
	}

	
	public abstract Subject getSubject();
	public abstract ProjectiveBrowserContext getBrowserContext();
}
