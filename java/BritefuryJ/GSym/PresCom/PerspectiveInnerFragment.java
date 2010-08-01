//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.PresCom;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public class PerspectiveInnerFragment extends Pres
{
	private GSymAbstractPerspective perspective;
	private Object model;
	private AttributeTable inheritedState;
	
	
	public PerspectiveInnerFragment(GSymAbstractPerspective perspective, Object model)
	{
		this.perspective = perspective;
		this.model = model;
		this.inheritedState = null;
	}
	
	public PerspectiveInnerFragment(GSymAbstractPerspective perspective, Object model, AttributeTable inheritedState)
	{
		this.perspective = perspective;
		this.model = model;
		this.inheritedState = inheritedState;
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymFragmentView fragment = ctx.getFragment();
		return fragment.presentInnerFragment( model, perspective, style, inheritedState != null  ?  inheritedState  :  ctx.getInheritedState() );
	}
}
