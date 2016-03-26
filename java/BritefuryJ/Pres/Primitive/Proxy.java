//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSProxy;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Proxy extends Pres
{
	private Pres child;
	
	
	public Proxy()
	{
		this.child = null;
	}
	
	public Proxy(Object child)
	{
		this.child = coerceNullable( child );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElement = child != null  ?  child.present( ctx, style )  :  null;
		return new LSProxy( childElement );
	}
}
