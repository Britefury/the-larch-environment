//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
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
