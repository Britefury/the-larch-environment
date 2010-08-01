//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.PresCom;

import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.View.GSymFragmentView;

public class InnerFragment extends Pres
{
	private Object model;
	private AttributeTable inheritedState;
	
	
	public InnerFragment(Object model)
	{
		this.model = model;
		this.inheritedState = null;
	}
	
	public InnerFragment(Object model, AttributeTable inheritedState)
	{
		this.model = model;
		this.inheritedState = inheritedState;
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymFragmentView fragment = ctx.getFragment();
		return fragment.presentInnerFragment( model, ctx.getPerspective(), style, inheritedState != null  ?  inheritedState  :  ctx.getInheritedState() );
	}
	
	
	
	public static InnerFragment[] mapInnerFragments(Object models[])
	{
		InnerFragment fragments[] = new InnerFragment[models.length];
		for (int i = 0; i < models.length; i++)
		{
			fragments[i] = new InnerFragment( models[i] );
		}
		return fragments;
	}

	public static InnerFragment[] mapInnerFragments(List<Object> models)
	{
		InnerFragment fragments[] = new InnerFragment[models.size()];
		for (int i = 0; i < models.size(); i++)
		{
			fragments[i] = new InnerFragment( models.get( i ) );
		}
		return fragments;
	}

	
	public static InnerFragment[] mapInnerFragments(Object models[], AttributeTable inheritedState)
	{
		InnerFragment fragments[] = new InnerFragment[models.length];
		for (int i = 0; i < models.length; i++)
		{
			fragments[i] = new InnerFragment( models[i], inheritedState );
		}
		return fragments;
	}

	public static InnerFragment[] mapInnerFragments(List<Object> models, AttributeTable inheritedState)
	{
		InnerFragment fragments[] = new InnerFragment[models.size()];
		for (int i = 0; i < models.size(); i++)
		{
			fragments[i] = new InnerFragment( models.get( i ), inheritedState );
		}
		return fragments;
	}
}
