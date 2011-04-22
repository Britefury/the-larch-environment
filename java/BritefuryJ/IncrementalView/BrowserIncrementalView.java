//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalView;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.Projection.Subject;

public class BrowserIncrementalView extends IncrementalView
{
	private IncrementalViewPage page;

	
	
	
	public BrowserIncrementalView(Subject subject, ProjectiveBrowserContext browserContext, PersistentStateStore persistentState)
	{
		super( subject, browserContext, persistentState );

		DPElement column = new Column( new Object[] { region } ).present();

		page = new IncrementalViewPage( column.alignHExpand().alignVExpand(), subject.getTitle(), browserContext, subject.getChangeHistory(), this );
	}
	
	
	
	public BrowserPage getPage()
	{
		return page;
	}
	
}
