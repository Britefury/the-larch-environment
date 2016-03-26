//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSegment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElement = child.present( ctx, Primitive.useContainerParams.get( style ) );
		return new LSSegment( Primitive.containerParams.get( style ), Primitive.caretSlotParams.get( style ), bGuardBegin, bGuardEnd, childElement );
	}

}
