//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;

public class PerspectivePres extends Pres
{
	private GSymAbstractPerspective perspective;
	private AttributeTable inheritedState;
	private Pres child;
	
	
	public PerspectivePres(GSymAbstractPerspective perspective, AttributeTable inheritedState, Object child)
	{
		this.perspective = perspective;
		this.inheritedState = inheritedState;
		this.child = coerce( child );
	}
	
	public PerspectivePres(GSymAbstractPerspective perspective, Object child)
	{
		this.perspective = perspective;
		this.inheritedState = null;
		this.child = coerce( child );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return child.present( new PresentationContext( ctx.getFragment(), perspective, inheritedState != null  ?  inheritedState : ctx.getInheritedState() ), style );
	}
}
