//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.Segment;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.HashUtils;



public class TextVisual
{
	private static final float SMALL_CAPS_FONT_SCALE = 0.77f;
	
	private static class MixedSizeCapsAttributedCharacterIterator extends Segment implements AttributedCharacterIterator
	{
		static HashSet<AttributedCharacterIterator.Attribute> attribKeys = null;
		
		protected final String text;
		protected final Font lowerCaseFont, upperCaseFont;
		protected final HashMap<AttributedCharacterIterator.Attribute, Object> lowerCaseAttribs, upperCaseAttribs;
		
		
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
		
		
		@Override
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
				return Character.isLowerCase( text.charAt( getIndex() ) )  ?  lowerCaseFont  :  upperCaseFont;
			}
			else
			{
				return null;
			}
		}

		public Map<Attribute, Object> getAttributes()
		{
			return Character.isLowerCase( text.charAt( getIndex() ) )  ?  lowerCaseAttribs  :  upperCaseAttribs;
		}

		public int getRunLimit()
		{
			boolean bLowerCase = Character.isLowerCase( text.charAt( getIndex() ) );
			for (int i = getIndex() + 1; i < getEndIndex(); i++)
			{
				if ( bLowerCase != Character.isLowerCase( text.charAt( i ) ) )
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
			boolean bLowerCase = Character.isLowerCase( text.charAt( getIndex() ) );
			for (int i = getIndex() - 1; i >= 0; i--)
			{
				if ( bLowerCase != Character.isLowerCase( text.charAt( i ) ) )
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
		private final String text;
		private final Font font;
		private final boolean bMixedSizeCaps;
		private final int hash;
		
		
		public Key(String text, Font font, boolean bMixedSizeCaps)
		{
			this.text = text;
			this.font = font;
			this.bMixedSizeCaps = bMixedSizeCaps;
			this.hash = HashUtils.tripleHash( text.hashCode(), font.hashCode(), Boolean.valueOf( bMixedSizeCaps ).hashCode() );
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
	
	
	private static final HashMap<PresentationComponent.RootElement, TextVisualTable> visualTables = new  HashMap<PresentationComponent.RootElement, TextVisualTable>();
	
	
	private TextLayout layout;
	private Path2D.Double squiggleUnderlineShape;
	private final String text;
	private final Font font;
	private final boolean bMixedSizeCaps;
	private LReqBox reqBox;
	

	
	
	private TextVisual(String text, Font font, boolean bMixedSizeCaps)
	{
		this.text = text;
		this.font = font;
		this.bMixedSizeCaps = bMixedSizeCaps;
		reqBox = new LReqBox();
		computeRequisition();
	}
	
	
	
	public String getText()
	{
		return text;
	}
	
	
	public LReqBox getRequisition()
	{
		return reqBox;
	}
	
	
	
	private void computeRequisition()
	{
		if ( text.length() > 0 )
		{
			FontRenderContext frc = new FontRenderContext( null, true, true );
			
			if ( bMixedSizeCaps )
			{
				Font upperCaseFont = font;
				Font lowerCaseFont = upperCaseFont.deriveFont( upperCaseFont.getSize2D() * SMALL_CAPS_FONT_SCALE );

				MixedSizeCapsAttributedCharacterIterator charIter = new MixedSizeCapsAttributedCharacterIterator( text, lowerCaseFont, upperCaseFont );
				layout = new TextLayout( charIter, frc );
			}
			else
			{
				layout = new TextLayout( text, font, frc );
			}

			double width = layout.getBounds().getWidth();
			double ascent = layout.getAscent(), descent = layout.getDescent();
			
			reqBox.setRequisitionX( width, layout.getAdvance() );
			reqBox.setRequisitionY( layout.getAscent() + layout.getDescent(), layout.getLeading(), layout.getAscent() );
			
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
			FontRenderContext frc = new FontRenderContext( null, true, true );
			LineMetrics lineMetrics = font.getLineMetrics( "", frc );
			
			reqBox.setRequisitionX( 0.0, 0.0 );
			reqBox.setRequisitionY( lineMetrics.getAscent() + lineMetrics.getDescent(), lineMetrics.getLeading(), lineMetrics.getAscent() );
			
			squiggleUnderlineShape = null;
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
			Stroke s = graphics.getStroke();
			graphics.setStroke( new BasicStroke( 1.0f ) );
			graphics.draw( squiggleUnderlineShape );
			graphics.setStroke( s );
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
			if ( offset < 0  ||  offset > layout.getCharacterCount() )
			{
				throw new RuntimeException( "TextVisual.drawCaret(): offset is out of range; offset=" + offset + ", text range=[0-" + text.length() + "], layout range=[0-" + layout.getCharacterCount() + "]." );
			}
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
	
	public Point2 getCaretPosition(Graphics2D graphics, int offset)
	{
		if ( layout != null )
		{
			if ( offset < 0  ||  offset > layout.getCharacterCount() )
			{
				throw new RuntimeException( "TextVisual.getCaretPosition(): offset is out of range; offset=" + offset + ", text range=[0-" + text.length() + "], layout range=[0-" + layout.getCharacterCount() + "]." );
			}
			TextHitInfo hit = layout.getNextLeftHit( offset );
			float caretInfo[] = layout.getCaretInfo( hit );
			return new Point2( caretInfo[0],  ( layout.getAscent() + layout.getDescent() ) * 0.5 );
		}
		else
		{
			FontRenderContext frc = graphics.getFontRenderContext();
			LineMetrics lineMetrics = font.getLineMetrics( "", frc );
			return new Point2( 0.0, ( lineMetrics.getAscent() + lineMetrics.getDescent() ) * 0.5 );
		}
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
	
	
	
	public Point2 getCharacterBoundaryPosition(int characterIndex)
	{
		if ( layout != null )
		{
			TextHitInfo hit = null;
			if ( characterIndex == 0 )
			{
				hit = layout.getNextRightHit( characterIndex );
				hit = layout.getNextLeftHit( hit );
			}
			else
			{
				hit = layout.getNextLeftHit( characterIndex );
				hit = layout.getNextRightHit( hit );
			}
			Point2D.Double point = new Point2D.Double();
			layout.hitToPoint( hit, point );
			return new Point2( point.x, point.y );
		}
		else
		{
			return new Point2( reqBox.getReqMinWidth() * 0.5, reqBox.getReqHeight() * 0.5 );
		}
	}
	
	
	
	
	public static TextVisual getTextVisual(PresentationComponent.RootElement root, String text, Font font, boolean bMixedSizeCaps)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}
		
		TextVisualTable table = visualTables.get( root );
		if ( table == null )
		{
			table = new TextVisualTable();
			visualTables.put( root, table );
		}
		
		return table.get( text, font, bMixedSizeCaps );
	}
}
