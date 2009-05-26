//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
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
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.HashUtils;



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
			this.hash = HashUtils.tripleHash( text.hashCode(), font.hashCode(), new Boolean( bMixedSizeCaps ).hashCode() );
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
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
	}
	
	

	
	private static class TextVisualTable
	{
		private HashMap<Key, WeakReference<TextVisual>> layoutTable;
		private ReferenceQueue<TextVisual> refQueue;


		public TextVisualTable()
		{
			layoutTable = new HashMap<Key, WeakReference<TextVisual>>();
			refQueue = new ReferenceQueue<TextVisual>();
		}
		
		
		@SuppressWarnings("unchecked")
		public TextVisual get(String text, Font font, boolean bMixedSizeCaps)
		{
			Key k = new Key( text, font, bMixedSizeCaps );
			
			WeakReference<TextVisual> visualRef = layoutTable.get( k );
			TextVisual visual = visualRef != null  ?  visualRef.get()  :  null;
			if ( visualRef == null  ||  visual == null )
			{
				visual = new TextVisual( text, font, bMixedSizeCaps );
				layoutTable.put( k, new WeakReference<TextVisual>( visual, refQueue ) );
				
				// Check if there are any unused entries; if so, build a list of set references
				WeakReference<TextVisual> deadRef = (WeakReference<TextVisual>)refQueue.poll();
				HashSet<WeakReference<TextVisual>> deadReferences = null;
				while ( deadRef != null )
				{
					// Found an unused entry; create the dead references set, if it is not already created
					if ( deadReferences == null )
					{
						deadReferences = new HashSet<WeakReference<TextVisual>>();
					}
					deadReferences.add( deadRef );
					
					deadRef = (WeakReference<TextVisual>)refQueue.poll();
				}
				
				if ( deadReferences != null )
				{
					// Map the set of dead references, back to their keys
					HashSet<Key> deadKeys = new HashSet<Key>();
					for (Map.Entry<Key, WeakReference<TextVisual>> entry: layoutTable.entrySet())
					{
						if ( deadReferences.contains( entry.getValue() ) )
						{
							deadKeys.add( entry.getKey() );
						}
					}
					
					// Remove them
					for (Key deadKey: deadKeys)
					{
						layoutTable.remove( deadKey );
					}
				}
			}
			
			return visual;
		}
	}
	
	
	
	
	
	public static class UnrealisedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	private static HashMap<DPPresentationArea, TextVisualTable> visualTables = new  HashMap<DPPresentationArea, TextVisualTable>();
	
	
	private TextLayout layout;
	private Path2D.Double squiggleUnderlineShape;
	private String text;
	private Font font;
	private boolean bMixedSizeCaps;
	private boolean bRealised;
	private HMetrics hmetrics;
	private VMetrics vmetrics;
	

	
	
	private TextVisual(String text, Font font, boolean bMixedSizeCaps)
	{
		this.text = text;
		this.font = font;
		this.bMixedSizeCaps = bMixedSizeCaps;
		this.hmetrics = new HMetrics();
		this.vmetrics = new VMetricsTypeset();
	}
	
	
	
	public String getText()
	{
		return text;
	}
	
	
	
	public HMetrics getHMetrics()
	{
		return hmetrics;
	}
	
	public VMetrics getVMetrics()
	{
		return vmetrics;
	}
	
	
	
	public void realise(DPPresentationArea a)
	{
		if ( !bRealised )
		{
			JComponent component = a.getComponent();
			assert component != null;
			if ( text.length() > 0 )
			{
				Graphics2D graphics = (Graphics2D)component.getGraphics();
				FontRenderContext frc = graphics.getFontRenderContext();
				
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

				double width = layout.getBounds().getWidth();
				double ascent = layout.getAscent(), descent = layout.getDescent();
				hmetrics = new HMetrics( width, layout.getAdvance() - width );
				vmetrics = new VMetricsTypeset( layout.getAscent(), layout.getDescent(), layout.getLeading() );
				
				// Squiggle shape
				squiggleUnderlineShape = new Path2D.Double();
				int numSquiggleSegments = (int)( width / descent  +  0.5 );
				squiggleUnderlineShape.moveTo( 0.0, ascent + descent );
				double squiggleDeltaX = width / (double)numSquiggleSegments;
				double squiggleX = squiggleDeltaX;
				
				for (int i = 0; i < numSquiggleSegments; i++)
				{
					squiggleUnderlineShape.lineTo( squiggleX, ( i % 2 ) == 0  ?  ascent  :  ascent + descent );
					squiggleX += squiggleDeltaX;
				}
			}
			else
			{
				Graphics2D graphics = (Graphics2D)component.getGraphics();
				assert graphics != null;
				FontRenderContext frc = graphics.getFontRenderContext();
				LineMetrics lineMetrics = font.getLineMetrics( "", frc );
				
				hmetrics = new HMetrics();
				vmetrics = new VMetricsTypeset( lineMetrics.getAscent(), lineMetrics.getDescent(), lineMetrics.getLeading() );
				
				squiggleUnderlineShape = null;
			}

			bRealised = true;
		}
	}
	
	
	
	
	public void drawText(Graphics2D graphics)
	{
		if ( layout != null )
		{
			layout.draw( graphics, 0, layout.getAscent() );
		}
	}
	
	public void drawSquiggleUnderline(Graphics2D graphics)
	{
		if ( squiggleUnderlineShape != null )
		{
			graphics.draw( squiggleUnderlineShape );
		}
	}
	
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void drawCaret(Graphics2D graphics, int offset)
	{
		if ( layout != null )
		{
			double ascent = layout.getAscent();
			graphics.translate( 0.0, ascent );

			Shape[] carets = layout.getCaretShapes( offset );
			graphics.draw( carets[0] );
			if ( carets[1] != null )
			{
				graphics.draw( carets[1] );
			}
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = font.getLineMetrics( "", frc );
			Line2D.Double line = new Line2D.Double( 0.0, 0.0, 0.0, lineMetrics.getAscent() + lineMetrics.getDescent() );
			graphics.draw( line );
		}
	}
	
	public void drawCaretAtStart(Graphics2D graphics)
	{
		double h = 0.0;
		if ( layout != null )
		{
			h = layout.getAscent() + layout.getDescent();
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = font.getLineMetrics( "", frc );
			h = lineMetrics.getAscent() + lineMetrics.getDescent();
		}
		graphics.draw( new Line2D.Double( 0.0, 0.0, 0.0, h ) );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		double x = 0.0, h = 0.0;
		if ( layout != null )
		{
			x = layout.getBounds().getWidth();
			h = layout.getAscent() + layout.getDescent();
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = font.getLineMetrics( "", frc );
			x = 0.0;
			h = lineMetrics.getAscent() + lineMetrics.getDescent();
		}
		graphics.draw( new Line2D.Double( x, 0.0, x, h ) );
	}
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public void drawSelection(Graphics2D graphics, int startIndex, int endIndex)
	{
		if ( layout != null )
		{
			AffineTransform transform = graphics.getTransform();
			double ascent = layout.getAscent();
			graphics.translate( 0.0, ascent );
			
			Shape shape = layout.getLogicalHighlightShape( startIndex, endIndex );
			graphics.fill( shape );
			
			graphics.setTransform( transform );
		}
	}

	
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		if ( layout != null )
		{
			return layout.hitTestChar( (float)pos.x, (float)( pos.y - layout.getAscent() ) );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	public static TextVisual getTextVisual(DPPresentationArea area, String text, Font font, boolean bMixedSizeCaps)
	{
		TextVisualTable table = visualTables.get( area );
		if ( table == null )
		{
			table = new TextVisualTable();
			visualTables.put( area, table );
		}
		
		return table.get( text, font, bMixedSizeCaps );
	}
}
