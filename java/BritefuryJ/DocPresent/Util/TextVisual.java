//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.Math.Point2;



public class TextVisual
{
	private static class SegmentLayout
	{
		public TextLayout layout;
		public int length;
		
		
		
		public SegmentLayout(String text, Font font, FontRenderContext frc, int length)
		{
			layout = new TextLayout( text, font, frc );
			this.length = length;
		}
		
		
		
		public void drawCaret(Graphics2D graphics, int charOffset, double xOffset)
		{
			Shape[] carets = layout.getCaretShapes( charOffset );
			graphics.translate( xOffset, 0.0 );
			graphics.draw( carets[0] );
			if ( carets[1] != null )
			{
				graphics.draw( carets[1] );
			}
		}

		
		
		public static HMetrics computeHMetrics(SegmentLayout layouts[])
		{
			double x = 0.0, end = 0.0;
			for (SegmentLayout l: layouts)
			{
				end = x + l.layout.getBounds().getWidth();
				x += l.layout.getAdvance();
			}
			
			return new HMetrics( end, x - end );
		}

		public static VMetricsTypeset computeVMetrics(SegmentLayout layouts[])
		{
			double ascent = 0.0, descent = 0.0, vspacing = 0.0;
			for (SegmentLayout l: layouts)
			{
				ascent = Math.max( ascent, l.layout.getAscent() );
				descent = Math.max( descent, l.layout.getDescent() );
				vspacing = Math.max( vspacing, l.layout.getLeading() );
			}
			
			return new VMetricsTypeset( ascent, descent, vspacing );
		}
		
		
		public static void draw(Graphics2D graphics, SegmentLayout layouts[])
		{
			double ascent = 0.0;
			for (SegmentLayout l: layouts)
			{
				ascent = Math.max( ascent, l.layout.getAscent() );
			}

			double x = 0.0;
			for (SegmentLayout l: layouts)
			{
				l.layout.draw( graphics, (float)x, (float)ascent );
				x += l.layout.getAdvance();
			}
		}
	
	
		public static void drawCaret(Graphics2D graphics, int offset, SegmentLayout layouts[])
		{
			double ascent = 0.0;
			for (SegmentLayout l: layouts)
			{
				ascent = Math.max( ascent, l.layout.getAscent() );
			}
			graphics.translate( 0.0, ascent );

			double x = 0.0;
			for (SegmentLayout l: layouts)
			{
				if ( offset <= l.length )
				{
					l.drawCaret( graphics, offset, x );
					break;
				}
				offset -= l.length;
				x += l.layout.getAdvance();
			}
		}
		
		
		public static TextHitInfo hitTest(Point2 pos, SegmentLayout layouts[])
		{
			double x = 0.0;
			for (SegmentLayout l: layouts)
			{
				double relX = pos.x - x;
				double advance = l.layout.getAdvance();
				if ( relX <= advance )
				{
					return l.layout.hitTestChar( (float)relX, (float)pos.y );
				}
				else
				{
					x += advance;
				}
			}
			
			return null;
		}
	}
	
	
	
	public static class UnrealisedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static interface TextVisualListener
	{
		public void textVisualRequestResize(TextVisual t);
		public void textVisualRequestRedraw(TextVisual t);
	}
	
	
	private SegmentLayout layouts[];
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
		layouts = null;
	}
	
	
	
	
	public HMetrics computeHMetrics()
	{
		refreshLayout();
		if ( layouts != null )
		{
			return SegmentLayout.computeHMetrics( layouts );
		}
		else
		{
			return new HMetrics();
		}
	}
	
	public VMetrics computeVMetrics()
	{
		refreshLayout();
		if ( layouts != null )
		{
			return SegmentLayout.computeVMetrics( layouts );
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
		if ( layouts != null )
		{
			Color prevColour = graphics.getColor();
			graphics.setColor( styleSheet.getColour() );
			SegmentLayout.draw( graphics, layouts );
			graphics.setColor( prevColour );
		}
	}
	
	public void drawCaret(Graphics2D graphics, int offset)
	{
		refreshLayout();
		if ( layouts != null )
		{
			SegmentLayout.drawCaret( graphics, offset, layouts );
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
		refreshLayout();
		double h = 0.0;
		if ( layouts != null )
		{
			VMetrics vm = SegmentLayout.computeVMetrics( layouts );
			h = vm.getLength();
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = styleSheet.getFont().getLineMetrics( "", frc );
			h = lineMetrics.getAscent() + lineMetrics.getDescent();
		}
		graphics.draw( new Line2D.Double( 0.0, 0.0, 0.0, h ) );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		refreshLayout();
		double x = 0.0, h = 0.0;
		if ( layouts != null )
		{
			HMetrics hm = SegmentLayout.computeHMetrics( layouts );
			VMetrics vm = SegmentLayout.computeVMetrics( layouts );
			x = hm.getLength();
			h = vm.getLength();
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = styleSheet.getFont().getLineMetrics( "", frc );
			x = 0.0;
			h = lineMetrics.getAscent() + lineMetrics.getDescent();
		}
		graphics.draw( new Line2D.Double( x, 0.0, x, h ) );
	}
	
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		refreshLayout();
		if ( layouts != null )
		{
			return SegmentLayout.hitTest( pos, layouts );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	
	private void layoutChanged()
	{
		layouts = null;
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
		if ( layouts == null  &&  text.length() > 0  &&  component != null )
		{
			Graphics2D graphics = (Graphics2D)component.getGraphics();
			FontRenderContext frc = graphics.getFontRenderContext();
			
			if ( styleSheet.getMixedSizeCaps() )
			{
				ArrayList<SegmentLayout> segments = new ArrayList<SegmentLayout>();
				String upperText = text.toUpperCase();
				
				Font upperCaseFont = styleSheet.getFont();
				Font lowerCaseFont = upperCaseFont.deriveFont( upperCaseFont.getSize2D() * 0.75f );
				boolean bSegmentLowercase = Character.isLowerCase( text.charAt( 0 ) );
				int segmentPos = 0;
				
				for (int i = 0; i < text.length(); i++)
				{
					boolean bCharLowercase = Character.isLowerCase( text.charAt( i ) );
					if ( bCharLowercase != bSegmentLowercase )
					{
						// Encountered end of segment
						SegmentLayout l = new SegmentLayout( upperText.substring( segmentPos, i ), bSegmentLowercase ? lowerCaseFont : upperCaseFont, frc, i - segmentPos );
						segments.add( l );
						bSegmentLowercase = bCharLowercase;
						segmentPos = i;
					}
				}

				SegmentLayout l = new SegmentLayout( upperText.substring( segmentPos, text.length() ), bSegmentLowercase ? lowerCaseFont : upperCaseFont, frc, text.length() - segmentPos );
				segments.add( l );
				
				layouts = new SegmentLayout[segments.size()];
				layouts = segments.toArray( layouts );
			}
			else
			{
				layouts = new SegmentLayout[1];
				layouts[0] = new SegmentLayout( text, styleSheet.getFont(), frc, text.length() );
			}
		}
	}
}
