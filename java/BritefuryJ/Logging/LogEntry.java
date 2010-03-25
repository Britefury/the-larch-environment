//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Logging;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.GSym.ObjectView.ObjectViewContextList;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class LogEntry implements Presentable, ObjectViewContextList.ContextListListener
{
	private static final List<String> emptyTags = new ArrayList<String>();
	
	
	private List<String> tags;
	private ObjectViewContextList objectViewContexts = new ObjectViewContextList();
	
	
	
	public LogEntry()
	{
		tags = emptyTags;
	}
	
	public LogEntry(List<String> tags)
	{
		this.tags = tags;
	}
	
	
	
	public List<String> getTags()
	{
		return tags;
	}





	public DPElement present(GSymFragmentViewContext ctx, Object state)
	{
		if ( objectViewContexts == null )
		{
			objectViewContexts = new ObjectViewContextList();
		}
		objectViewContexts.addContext( ctx );
		return null;
	}



	public void onObjectViewContextListEmpty()
	{
		objectViewContexts = null;
	}
}
