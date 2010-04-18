//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.Arrays;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.Hyperlink;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.Logging.Log;
import BritefuryJ.Logging.LogView;

public class GSymViewPage extends Page
{
	private DPElement element;
	private Hyperlink logLink;
	private DPElement pageElement;
	private String title;
	private Log log;
	private CommandHistory commandHistory;
	
	
	
	public GSymViewPage(DPElement element, String title, final GSymBrowserContext browserContext, CommandHistory commandHistory)
	{
		this.element = element;
		this.title = title;
		this.commandHistory = commandHistory;
		
		Hyperlink.LinkListener listener = new Hyperlink.LinkListener()
		{
			@Override
			public boolean onLinkClicked(Hyperlink link, PointerButtonEvent event)
			{
				initLog();
				LogView view = new LogView( log );
				Location location = browserContext.getLocationForObject( view );
				link.getElement().getRootElement().getPageController().openLocation( location, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
				return true;
			}
		};
		
		logLink = ControlsStyleSheet.instance.link( "Page log", listener );
		pageElement = PrimitiveStyleSheet.instance.vbox( Arrays.asList( new DPElement[] { this.element.alignHExpand(), logLink.getElement().pad( 10, 10 ).alignHRight() } ) );
	}
	
	
	
	public DPElement getContentsElement()
	{
		return pageElement;
	}
	
	public String getTitle()
	{
		return title;
	}

	public Log getLog()
	{
		return log;
	}

	
	private Log initLog()
	{
		if ( log == null )
		{
			log = new Log( "Page log" );
		}
		return log;
	}
	
	
	public CommandHistoryController getCommandHistoryController()
	{
		return commandHistory;
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
		if ( commandHistory != null )
		{
			commandHistory.setCommandHistoryListener( listener );
		}
	}
}
