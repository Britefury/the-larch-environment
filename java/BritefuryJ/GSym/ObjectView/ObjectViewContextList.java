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
	public interface ContextListListener
	{
		void onObjectViewContextListEmpty();
	}
	
	private WeakHashMap<GSymFragmentViewContext, Object> contexts = new WeakHashMap<GSymFragmentViewContext, Object>();
	private ContextListListener listener;
	
	
	public ObjectViewContextList()
	{
		this( null );
	}
	
	public ObjectViewContextList(ContextListListener listener)
	{
		this.listener = listener;
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
		
		if ( contexts.isEmpty() )
		{
			if ( listener != null )
			{
				listener.onObjectViewContextListEmpty();
			}
		}
	}
}
