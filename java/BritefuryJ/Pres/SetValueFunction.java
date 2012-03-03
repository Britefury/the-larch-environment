//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
