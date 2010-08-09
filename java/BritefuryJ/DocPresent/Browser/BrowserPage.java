//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;

public abstract class BrowserPage
{
	public abstract String getTitle();
	public abstract DPElement getContentsElement();
	
	public CommandHistoryController getCommandHistoryController()
	{
		return null;
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
	}
	
	
	public PersistentStateStore storePersistentState()
	{
		return null;
	}
}
