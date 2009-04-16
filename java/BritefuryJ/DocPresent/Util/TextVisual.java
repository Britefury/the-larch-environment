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
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.text.Segment;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.Math.Point2;



public class TextVisual
{
	private static class MixedSizeCapsAttributedCharacterIterator extends Segment implements AttributedCharacterIterator
	{
		static HashSet<AttributedCharacterIterator.Attribute> attribKeys = null;
		
		protected String text;
		protected Font lowerCaseFont, upperCaseFont;
		protected HashMap<AttributedCharacterIterator.Attribute, Object> lowerCaseAttribs, upperCaseAttribs;
		
		
		public MixedSizeCapsAttributedCharacterIterator(String text, Font lowerCaseFont, Font upperCaseFont)
		{
			super( text.toUpperCase().toCharArray(), 0, text.length() );
			
			this.text = text;
			this.lowerCaseFont = lowerCaseFont;
			this.upperCaseFont = upperCaseFont;
			
			lowerCaseAttribs = new HashMap<AttributedCharacterIterator.Attribute, Object>();
			lowerCaseAttribs.put( TextAttribute.FONT, lowerCaseFont );
			upperCaseAttribs = new HashMap<AttributedCharacterIterator.Attribute, Object>();
			upperCaseAttribs.put( TextAttribute.FONT, upperCaseFont );
		}
		
		
		public Set<Attribute> getAllAttributeKeys()
		{
			if ( attribKeys == null )
			{
				attribKeys = new HashSet<AttributedCharacterIterator.Attribute>();
				attribKeys.add( TextAttribute.FONT );
			}
			
			return attribKeys;
		}

		public Object getAttribute(Attribute attrib)
		{
			if ( attrib == TextAttribute.FONT )
			{
				return Character.isUpperCase( text.charAt( getIndex() ) )  ?  upperCaseFont  :  lowerCaseFont;
			}
			else
			{
				return null;
			}
		}

		public Map<Attribute, Object> getAttributes()
		{
			return Character.isUpperCase( text.charAt( getIndex() ) )  ?  upperCaseAttribs  :  lowerCaseAttribs;
		}

		public int getRunLimit()
		{
			boolean bUpperCase = Character.isUpperCase( text.charAt( getIndex() ) );
			for (int i = getIndex() + 1; i < getEndIndex(); i++)
			{
				if ( bUpperCase != Character.isUpperCase( text.charAt( i ) ) )
				{
					return i;
				}
			}
			
			return getEndIndex();
		}

		public int getRunLimit(Attribute attrib)
		{
			if ( attrib == TextAttribute.FONT )
			{
				return getRunLimit();
			}
			else
			{
				return getEndIndex();
			}
		}

		public int getRunLimit(Set<? extends Attribute> attribs)
		{
			if ( attribs.contains( TextAttribute.FONT ) )
			{
				return getRunLimit();
			}
			else
			{
				return getEndIndex();
			}
		}

		public int getRunStart()
		{
			boolean bUpperCase = Character.isUpperCase( text.charAt( getIndex() ) );
			for (int i = getIndex() - 1; i >= 0; i--)
			{
				if ( bUpperCase != Character.isUpperCase( text.charAt( i ) ) )
				{
					return i + 1;
				}
			}
			
			return getBeginIndex();
		}

		public int getRunStart(Attribute arg0)
		{
			if ( arg0 == TextAttribute.FONT )
			{
				return getRunStart();
			}
			else
			{
				return getBeginIndex();
			}
		}

		public int getRunStart(Set<? extends Attribute> attribs)
		{
			if ( attribs.contains( TextAttribute.FONT ) )
			{
				return getRunStart();
			}
			else
			{
				return getBeginIndex();
			}
		}
	}
	
	
	
	
	private static class TextLayoutTable
	{
		private static class Key
		{
			private String text;
			private Font font;
			private boolean bMixedSizeCaps;
			private int hash;
			
			
			public Key(String text, Font font, boolean bMixedSizeCaps)
			{
				this.text = text;
				this.font = font;
				this.bMixedSizeCaps = bMixedSizeCaps;
				this.hash = tripleHash( text.hashCode(), font.hashCode(), new Boolean( bMixedSizeCaps ).hashCode() );
			}
			
			
			public boolean equals(Object x)
			{
				if ( x instanceof Key )
				{
					Key kx = (Key)x;
					return text.equals( kx.text )  &&  font.equals( kx.font )  &&  bMixedSizeCaps == kx.bMixedSizeCaps;
				}
				else
				{
					return false;
				}
			}
			
			public int hashCode()
			{
				return hash;
			}

		
		
			private static int tripleHash(int a, int b, int c)
			{
				int mult = 1000003;
				int x = 0x345678;
				x = ( x ^ c ) * mult;
				mult += 82520 + 4;
				x = ( x ^ b ) * mult;
				mult += 82520 + 2;
				x = ( x ^ a ) * mult;
				return x + 97351;
			}
		}
		
		
		
		private HashMap<Key, TextLayout> layoutTable;


		public TextLayoutTable()
		{
			layoutTable = new HashMap<Key, TextLayout>();
		}
		
		
		public TextLayout get(String text, Font font, boolean bMixedSizeCaps, FontRenderContext frc)
		{
			Key k = new Key( text, font, bMixedSizeCaps );
			
			TextLayout layout = layoutTable.get( k );
			if ( layout == null )
			{
				if ( bMixedSizeCaps )
				{
					Font upperCaseFont = font;
					Font lowerCaseFont = upperCaseFont.deriveFont( upperCaseFont.getSize2D() * 0.75f );

					MixedSizeCapsAttributedCharacterIterator charIter = new MixedSizeCapsAttributedCharacterIterator( text, lowerCaseFont, upperCaseFont );
					layout = new TextLayout( charIter, frc );
				}
				else
				{
					layout = new TextLayout( text, font, frc );
				}
				
				layoutTable.put( k, layout );
			}
			
			return layout;
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
	
	
	private TextLayout layout;
	private String text;
	private TextStyleSheet styleSheet;
	private TextVisualListener listener;
	private JComponent component;
	
	private static TextLayoutTable layoutTable = new TextLayoutTable();
	
	
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
			layout.draw( graphics, 0, layout.getAscent() );
			graphics.setColor( prevColour );
		}
	}
	
	public void drawCaret(Graphics2D graphics, int offset)
	{
		refreshLayout();
		if ( layout != null )
		{
			double ascent = layout.getAscent();
			graphics.translate( 0.0, ascent );

			Shape[] carets = layout.getCaretShapes( offset );
			graphics.translate( 0.0, ascent );
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
		refreshLayout();
		double h = 0.0;
		if ( layout != null )
		{
			h = layout.getAscent() + layout.getDescent();
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
		if ( layout != null )
		{
			x = layout.getBounds().getWidth();
			h = layout.getAscent() + layout.getDescent();
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
		if ( layout != null )
		{
			return layout.hitTestChar( (float)pos.x, (float)( pos.y - layout.getAscent() ) );
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
			
			layout = layoutTable.get( text, styleSheet.getFont(), styleSheet.getMixedSizeCaps(), frc );
		}
	}
}
