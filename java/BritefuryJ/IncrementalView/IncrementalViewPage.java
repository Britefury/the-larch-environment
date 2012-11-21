//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.Browser.BrowserPage;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.BoundCommandSet;
import BritefuryJ.Command.Command;
import BritefuryJ.Command.Command.CommandAction;
import BritefuryJ.Command.CommandSet;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Projection.Subject;

public class IncrementalViewPage extends BrowserPage
{
	private Pres pagePres;
	private String title;
	private ChangeHistory changeHistory;
	private BrowserIncrementalView view;
	private Subject subject;
	
	
	private static CommandAction pageLogCmdAction = new CommandAction()
	{
		public void commandAction(Object context, PageController pageController)
		{
			IncrementalViewPage page = (IncrementalViewPage)context;
			page.showPageLog( pageController );
		}
	};
	
	private static Command pageLogCommand = new Command( "&Debug: show &page &log", pageLogCmdAction );
	
	private static CommandSet pageLogCmdSet = new CommandSet( "Larch.Debug.PageLog", Arrays.asList( new Command[] { pageLogCommand } ) );
	
	
	
	public IncrementalViewPage(Pres pres, BrowserIncrementalView view, Subject subject)
	{
		this.pagePres = pres;
		this.title = subject.getTitle();
		this.changeHistory = subject.getChangeHistory();
		this.view = view;
		this.subject = subject;
	}
	
	
	
	public Pres getContentsPres()
	{
		return pagePres;
	}
	
	public String getTitle()
	{
		return title;
	}

	public Log getLog()
	{
		return view.getLog();
	}

	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}


	public PersistentStateStore storePersistentState()
	{
		return view.storePersistentState();
	}


	public void buildBoundCommandSetList(List<BoundCommandSet> boundCommandSets)
	{
		subject.buildBoundCommandSetList( boundCommandSets );
		boundCommandSets.add( pageLogCmdSet.bindTo( this ) );
	}



	private void showPageLog(PageController pageController)
	{
		Log log = getLog();
		log.startRecording();
		LogView view = new LogView( log );
		Subject subject = DefaultPerspective.instance.objectSubject( view );
		pageController.openSubject( subject, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
	}
}
