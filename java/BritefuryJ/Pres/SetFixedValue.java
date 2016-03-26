//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.setFixedValue( value );
		return element;
	}
}
