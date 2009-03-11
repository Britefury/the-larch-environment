//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.DPPresentationArea;

public class MathRootStyleSheet extends ContainerStyleSheet
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	public static MathRootStyleSheet defaultStyleSheet = new MathRootStyleSheet();
	
	protected Font font;
	protected Color colour;
	protected double thickness;
	
	protected double glyphLineWidths[], glyphWidth, barSpacing;
	protected boolean bRealised;
	
	
	
	
	public MathRootStyleSheet()
	{
		this( defaultFont, Color.BLACK, 1.5 );
	}
	
	public MathRootStyleSheet(Color colour, double thickness)
	{
		this( defaultFont, colour, thickness );
	}
	
	public MathRootStyleSheet(Font font, Color colour, double thickness)
	{
		super();
		
		this.font = font;
		this.colour = colour;
		this.thickness = thickness;
		glyphLineWidths = new double[3];
		this.barSpacing = 0.0;
		bRealised = false;
	}
	
	
	public Font getFont()
	{
		return font;
	}

	public Color getColour()
	{
		return colour;
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

	
	
	public void realise(DPPresentationArea a)
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