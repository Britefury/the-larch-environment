//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.DocPresent.Painter.Painter;

public class FractionStyleParams extends ContainerStyleParams
{
	public static class BarStyleParams extends ContentLeafStyleParams
	{
		public static final BarStyleParams defaultStyleParams = new BarStyleParams( null, Color.black );
		
		
		protected final Paint barPaint;


		public BarStyleParams(Painter background, Paint barPaint)
		{
			super( background );
			
			this.barPaint = barPaint;
		}


		public Paint getBarPaint()
		{
			return barPaint;
		}
	}

	
	public static final FractionStyleParams defaultStyleParams = new FractionStyleParams( null, 2.0, 3.0, 5.0, null, Color.black );
	
	protected final BarStyleParams barStyleParams;
	
	protected final double vspacing, hpadding, yOffset;
	
	
	
	
	public FractionStyleParams(Painter background, double vspacing, double hpadding, double yOffset, Painter barBackground, Paint barPaint)
	{
		super( background );
		
		this.vspacing = vspacing;
		this.hpadding = hpadding;
		this.yOffset = yOffset;
		
		barStyleParams = new BarStyleParams( barBackground, barPaint );
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
