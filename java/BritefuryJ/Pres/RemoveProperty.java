//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class RemoveProperty extends Pres
{
	private Object property;
	private Pres child;
	
	
	public RemoveProperty(Object property, Object child)
	{
		this.property = property;
		this.child = Pres.coerce( child );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement e = child.present( ctx, style );
		e.removeProperty( property );
		return e;
	}


	//
	//
	// Property methods
	//
	//
	
	public SetProperty withProperty(Object property, Object value)
	{
		if ( property.equals( this.property ) )
		{
			return new SetProperty( property, value, child );
		}
		else
		{
			return new SetProperty( property, value, this );
		}
	}
	
	public RemoveProperty withoutProperty(Object property)
	{
		if ( property.equals( this.property ) )
		{
			return this;
		}
		else
		{
			return new RemoveProperty( property, this );
		}
	}
}
