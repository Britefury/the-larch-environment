//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresentationStateListenerList;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class LogView implements Presentable
{
	private Log log;
	private LogFilterFn filterFn;
	private List<LogEntry> visibleEntries = new ArrayList<LogEntry>();
	private PresentationStateListenerList stateListeners = null;
	
	
	public LogView(Log log)
	{
		this( log, null );
	}
	
	public LogView(Log log, LogFilterFn filterFn)
	{
		this.log = log;
		this.log.registerView( this );
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
		stateListeners = PresentationStateListenerList.onPresentationStateChanged( stateListeners, this );
	}




	public DPElement present(GSymFragmentViewContext ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		stateListeners = PresentationStateListenerList.addListener( stateListeners, ctx );
		
		DPElement entryElements[] = new DPElement[visibleEntries.size()+1];
		entryElements[0] = titleStyle.staticText( log.getTitle() ).pad( 0.0, 20.0 ).alignHCentre();
		int i = 1;
		for (LogEntry entry: visibleEntries)
		{
			entryElements[i++] = ctx.presentFragment( entry, PrimitiveStyleSheet.instance );
		}
		
		return PrimitiveStyleSheet.instance.withVBoxSpacing( 5.0 ).vbox( entryElements ).alignHExpand();
	}



	static PrimitiveStyleSheet titleStyle = PrimitiveStyleSheet.instance.withFontFace( "Serif" ).withFontBold( true ).withFontSize( 28 );
}