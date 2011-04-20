//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.StyleSheet.StyleValues;

public class ApplyPerspective extends Pres
{
	private AbstractPerspective perspective;
	private Pres child;
	
	
	public ApplyPerspective(AbstractPerspective perspective, Object child)
	{
		this.perspective = perspective;
		this.child = coerceNonNull( child );
	}
	
	
	public static ApplyPerspective defaultPerspective(Object child)
	{
		return new ApplyPerspective( null, child );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		AbstractPerspective p = perspective;
		if ( p == null )
		{
			p = DefaultPerspective.instance;
		}
		
		return child.present( new PresentationContext( ctx.getFragment(), p, ctx.getInheritedState() ), style );
	}
}
