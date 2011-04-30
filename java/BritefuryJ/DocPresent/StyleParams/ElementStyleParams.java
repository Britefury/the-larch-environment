//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.Primitive.Label;

public class ElementStyleParams implements Presentable
{
	public static final ElementStyleParams defaultStyleParams = new ElementStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null );
	
	
	private final HAlignment hAlign;
	private final VAlignment vAlign;
	private final Painter background, hoverBackground;
	private final Cursor cursor;
	
	
	public ElementStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor)
	{
		this.hAlign = hAlign;
		this.vAlign = vAlign;
		this.background = background;
		this.hoverBackground = hoverBackground;
		this.cursor = pointerCursor;
	}
	
	
	public HAlignment getHAlignment()
	{
		return hAlign;
	}
	
	public VAlignment getVAlignment()
	{
		return vAlign;
	}
	
	public Painter getBackground()
	{
		return background;
	}
	
	public Painter getHoverBackground()
	{
		return hoverBackground;
	}
	
	public Cursor getCursor()
	{
		return cursor;
	}
	
	
	protected void buildFieldList(List<Object> fields)
	{
		fields.add( new HorizontalField( "H-Align", new Label( hAlign.toString() ) ) );
		fields.add( new HorizontalField( "V-Align", new Label( vAlign.toString() ) ) );
		fields.add( new HorizontalField( "Background", Pres.coerceNonNull( background ) ) );
		fields.add( new HorizontalField( "Hover background", Pres.coerceNonNull( hoverBackground ) ) );
		fields.add( new HorizontalField( "Cursor", Pres.coerceNonNull( cursor ) ) );
	}


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String className = getClass().getName();
		className = className.substring( className.lastIndexOf( "." ) );
		
		ArrayList<Object> fields = new ArrayList<Object>();
		buildFieldList( fields );
		return new ObjectBoxWithFields( className, fields );
	}
}
