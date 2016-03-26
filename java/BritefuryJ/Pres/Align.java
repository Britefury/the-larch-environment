//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
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
	public LSElement present(PresentationContext ctx, StyleValues style)
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
