//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class SetProperty extends Pres
{
	private Object key, value;
	private Pres child;
	
	
	public SetProperty(Object key, Object value, Object child)
	{
		this.key = key;
		this.value = value;
		this.child = Pres.coerce( child );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement e = child.present( ctx, style );
		e.setProperty( key, value );
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
			return new RemoveProperty( key, child );
		}
		else
		{
			return new RemoveProperty( key, this );
		}
	}
}
