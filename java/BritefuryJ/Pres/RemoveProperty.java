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
	private Object key;
	private Pres child;
	
	
	public RemoveProperty(Object key, Object child)
	{
		this.key = key;
		this.child = Pres.coerce( child );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement e = child.present( ctx, style );
		e.removeProperty( key );
		return e;
	}


	//
	//
	// Property methods
	//
	//
	
	public SetProperty withProperty(Object key, Object value)
	{
		if ( key.equals( this.key ) )
		{
			return new SetProperty( key, value, child );
		}
		else
		{
			return new SetProperty( key, value, this );
		}
	}
	
	public RemoveProperty withoutProperty(Object key)
	{
		if ( key.equals( this.key ) )
		{
			return this;
		}
		else
		{
			return new RemoveProperty( key, this );
		}
	}
}
