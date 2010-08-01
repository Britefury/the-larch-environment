//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GenericPerspectiveInnerFragment extends Pres
{
	private Object model;
	
	
	public GenericPerspectiveInnerFragment(Object model)
	{
		this.model = model;
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymFragmentView fragment = ctx.getFragment();
		GSymAbstractPerspective genericPerspective = fragment.getBrowserContext().getGenericPerspective();
		return fragment.presentInnerFragment( model, genericPerspective, style, ctx.getInheritedState() );
	}
}
