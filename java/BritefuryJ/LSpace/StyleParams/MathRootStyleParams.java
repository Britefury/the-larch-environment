//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;

public class MathRootStyleParams extends ContainerStyleParams
{
	private static final Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	public static final MathRootStyleParams defaultStyleParams = new MathRootStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, defaultFont, Color.BLACK, null, 1.5 );
	
	protected final Font font;
	protected final Paint symbolPaint, hoverSymbolPaint;
	protected final double thickness;
	
	protected double glyphLineWidths[], glyphWidth, barSpacing;
	
	protected boolean gotMetrics = false;


	public MathRootStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, Font font, Paint symbolPaint, Paint hoverSymbolPaint, double thickness)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.font = font;
		this.symbolPaint = symbolPaint;
		this.hoverSymbolPaint = hoverSymbolPaint;
		this.thickness = thickness;
		glyphLineWidths = new double[3];
		this.barSpacing = 0.0;
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
		refreshMetrics();
		return thickness;
	}
	
	public double getBarSpacing()
	{
		refreshMetrics();
		return barSpacing;
	}
	
	public double getGlyphWidth()
	{
		refreshMetrics();
		return glyphWidth;
	}
	
	public double[] getGlyphLineWidths()
	{
		refreshMetrics();
		return glyphLineWidths;
	}

	
	
	public void refreshMetrics()
	{
		if ( !gotMetrics )
		{
			FontRenderContext frc = new FontRenderContext( null, true, true );
			LineMetrics metrics = font.getLineMetrics( " ", frc );
			double height = metrics.getAscent() + metrics.getDescent();
			
			barSpacing = height * 0.1;
			glyphLineWidths[2] = height * 0.5;
			glyphLineWidths[1] = glyphLineWidths[2] * 0.4;
			glyphLineWidths[0] = glyphLineWidths[2] * 0.3;
			glyphWidth = glyphLineWidths[0] + glyphLineWidths[1] + glyphLineWidths[2];
			
			gotMetrics = true;
		}
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Font", Pres.coercePresentingNull(font) ) );
		fields.add( new HorizontalField( "Symbol paint", Pres.coercePresentingNull(symbolPaint) ) );
		fields.add( new HorizontalField( "Hover symbol paint", Pres.coercePresentingNull(hoverSymbolPaint) ) );
		fields.add( new HorizontalField( "Thickness", Pres.coercePresentingNull(getThickness()) ) );
		fields.add( new HorizontalField( "Bar spacing", Pres.coercePresentingNull(getBarSpacing()) ) );
		fields.add( new HorizontalField( "Glyph width", Pres.coercePresentingNull(getGlyphWidth()) ) );
		fields.add( new HorizontalField( "Glyph line widths", Pres.coercePresentingNull(getGlyphLineWidths()) ) );
	}
}