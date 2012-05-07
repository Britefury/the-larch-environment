//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextHitInfo;
import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.LayoutTree.LayoutNode;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeText;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.TextStyleParams;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LSText extends LSContentLeafEditable
{
	protected final static int FLAGS_TEXT_START = FLAGS_CONTENTLEAFEDITABLE_END;
	protected final static int FLAGS_TEXT_END = FLAGS_TEXT_START;

	
	protected TextVisual visual;
	protected String text;
	
	
	public LSText(String text)
	{
		this( TextStyleParams.defaultStyleParams, text, text );
	}
	
	public LSText(String text, String textRepresentation)
	{
		this( TextStyleParams.defaultStyleParams, text, textRepresentation );
	}
	
	public LSText(TextStyleParams styleParams, String text)
	{
		this(styleParams, text, text );
	}

	public LSText(TextStyleParams styleParams, String text, String textRepresentation)
	{
		super(styleParams, textRepresentation );
		
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		this.text = text;
		
		visual = TextVisual.getTextVisual( this.text, styleParams.getFont(), styleParams.getUnderline(), styleParams.getStrikethrough(), styleParams.getMixedSizeCaps() );
		
		layoutNode = new LayoutNodeText( this );
	}
	
	

	//
	// Text access / modification
	//
	
	public void setText(String text)
	{
		setText( text, text );
	}
	
	public void setText(String text, String textRepresentation)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		this.text = text;
		setLeafTextRepresentation( textRepresentation );
		onTextModified();
	}
	
	public String getText()
	{
		return text;
	}
	
	
	
	private void onTextModified()
	{
		TextStyleParams textStyleParams = (TextStyleParams) styleParams;

		TextVisual v = TextVisual.getTextVisual( text, textStyleParams.getFont(), textStyleParams.getUnderline(), textStyleParams.getStrikethrough(), textStyleParams.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			LayoutNodeText layout = (LayoutNodeText)getLayoutNode();
			layout.setVisual( visual );
			
			queueResize();
		}
	}
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		return visual.hitTest( pos.sub( new Vector2( 0.0, deltaY ) ) );
	}
	
	
	
	public boolean isRedrawRequiredOnHover()
	{
		TextStyleParams s = (TextStyleParams)styleParams;
		return super.isRedrawRequiredOnHover()  ||  s.getHoverTextPaint() != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		TextStyleParams textStyleParams = (TextStyleParams)styleParams;

		Paint prevPaint = graphics.getPaint();

		AffineTransform prevTransform = null;
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		if ( deltaY != 0.0 )
		{
			prevTransform = graphics.getTransform();
			graphics.translate( 0.0, deltaY );
		}

		
		Paint squiggleUnderlinePaint = textStyleParams.getSquiggleUnderlinePaint();
		if ( squiggleUnderlinePaint != null )
		{
			graphics.setPaint( squiggleUnderlinePaint );
			visual.drawSquiggleUnderline( graphics );
		}


		Paint textPaint;
		if ( isHoverActive() )
		{
			Paint hoverSymbolPaint = textStyleParams.getHoverTextPaint();
			textPaint = hoverSymbolPaint != null  ?  hoverSymbolPaint  :  textStyleParams.getTextPaint();
		}
		else
		{
			textPaint = textStyleParams.getTextPaint();
		}

		graphics.setPaint( textPaint );
		visual.drawText( graphics );
		
		if ( deltaY != 0.0 )
		{
			graphics.setTransform( prevTransform );
		}


		graphics.setPaint( prevPaint );
	}
	
	

	
	public TextVisual getVisual()
	{
		return visual;
	}
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	@Override
	public void drawCaret(Graphics2D graphics, Marker c)
	{
		int index = c.getIndex();
		if ( index < 0  ||  ( text.length() > 0  ?  ( index > text.length() )  :  ( index > 1 ) ) )
		{
			throw new RuntimeException( "LSText.drawCaret(): caret marker is out of range; " + index + " is not within the range[0-" + text.length() + "]." );
		}
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
		graphics.translate( 0.0, deltaY );
		visual.drawCaret( graphics, index );
		popGraphicsTransform( graphics, current );
	}




	//
	//
	// TEXT SELECTION METHODS
	//
	//
	
	public void drawTextSelection(Graphics2D graphics, int startIndex, int endIndex)
	{
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		AffineTransform current = pushLocalToRootGraphicsTransform( graphics );
		graphics.translate( 0.0, deltaY );
		visual.drawSelection( graphics, startIndex, endIndex );
		popGraphicsTransform( graphics, current );
	}
	

	
	
	public int getMarkerRange()
	{
		return textRepresentation.length();
	}

	public int getMarkerPositonForPoint(Point2 localPos)
	{
		TextHitInfo info = hitTest( localPos );
		if ( info != null )
		{
			return info.getInsertionIndex();
		}
		else
		{
			return 0;
		}
	}


	public Point2 getMarkerPosition(Marker marker)
	{
		if ( marker.getElement() != this )
		{
			throw new RuntimeException( "Marker is not within the bounds of this element" );
		}
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		int index = marker.getClampedIndex();
		return visual.getCharacterBoundaryPosition( index ).add( new Vector2( 0.0, deltaY ) );
	}
	
	
	
	//
	//
	// TEXT MODIFICATION METHODS
	//
	//
	
	@Override
	public void insertText(int index, String x)
	{
		text = text.substring( 0, index ) + x + text.substring( index );
		onTextModified();

		super.insertText( index, x );
	}

	@Override
	public void removeText(int index, int length)
	{
		text = text.substring( 0, index ) + text.substring( index + length );
		onTextModified();

		super.removeText( index, length );
	}
	

	@Override
	public void replaceText(int index, int length, String x)
	{
		text = text.substring( 0, index )  +  x  +  text.substring( index + length );
		onTextModified();

		super.replaceText( index, length, x );
	}
	
	
	
	public boolean deleteText()
	{
		if ( isEditable() )
		{
			boolean bResult = super.deleteText();
			
			setText( "" );
			
			return bResult;
		}
		else
		{
			return false;
		}
	}
	
	
	
	public String toString()
	{
		return super.toString()  +  " <" + text + ">";
	}
}
