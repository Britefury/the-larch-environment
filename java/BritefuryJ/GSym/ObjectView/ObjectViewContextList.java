//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectView;

import java.util.WeakHashMap;

import BritefuryJ.GSym.View.GSymFragmentViewContext;

public class ObjectViewContextList
{
	private WeakHashMap<GSymFragmentViewContext, Object> contexts = new WeakHashMap<GSymFragmentViewContext, Object>();
	
	
	public ObjectViewContextList()
	{
	}
	
	
	public void addContext(GSymFragmentViewContext ctx)
	{
		contexts.put( ctx, null );
	}
	
	public void queueRefresh()
	{
		for (GSymFragmentViewContext ctx: contexts.keySet())
		{
			ctx.queueRefresh();
		}
	}
}
