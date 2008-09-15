//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.Math.Point2;



public class TextVisual
{
	public static class UnrealisedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static interface TextVisualListener
	{
		public void textVisualRequestResize(TextVisual t);
		public void textVisualRequestRedraw(TextVisual t);
	}
	
	
	private TextLayout layout;
	private String text;
	private TextStyleSheet styleSheet;
	private TextVisualListener listener;
	private JComponent component;
	
	
	public TextVisual(String text, TextStyleSheet styleSheet, TextVisualListener listener)
	{
		this.text = text;
		this.styleSheet = styleSheet;
		this.listener = listener;
	}
	
	
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		if ( !text.equals( this.text ) )
		{
			this.text = text;
			layoutChanged();
		}
	}
	
	
	
	public void realise(DPPresentationArea a)
	{
		component = a.getComponent();
	}
	
	public void realise(JComponent component)
	{
		this.component = component;
	}
	
	
	public void unrealise()
	{
		component = null;
		layout = null;
	}
	
	
	
	
	public HMetrics computeHMetrics()
	{
		refreshLayout();
		if ( layout != null )
		{
			double width = layout.getBounds().getWidth();
			return new HMetrics( width, layout.getAdvance() - width );
		}
		else
		{
			return new HMetrics();
		}
	}
	
	public VMetrics computeVMetrics()
	{
		refreshLayout();
		if ( layout != null )
		{
			return new VMetricsTypeset( layout.getAscent(), layout.getDescent(), layout.getLeading() );
		}
		else
		{
			if ( component != null )
			{
				Graphics2D graphics = (Graphics2D)component.getGraphics();
				if ( graphics != null )
				{
					FontRenderContext frc = graphics.getFontRenderContext();
					LineMetrics lineMetrics = styleSheet.getFont().getLineMetrics( "", frc );
					return new VMetricsTypeset( lineMetrics.getAscent(), lineMetrics.getDescent(), lineMetrics.getLeading() );
				}
			}
			return new VMetrics();
		}
	}
	
	
	
	public void draw(Graphics2D graphics)
	{
		refreshLayout();
		if ( layout != null )
		{
			Color prevColour = graphics.getColor();
			graphics.setColor( styleSheet.getColour() );
			layout.draw( graphics, 0.0f, layout.getAscent() );
			graphics.setColor( prevColour );
		}
	}
	
	public void drawCaret(Graphics2D graphics, int offset)
	{
		refreshLayout();
		if ( layout != null )
		{
			Shape[] carets = layout.getCaretShapes( offset );
			graphics.translate( 0.0, layout.getAscent() );
			graphics.draw( carets[0] );
			if ( carets[1] != null )
			{
				graphics.draw( carets[1] );
			}
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = styleSheet.getFont().getLineMetrics( "", frc );
			Line2D.Double line = new Line2D.Double( 0.0, 0.0, 0.0, lineMetrics.getAscent() + lineMetrics.getDescent() );
			graphics.draw( line );
		}
	}
	
	public void drawCaretAtStart(Graphics2D graphics)
	{
		double h = layout.getBounds().getHeight();
		graphics.draw( new Line2D.Double( 0.0, 0.0, 0.0, h ) );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		double x = layout.getBounds().getWidth();
		double h = layout.getBounds().getHeight();
		graphics.draw( new Line2D.Double( x, 0.0, x, h ) );
	}
	
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		refreshLayout();
		if ( layout != null )
		{
			return layout.hitTestChar( (float)pos.x, (float)pos.y );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	
	private void layoutChanged()
	{
		layout = null;
		requestResize();
	}
	
	private void requestResize()
	{
		if ( listener != null )
		{
			listener.textVisualRequestResize( this );
		}
	}
	
	private void refreshLayout()
	{
		if ( layout == null  &&  text.length() > 0  &&  component != null )
		{
			Graphics2D graphics = (Graphics2D)component.getGraphics();
			FontRenderContext frc = graphics.getFontRenderContext();
			layout = new TextLayout( text, styleSheet.getFont(), frc );
		}
	}
}
