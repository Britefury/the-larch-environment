//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
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
		fields.add( new HorizontalField( "Background", Pres.coercePresentingNull(background) ) );
		fields.add( new HorizontalField( "Hover background", Pres.coercePresentingNull(hoverBackground) ) );
		fields.add( new HorizontalField( "Cursor", Pres.coercePresentingNull(cursor) ) );
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
