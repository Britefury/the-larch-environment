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
import BritefuryJ.StyleSheet.StyleValues;

public class Align extends Pres
{
	private static final int FLAG_ALIGN_H = 0x1;
	private static final int FLAG_ALIGN_V = 0x2;
	
	
	private int flags;
	private HAlignment hAlign = HAlignment.PACK;
	private VAlignment vAlign = VAlignment.REFY;
	private Pres child;
	
	
	public Align(HAlignment hAlign, Pres child)
	{
		this.hAlign = hAlign;
		this.child = child;
		this.flags = FLAG_ALIGN_H;
	}
	
	public Align(VAlignment vAlign, Pres child)
	{
		this.vAlign = vAlign;
		this.child = child;
		this.flags = FLAG_ALIGN_V;
	}
	
	public Align(HAlignment hAlign, VAlignment vAlign, Pres child)
	{
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		this.child = child;
		this.flags = FLAG_ALIGN_H | FLAG_ALIGN_V;
	}
	
	private Align(HAlignment hAlign, VAlignment vAlign, Pres child, int flags)
	{
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		this.child = child;
		this.flags = flags;
	}

	public Align alignH(HAlignment hAlign)
	{
		return new Align( hAlign, vAlign, child, flags | FLAG_ALIGN_H );
	}

	public Align alignV(VAlignment vAlign)
	{
		return new Align( hAlign, vAlign, child, flags | FLAG_ALIGN_V );
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		if ( flags == FLAG_ALIGN_H )
		{
			return element.alignH( hAlign );
		}
		else if ( flags == FLAG_ALIGN_V )
		{
			return element.alignV( vAlign );
		}
		else if ( flags == ( FLAG_ALIGN_H | FLAG_ALIGN_V ) )
		{
			return element.align( hAlign, vAlign );
		}
		else
		{
			return element;
		}
	}
}
