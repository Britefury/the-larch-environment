//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Bin extends Pres
{
	private Pres child;
	
	
	public Bin(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPBin element = new DPBin( Primitive.containerParams.get( style ) );
		if ( child != null )
		{
			StyleValues childStyle = Primitive.useContainerParams.get( style );
			element.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
		}
		return element;
	}
}
