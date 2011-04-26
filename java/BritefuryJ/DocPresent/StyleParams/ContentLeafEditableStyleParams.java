//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class ContentLeafEditableStyleParams extends ContentLeafStyleParams
{
	public static final ContentLeafEditableStyleParams defaultStyleParams = new ContentLeafEditableStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, true, true );
	
	private final boolean bEditable, bSelectable;
	
	
	
	public ContentLeafEditableStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, boolean bEditable, boolean bSelectable)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.bEditable = bEditable;
		this.bSelectable = bSelectable;
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
		fields.add( new HorizontalField( "Editable", Pres.coerceNonNull( bEditable ) ) );
		fields.add( new HorizontalField( "Selectable", Pres.coerceNonNull( bSelectable ) ) );
	}
}
