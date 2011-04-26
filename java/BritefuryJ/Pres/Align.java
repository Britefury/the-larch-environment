//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleValues;

public class Align extends Pres
{
	private HAlignment hAlign;
	private VAlignment vAlign;
	private Pres child;
	
	
	public Align(HAlignment hAlign, VAlignment vAlign, Object child)
	{
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		
		setChild( coerce( child ) );
	}

	public Align(HAlignment hAlign, Object child)
	{
		this.hAlign = hAlign;
		this.vAlign = null;

		setChild( coerce( child ) );
	}

	public Align(VAlignment vAlign, Object child)
	{
		this.hAlign = null;
		this.vAlign = vAlign;

		setChild( coerce( child ) );
	}
	
	
	private void setChild(Pres child)
	{
		if ( child instanceof Align )
		{
			Align align = (Align)child;
			if ( align.hAlign != null )
			{
				hAlign = align.hAlign;
			}
			if ( align.vAlign != null )
			{
				vAlign = align.vAlign;
			}
			
			this.child = align.child;
		}
		else
		{
			this.child = child;
		}
	}
	

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		if ( hAlign != null )
		{
			style = style.withAttr( Primitive.hAlign, hAlign );
		}

		if ( vAlign != null )
		{
			style = style.withAttr( Primitive.vAlign, vAlign );
		}

		return child.present( ctx, style );
	}

}
