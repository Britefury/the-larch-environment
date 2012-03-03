//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Browser;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.ChangeHistory.ChangeHistoryController;
import BritefuryJ.ChangeHistory.ChangeHistoryListener;
import BritefuryJ.Command.BoundCommandSet;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Pres;

public abstract class BrowserPage
{
	public abstract String getTitle();
	public abstract Pres getContentsPres();
	
	public ChangeHistoryController getChangeHistoryController()
	{
		return null;
	}
	
	public void setChangeHistoryListener(ChangeHistoryListener listener)
	{
	}
	
	
	public PersistentStateStore storePersistentState()
	{
		return null;
	}


	public List<BoundCommandSet> getBoundCommandSets()
	{
		return Arrays.asList();
	}
}
