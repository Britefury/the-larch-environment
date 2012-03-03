//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalView;

import java.util.List;

import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.ChangeHistoryController;
import BritefuryJ.ChangeHistory.ChangeHistoryListener;
import BritefuryJ.Command.BoundCommandSet;
import BritefuryJ.Controls.AbstractHyperlink;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.Browser.BrowserPage;
import BritefuryJ.LSpace.Browser.Location;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;

public class IncrementalViewPage extends BrowserPage
{
	private Pres pagePres;
	private String title;
	private ChangeHistory changeHistory;
	private BrowserIncrementalView view;
	private Subject subject;
	
	
	
	public IncrementalViewPage(Pres pres, String title, final ProjectiveBrowserContext browserContext, ChangeHistory changeHistory, BrowserIncrementalView view, Subject subject)
	{
		this.title = title;
		this.changeHistory = changeHistory;
		this.view = view;
		this.subject = subject;
		
		Hyperlink.LinkListener listener = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.AbstractHyperlinkControl link, PointerButtonClickedEvent event)
			{
				Log log = getLog();
				log.startRecording();
				LogView view = new LogView( log );
				Location location = browserContext.getLocationForObject( view );
				link.getElement().getRootElement().getPageController().openLocation( location, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
			}
		};

		AbstractHyperlink logLink = new Hyperlink( "Page log", listener );
//		pagePres = new Column( new Object[] { this.element.alignHExpand(), logLink.pad( 10, 10 ).alignHRight() } );
		pagePres = new Column( new Object[] { pres.alignHExpand().alignVExpand(), logLink.pad( 10, 10 ).alignHRight().alignVRefY() } );
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

	
	public ChangeHistoryController getChangeHistoryController()
	{
		return changeHistory;
	}
	
	public void setChangeHistoryListener(ChangeHistoryListener listener)
	{
		if ( changeHistory != null )
		{
			changeHistory.setChangeHistoryListener( listener );
		}
	}


	public PersistentStateStore storePersistentState()
	{
		return view.storePersistentState();
	}


	public List<BoundCommandSet> getBoundCommandSets()
	{
		return subject.getBoundCommandSets();
	}
}
