//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Cursor;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class ScriptStyleParams extends ContainerStyleParams
{
	public static final ScriptStyleParams defaultStyleSheet = new ScriptStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 1.0, 1.0 );
	
	
	private final double columnSpacing, rowSpacing;
	
	
	public ScriptStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, double columnSpacing, double rowSpacing)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.columnSpacing = columnSpacing;
		this.rowSpacing = rowSpacing;
	}


	public double getColumnSpacing()
	{
		return this.columnSpacing;
	}

	
	public double getRowSpacing()
	{
		return this.rowSpacing;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Column spacing", Pres.coercePresentingNull(columnSpacing) ) );
		fields.add( new HorizontalField( "Row spacing", Pres.coercePresentingNull(rowSpacing) ) );
	}
}
