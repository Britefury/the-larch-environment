//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Segment extends PresentationCombinator
{
	private PresentationCombinator child;
	private boolean bGuardBegin, bGuardEnd;
	
	
	public Segment(Object child, boolean bGuardBegin, boolean bGuardEnd)
	{
		this.child = coerce( child );
		this.bGuardBegin = bGuardBegin;
		this.bGuardEnd = bGuardEnd;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		DPSegment element = new DPSegment( style.getContainerParams(), style.getTextParams(), bGuardBegin, bGuardEnd );
		element.setChild( child.present( ctx ) );
		return element;
	}

}
