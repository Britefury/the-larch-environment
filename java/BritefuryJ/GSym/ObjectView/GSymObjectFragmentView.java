//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectView;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymObjectFragmentView
{
	private class GSymObjectViewFragmentFunction implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymIncrementalNodeContext ctx, StyleSheet styleSheet, Object state)
		{
			GSymFragmentViewContext fragmentCtx = (GSymFragmentViewContext)ctx;
			if ( x instanceof Presentable )
			{
				return viewPresentable( (Presentable)x, fragmentCtx, state );
			}
			return null;
		}
	}
	
	
	private GSymObjectViewFragmentFunction viewFragFn = new GSymObjectViewFragmentFunction();
	
	
	public GSymObjectFragmentView()
	{
	}
	
	
	public GSymViewFragmentFunction getViewFragmentFunction()
	{
		return viewFragFn;
	}
	
	
	
	
	private DPElement viewPresentable(Presentable p, GSymFragmentViewContext ctx, Object state)
	{
		return p.present( ctx, state );
	}
}
