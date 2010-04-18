//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Painter.Painter;

public class MathRootStyleParams extends ContainerStyleParams
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	public static final MathRootStyleParams defaultStyleParams = new MathRootStyleParams( null, null, null, defaultFont, Color.BLACK, null, 1.5 );
	
	protected final Font font;
	protected final Paint symbolPaint, hoverSymbolPaint;
	protected final double thickness;
	
	protected double glyphLineWidths[], glyphWidth, barSpacing;
	protected boolean bRealised;


	public MathRootStyleParams(Painter background, Painter hoverBackground, Cursor pointerCursor, Font font, Paint symbolPaint, Paint hoverSymbolPaint, double thickness)
	{
		super( background, hoverBackground, pointerCursor );
		
		this.font = font;
		this.symbolPaint = symbolPaint;
		this.hoverSymbolPaint = hoverSymbolPaint;
		this.thickness = thickness;
		glyphLineWidths = new double[3];
		this.barSpacing = 0.0;
		bRealised = false;
	}


	public Font getFont()
	{
		return font;
	}

	public Paint getSymbolPaint()
	{
		return symbolPaint;
	}
	
	public Paint getHoverSymbolPaint()
	{
		return hoverSymbolPaint;
	}

	public double getThickness()
	{
		return thickness;
	}
	
	public double getBarSpacing()
	{
		return barSpacing;
	}
	
	public double getGlyphWidth()
	{
		return glyphWidth;
	}
	
	public double[] getGlyphLineWidths()
	{
		return glyphLineWidths;
	}

	
	
	public void realise(PresentationComponent.RootElement a)
	{
		realise( a.getComponent() );
	}
	
	public void realise(JComponent component)
	{
		Graphics2D graphics = (Graphics2D)component.getGraphics();
		realise( graphics );
	}

	
	public void realise(Graphics2D graphics)
	{
		if ( !bRealised  &&  graphics != null )
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics metrics = font.getLineMetrics( " ", frc );
			double height = metrics.getAscent() + metrics.getDescent();
			
			barSpacing = height * 0.1;
			glyphLineWidths[2] = height * 0.5;
			glyphLineWidths[1] = glyphLineWidths[2] * 0.4;
			glyphLineWidths[0] = glyphLineWidths[2] * 0.3;
			glyphWidth = glyphLineWidths[0] + glyphLineWidths[1] + glyphLineWidths[2];
			
			bRealised = true;
		}
	}
}