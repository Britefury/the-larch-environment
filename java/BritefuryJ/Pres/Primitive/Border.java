//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Border extends Pres
{
	private Pres child;
	private AbstractBorder border;
	
	
	public Border(Object child, AbstractBorder border)
	{
		this.child = coerce( child );
		this.border = border;
	}
	
	public Border(Object child)
	{
		this( child, null );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		if ( border != null )
		{
			DPBorder element = new DPBorder( border, Primitive.containerParams.get( style ) );
			StyleValues childStyle = Primitive.useContainerParams.get( Primitive.useBorderParams.get( style ) );
			element.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
			return element;
		}
		else
		{
			DPBorder element = new DPBorder( Primitive.getBorderParams( style ), Primitive.containerParams.get( style ) );
			StyleValues childStyle = Primitive.useContainerParams.get( Primitive.useBorderParams.get( style ) );
			element.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
			return element;
		}
	}
}
