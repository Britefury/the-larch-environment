//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Bin extends Pres
{
	private Pres child;
	
	
	public Bin()
	{
		this.child = null;
	}

	public Bin(Object child)
	{
		this.child = coerceNullable( child );
	}



	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElem = null;
		if ( child != null )
		{
			StyleValues childStyle = Primitive.useContainerParams.get( style );
			childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
		}
		return new LSBin( Primitive.containerParams.get( style ), childElem );
	}
}
