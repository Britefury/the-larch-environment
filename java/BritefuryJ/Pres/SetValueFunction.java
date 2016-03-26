//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementValueFunction;
import BritefuryJ.StyleSheet.StyleValues;

public class SetValueFunction extends Pres
{
	private Pres child;
	private ElementValueFunction valueFn;
	
	
	public SetValueFunction(Object child, ElementValueFunction valueFn)
	{
		this.child = coerce( child );
		this.valueFn = valueFn;
	}


	public SetValueFunction withValueFunction(ElementValueFunction valueFn)
	{
		return new SetValueFunction( child, valueFn );
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.setValueFunction( valueFn );
		return element;
	}
}
