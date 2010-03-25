//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class Log
{
	protected List<LogEntry> logEntries = new ArrayList<LogEntry>();
	private WeakHashMap<LogView, Object> views = new WeakHashMap<LogView, Object>();
	private String title;
	
	
	public Log(String title)
	{
		this.title = title;
	}
	
	
	public String getTitle()
	{
		return title;
	}
	
	
	public void log(LogEntry entry)
	{
		logEntries.add( entry );
		for (LogView view: views.keySet())
		{
			view.notifyLogEntryAdded( entry );
		}
	}
	
	
	
	protected void registerView(LogView view)
	{
		views.put( view, null );
	}
}
