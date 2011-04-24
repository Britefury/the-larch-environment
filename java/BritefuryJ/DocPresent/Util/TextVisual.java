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
import java.text.AttributedString;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.HashUtils;



public class TextVisual
{
	private static final float SMALL_CAPS_FONT_SCALE = 0.77f;
	

	private static class Key
	{
		private final String text;
		private final Font font;
		private final int flags;
		private final int hash;
		
		
		public Key(String text, Font font, int flags)
		{
			this.text = text;
			this.font = font;
			this.flags = flags;
			this.hash = HashUtils.tripleHash( text.hashCode(), font.hashCode(), flags );
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
				return text.equals( kx.text )  &&  font.equals( kx.font )  &&  flags == kx.flags;
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
		
		
		public TextVisual get(String text, Font font, int flags)
		{
			Key k = new Key( text, font, flags );
			
			WeakReference<TextVisual> visualRef = layoutTable.get( k );
			TextVisual visual = visualRef != null  ?  visualRef.get()  :  null;
			if ( visualRef == null  ||  visual == null )
			{
				visual = new TextVisual( text, font, flags );
				layoutTable.put( k, new WeakReference<TextVisual>( visual, refQueue ) );
				
				clean();
			}
			
			return visual;
		}


		@SuppressWarnings("unchecked")
		private void clean()
		{
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
	}
	
	
	
	
	
	public static class UnrealisedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	private static final int FLAG_UNDERLINE = 0x1;
	private static final int FLAG_STRIKETHROUGH = 0x2;
	private static final int FLAG_MIXEDSIZECAPS = 0x4;
	
	
	private static final TextVisualTable visualTable = new TextVisualTable();
	
	
	private TextLayout layout;
	private Path2D.Double squiggleUnderlineShape;
	private final String text;
	private final Font font;
	private final int flags;
	private LReqBox reqBox;
	

	
	
	private TextVisual(String text, Font font, int flags)
	{
		this.text = text;
		this.font = font;
		this.flags = flags;
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
	
	
	
	private AttributedString createAttributedString()
	{
		AttributedString str;
		
		if ( text.length() == 0 )
		{
			throw new RuntimeException( "TextVisual.createAttributeString cannot operate on empty string" );
		}
		
		if ( testFlag( FLAG_MIXEDSIZECAPS ) )
		{
			str = new AttributedString( text.toUpperCase() );

			Font upperCaseFont = font;
			Font lowerCaseFont = upperCaseFont.deriveFont( upperCaseFont.getSize2D() * SMALL_CAPS_FONT_SCALE );
			
			boolean spanIsUppercase = Character.isUpperCase( text.charAt( 0 ) );
			int spanStart = 0;
			int spanEnd = 0;
			for (int i = 1; i < text.length(); i++)
			{
				spanEnd = i;
				
				char c = text.charAt( i );
				boolean uppercase = Character.isUpperCase( c );
				if ( uppercase != spanIsUppercase )
				{
					str.addAttribute( TextAttribute.FONT, spanIsUppercase  ?  upperCaseFont  :  lowerCaseFont, spanStart, spanEnd );
					spanStart = spanEnd = i;
					spanIsUppercase = uppercase;
				}
			}
			str.addAttribute( TextAttribute.FONT, spanIsUppercase  ?  upperCaseFont  :  lowerCaseFont, spanStart, text.length() );
		}
		else
		{
			str = new AttributedString( text );
			str.addAttribute( TextAttribute.FONT, font );
		}
		
		if ( testFlag( FLAG_UNDERLINE ) )
		{
			str.addAttribute( TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON );
		}
		
		if ( testFlag( FLAG_STRIKETHROUGH ) )
		{
			str.addAttribute( TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON );
		}
		
		return str;
	}
	
	
	
	private void computeRequisition()
	{
		if ( text.length() > 0 )
		{
			FontRenderContext frc = new FontRenderContext( null, true, true );
			
			if ( flags != 0 )
			{
				AttributedString s = createAttributedString();
				layout = new TextLayout( s.getIterator(), frc );
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
	
	
	
	
	public static TextVisual getTextVisual(String text, Font font, int flags)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}
		
		return visualTable.get( text, font, flags );
	}
	
	public static TextVisual getTextVisual(String text, Font font, boolean bUnderline, boolean bStrikethrough, boolean bMixedSizeCaps)
	{
		return getTextVisual( text, font, buildFlags( bUnderline, bStrikethrough, bMixedSizeCaps) );
	}
	
	
	
	private boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
	
	
	public static int buildFlags(boolean bUnderline, boolean bStrikethrough, boolean bMixedsizeCaps)
	{
		int f = 0;
		f |= bUnderline  ?  FLAG_UNDERLINE  :  0;
		f |= bStrikethrough  ?  FLAG_STRIKETHROUGH  :  0;
		f |= bMixedsizeCaps  ?  FLAG_MIXEDSIZECAPS  :  0;
		return f;
	}
}
