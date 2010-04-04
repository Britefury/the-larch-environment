//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.ObjectView.ObjectViewContextList;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class LogView implements Presentable, ObjectViewContextList.ContextListListener
{
	private Log log;
	private LogFilterFn filterFn;
	private List<LogEntry> visibleEntries = new ArrayList<LogEntry>();
	private ObjectViewContextList objectViewContexts = new ObjectViewContextList();
	
	
	public LogView(Log log)
	{
		this( log, null );
	}
	
	public LogView(Log log, LogFilterFn filterFn)
	{
		this.log = log;
		this.filterFn = filterFn;
		
		for (LogEntry entry: log.logEntries)
		{
			if ( filterFn == null  ||  filterFn.test( entry ) )
			{
				visibleEntries.add( entry );
			}
		}
	}
	
	
	public Log getLog()
	{
		return log;
	}
	
	
	
	
	protected void notifyLogEntryAdded(LogEntry entry)
	{
		if ( filterFn == null  ||  filterFn.test( entry ) )
		{
			visibleEntries.add( entry );
			onModified();
		}
	}
	
	
	private void onModified()
	{
		if ( objectViewContexts != null )
		{
			objectViewContexts.queueRefresh();
		}
	}




	public DPElement present(GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
	{
		if ( objectViewContexts == null )
		{
			objectViewContexts = new ObjectViewContextList();
		}
		objectViewContexts.addContext( ctx );
		
		DPElement entryElements[] = new DPElement[visibleEntries.size()+1];
		entryElements[0] = titleStyle.staticText( log.getTitle() ).pad( 0.0, 20.0 ).alignHCentre();
		int i = 1;
		for (LogEntry entry: visibleEntries)
		{
			entryElements[i++] = ctx.presentFragment( entry, PrimitiveStyleSheet.instance );
		}
		
		return PrimitiveStyleSheet.instance.vbox( Arrays.asList( entryElements ) );
	}



	public void onObjectViewContextListEmpty()
	{
		objectViewContexts = null;
	}
	
	
	static PrimitiveStyleSheet titleStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Serif", Font.BOLD, 28 ) );
}