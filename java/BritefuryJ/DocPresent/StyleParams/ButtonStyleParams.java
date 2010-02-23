//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.Painter;

public class ButtonStyleParams extends ContainerStyleParams
{
	public static final ButtonStyleParams defaultStyleParams = new ButtonStyleParams( null, 
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.2f, 0.3f, 0.5f ), new Color( 0.3f, 0.45f, 0.75f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ),
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 0.9f, 0.92f, 1.0f ), new Color( 0.75f, 0.825f, 0.9f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ),
			new RadialGradientPaint( -10.0f, -10.0f, 100.0f, new float[] { 0.0f, 1.0f }, new Color[] { new Color( 1.0f, 1.0f, 1.0f ), new Color( 0.85f, 0.85f, 0.85f ) }, RadialGradientPaint.CycleMethod.NO_CYCLE ) );
	
	
	
	protected final Paint borderPaint, backgroundPaint, highlightBackgPaint;
	protected final SolidBorder border, highlightBorder;


	public ButtonStyleParams(Painter background, Paint borderPaint, Paint backgroundPaint, Paint highlightBackgPaint)
	{
		super( background );
		
		this.borderPaint = borderPaint;
		this.backgroundPaint = backgroundPaint;
		this.highlightBackgPaint = highlightBackgPaint;
		
		border = new SolidBorder( 1.0, 2.0, 10.0, 10.0, borderPaint, backgroundPaint );
		highlightBorder = new SolidBorder( 1.0, 2.0, 10.0, 10.0, borderPaint, highlightBackgPaint );
	}


	public Paint getBorderPaint()
	{
		return borderPaint;
	}
	
	public Paint getBackgroundPaint()
	{
		return backgroundPaint;
	}
	
	public Paint getHighlightBackgroundPaint()
	{
		return highlightBackgPaint;
	}
	
	
	public SolidBorder getBorder()
	{
		return border;
	}

	public SolidBorder getHighlightBorder()
	{
		return highlightBorder;
	}
}
