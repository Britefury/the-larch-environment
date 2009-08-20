//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Paint;

public class FractionStyleSheet extends ContainerStyleSheet
{
	public static class BarStyleSheet extends ContentLeafStyleSheet
	{
		public static BarStyleSheet defaultStyleSheet = new BarStyleSheet( Color.black );
		
		
		protected Paint barPaint;
		
		
		public BarStyleSheet(Paint barPaint)
		{
			super();
			
			this.barPaint = barPaint;
		}
		
		
		public Paint getBarPaint()
		{
			return barPaint;
		}
	}

	
	public static FractionStyleSheet defaultStyleSheet = new FractionStyleSheet();
	
	protected BarStyleSheet barStyleSheet;
	
	protected double vspacing, hpadding, yOffset;
	
	
	
	
	public FractionStyleSheet()
	{
		this( 2.0, 3.0, 5.0, Color.black );
	}
	
	public FractionStyleSheet(Paint barPaint)
	{
		this( 2.0, 3.0, 5.0, barPaint );
	}
	
	public FractionStyleSheet(double vspacing, double hpadding, double yOffset, Paint barPaint)
	{
		super();
		
		this.vspacing = vspacing;
		this.hpadding = hpadding;
		this.yOffset = yOffset;
		
		barStyleSheet = new BarStyleSheet( barPaint );
	}
	
	
	public BarStyleSheet getBarStyleSheet()
	{
		return barStyleSheet;
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
