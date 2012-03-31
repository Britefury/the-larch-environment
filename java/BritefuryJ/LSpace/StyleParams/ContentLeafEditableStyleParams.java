//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class ContentLeafEditableStyleParams extends ContentLeafStyleParams
{
	public static final ContentLeafEditableStyleParams defaultStyleParams = new ContentLeafEditableStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null,
			new Color( 0.0f, 0.0f, 1.0f ), true, true );
	
	private final Color caretColour;
	private final boolean bEditable, bSelectable;
	
	
	
	public ContentLeafEditableStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, Color caretColour, boolean bEditable, boolean bSelectable)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.caretColour = caretColour;
		this.bEditable = bEditable;
		this.bSelectable = bSelectable;
	}
	
	
	public Color getCaretColour()
	{
		return caretColour;
	}
	
	public boolean getEditable()
	{
		return bEditable;
	}
	
	public boolean getSelectable()
	{
		return bSelectable;
	}



	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Caret colour", Pres.coerceNonNull( caretColour ) ) );
		fields.add( new HorizontalField( "Editable", Pres.coerceNonNull( bEditable ) ) );
		fields.add( new HorizontalField( "Selectable", Pres.coerceNonNull( bSelectable ) ) );
	}
}
