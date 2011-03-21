//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.StyleSheet.StyleValues;

public class SetFixedValue extends Pres
{
	private Pres child;
	private Object value;
	
	
	public SetFixedValue(Object child, Object value)
	{
		this.child = coerce( child );
		this.value = value;
	}


	public SetFixedValue withFixedValue(Object value)
	{
		return new SetFixedValue( child, value );
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setFixedValue( value );
		return element;
	}
}