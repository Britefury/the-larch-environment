//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Logging;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

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




	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		stateListeners = PresentationStateListenerList.addListener( stateListeners, fragment );
		
		Pres entryElements[] = new Pres[visibleEntries.size()+1];
		entryElements[0] = titleStyle.applyTo( new Label( log.getTitle() ) ).pad( 0.0, 20.0 ).alignHCentre();
		int i = 1;
		for (LogEntry entry: visibleEntries)
		{
			entryElements[i++] = new InnerFragment( entry );
		}
		
		return boxStyle.applyTo( new Column( entryElements ) ).alignHExpand();
	}


	static StyleSheet titleStyle = StyleSheet.style( Primitive.fontFace.as( "Serif" ), Primitive.fontBold.as( true ), Primitive.fontSize.as( 28 ) );
	static StyleSheet boxStyle = StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) );
}