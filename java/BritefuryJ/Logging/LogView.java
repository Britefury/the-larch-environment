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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;

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




	public Pres present(GSymFragmentView fragment, AttributeTable inheritedState)
	{
		stateListeners = PresentationStateListenerList.addListener( stateListeners, fragment );
		
		Pres entryElements[] = new Pres[visibleEntries.size()+1];
		entryElements[0] = titleStyle.applyTo( new StaticText( log.getTitle() ) ).pad( 0.0, 20.0 ).alignHCentre();
		int i = 1;
		for (LogEntry entry: visibleEntries)
		{
			entryElements[i++] = new InnerFragment( entry );
		}
		
		return boxStyle.applyTo( new VBox( entryElements ) ).alignHExpand();
	}



	static StyleSheet2 titleStyle = StyleSheet2.instance.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 28 );
	static StyleSheet2 boxStyle = StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 5.0 );
}