//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
