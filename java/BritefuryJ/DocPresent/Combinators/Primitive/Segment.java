//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Segment extends Pres
{
	private Pres child;
	private boolean bGuardBegin, bGuardEnd;
	
	
	public Segment(boolean bGuardBegin, boolean bGuardEnd, Object child)
	{
		this.child = coerce( child );
		this.bGuardBegin = bGuardBegin;
		this.bGuardEnd = bGuardEnd;
	}
	
	public Segment(Object child)
	{
		this( true, true, child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPSegment element = new DPSegment( Primitive.containerParams.get( style ), Primitive.textParams.get( style ), bGuardBegin, bGuardEnd );
		element.setChild( child.present( ctx, Primitive.useContainerParams.get( style ) ) );
		return element;
	}

}
