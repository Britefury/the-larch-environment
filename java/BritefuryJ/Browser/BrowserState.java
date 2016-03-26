//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser;

import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Projection.SubjectPath;

class BrowserState
{
	private SubjectPath path;
	private PersistentState viewportState;
	private PersistentStateStore pageState;
	
	
	public BrowserState(SubjectPath path)
	{
		this.path = path;
		this.viewportState = new PersistentState();
	}
	
	public SubjectPath getSubjectPath()
	{
		return path;
	}

	public PersistentState getViewportState()
	{
		return viewportState;
	}
	
	
	public void setPagePersistentState(PersistentStateStore state)
	{
		pageState = state;
	}
	
	public PersistentStateStore getPagePersistentState()
	{
		return pageState;
	}
}
