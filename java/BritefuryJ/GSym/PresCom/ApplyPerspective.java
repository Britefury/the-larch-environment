//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.PresCom;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;

public class ApplyPerspective extends Pres
{
	private GSymAbstractPerspective perspective;
	private SimpleAttributeTable inheritedState;
	private Pres child;
	
	
	public ApplyPerspective(GSymAbstractPerspective perspective, SimpleAttributeTable inheritedState, Object child)
	{
		this.perspective = perspective;
		this.inheritedState = inheritedState;
		this.child = coerce( child );
	}
	
	public ApplyPerspective(GSymAbstractPerspective perspective, Object child)
	{
		this.perspective = perspective;
		this.inheritedState = null;
		this.child = coerce( child );
	}
	
	
	public static ApplyPerspective generic(Object child)
	{
		return new ApplyPerspective( null, child );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymAbstractPerspective p = perspective;
		if ( p == null )
		{
			p = ctx.getFragment().getBrowserContext().getGenericPerspective();
		}
		
		SimpleAttributeTable s = inheritedState != null  ?  inheritedState  :  p.getInitialInheritedState();
		
		return child.present( new PresentationContext( ctx.getFragment(), p, s ), style );
	}
}
