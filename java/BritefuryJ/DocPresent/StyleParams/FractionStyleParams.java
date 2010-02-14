//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Paint;

public class FractionStyleParams extends ContainerStyleParams
{
	public static class BarStyleParams extends ContentLeafStyleParams
	{
		public static final BarStyleParams defaultStyleParams = new BarStyleParams( Color.black );
		
		
		protected final Paint barPaint;


		public BarStyleParams(Paint barPaint)
		{
			super();
			
			this.barPaint = barPaint;
		}


		public Paint getBarPaint()
		{
			return barPaint;
		}
	}

	
	public static final FractionStyleParams defaultStyleParams = new FractionStyleParams();
	
	protected final BarStyleParams barStyleParams;
	
	protected final double vspacing, hpadding, yOffset;
	
	
	
	
	public FractionStyleParams()
	{
		this( 2.0, 3.0, 5.0, Color.black );
	}
	
	public FractionStyleParams(Paint barPaint)
	{
		this( 2.0, 3.0, 5.0, barPaint );
	}
	
	public FractionStyleParams(double vspacing, double hpadding, double yOffset, Paint barPaint)
	{
		super();
		
		this.vspacing = vspacing;
		this.hpadding = hpadding;
		this.yOffset = yOffset;
		
		barStyleParams = new BarStyleParams( barPaint );
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
