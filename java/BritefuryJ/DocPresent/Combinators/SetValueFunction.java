//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setValueFunction( valueFn );
		return element;
	}
}
