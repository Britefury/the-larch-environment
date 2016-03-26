//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class FractionStyleParams extends ContainerStyleParams
{
	public static class BarStyleParams extends ContentLeafEditableStyleParams
	{
		public static final BarStyleParams defaultStyleParams = new BarStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, new Color( 0.0f, 0.0f, 1.0f ), true, true, Color.black, null );
		
		
		protected final Paint barPaint, hoverBarPaint;


		public BarStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor,
				Color caretColour, boolean bEditable, boolean bSelectable, Paint barPaint, Paint hoverBarPaint)
		{
			super( hAlign, vAlign, background, hoverBackground, pointerCursor, caretColour, bEditable, bSelectable );
			
			this.barPaint = barPaint;
			this.hoverBarPaint = hoverBarPaint;
		}


		public Paint getBarPaint()
		{
			return barPaint;
		}
		
		public Paint getHoverBarPaint()
		{
			return hoverBarPaint;
		}



		protected void buildFieldList(List<Object> fields)
		{
			super.buildFieldList( fields );
			fields.add( new HorizontalField( "Bar paint", Pres.coercePresentingNull(barPaint) ) );
			fields.add( new HorizontalField( "Hover bar paint", Pres.coercePresentingNull(hoverBarPaint) ) );
		}
	}

	
	public static final FractionStyleParams defaultStyleParams = new FractionStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 2.0, 3.0, 5.0, BarStyleParams.defaultStyleParams );
	
	protected final BarStyleParams barStyleParams;
	
	protected final double vspacing, hpadding, yOffset;
	
	
	
	
	public FractionStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor,
			double vspacing, double hpadding, double yOffset, BarStyleParams barStyleParams)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.vspacing = vspacing;
		this.hpadding = hpadding;
		this.yOffset = yOffset;
		this.barStyleParams = barStyleParams;
	}
	
	
	public BarStyleParams getBarStyleSheet()
	{
		return barStyleParams;
	}
	
	
	public double getVSpacing()
	{
		return vspacing;
	}
	
	public double getHPadding()
	{
		return hpadding;
	}
	
	public double getYOffset()
	{
		return yOffset;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Vertical spacing", Pres.coercePresentingNull(vspacing) ) );
		fields.add( new HorizontalField( "Horizontal padding", Pres.coercePresentingNull(hpadding) ) );
		fields.add( new HorizontalField( "Y-offset", Pres.coercePresentingNull(yOffset) ) );
	}
}
