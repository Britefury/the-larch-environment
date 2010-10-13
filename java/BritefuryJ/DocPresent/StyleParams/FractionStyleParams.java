//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;

import BritefuryJ.DocPresent.Painter.Painter;

public class FractionStyleParams extends ContainerStyleParams
{
	public static class BarStyleParams extends ContentLeafEditableStyleParams
	{
		public static final BarStyleParams defaultStyleParams = new BarStyleParams( null, null, null, true, true, Color.black, null );
		
		
		protected final Paint barPaint, hoverBarPaint;


		public BarStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, boolean bEditable, boolean bSelectable, Paint barPaint, Paint hoverBarPaint)
		{
			super( background, hoverBackground, pointerCursor, bEditable, bSelectable );
			
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
	}

	
	public static final FractionStyleParams defaultStyleParams = new FractionStyleParams( null, null, null, 2.0, 3.0, 5.0, BarStyleParams.defaultStyleParams );
	
	protected final BarStyleParams barStyleParams;
	
	protected final double vspacing, hpadding, yOffset;
	
	
	
	
	public FractionStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, double vspacing, double hpadding, double yOffset, BarStyleParams barStyleParams)
	{
		super( background, hoverBackground, pointerCursor );
		
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
}
