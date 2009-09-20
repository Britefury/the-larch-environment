//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.util.ArrayList;

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
import BritefuryJ.DocPresent.DPWidget;

public abstract class Page
{
	private ArrayList<Browser> browsers = new ArrayList<Browser>();
	

	
	public void contentsModified()
	{
		for (Browser browser: browsers)
		{
			browser.onPageContentsModified( this );
		}
	}
	
	
	public abstract String getTitle();
	public abstract DPWidget getContentsElement();
	
	public CommandHistoryController getCommandHistoryController()
	{
		return null;
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
	}


	protected void addBrowser(Browser browser)
	{
		browsers.add( browser );
	}

	protected void removeBrowser(Browser browser)
	{
		browsers.remove( browser );
	}
}
