//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Border extends Pres
{
	private Pres child;
	private AbstractBorder border;
	private boolean clip;
	
	
	public Border(Object child, AbstractBorder border, boolean clip)
	{
		this.child = coerce( child );
		this.border = border;
		this.clip = clip;
	}

	public Border(Object child, AbstractBorder border)
	{
		this( child, border, false );
	}

	public Border(Object child)
	{
		this( child, null );
	}


	public static Border clip(Object child, AbstractBorder border)
	{
		return new Border( child, border, true );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSBorder elem;
		if ( border != null )
		{
			StyleValues childStyle = Primitive.useContainerParams.get( Primitive.useBorderParams.get( style ) );
			LSElement childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
			elem = new LSBorder( border, Primitive.containerParams.get( style ), childElem );
		}
		else
		{
			StyleValues childStyle = Primitive.useContainerParams.get( Primitive.useBorderParams.get( style ) );
			LSElement childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
			elem =  new LSBorder( Primitive.getBorderParams( style ), Primitive.containerParams.get( style ), childElem );
		}
		if ( clip )
		{
			elem.enableClipping();
		}
		return elem;
	}
}
