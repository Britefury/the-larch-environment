//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextHitInfo;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeText;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditable
{
	protected TextVisual visual;
	protected String text;
	
	
	public DPText(String text)
	{
		this( TextStyleParams.defaultStyleParams, text, text );
	}
	
	public DPText(String text, String textRepresentation)
	{
		this( TextStyleParams.defaultStyleParams, text, textRepresentation );
	}
	
	public DPText(TextStyleParams styleParams, String text)
	{
		this(styleParams, text, text );
	}

	public DPText(TextStyleParams styleParams, String text, String textRepresentation)
	{
		super(styleParams, textRepresentation );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getRootElement(), this.text, styleParams.getFont(), styleParams.getMixedSizeCaps() );
		
		layoutNode = new LayoutNodeText( this );
	}
	
	
	
	public void setText(String text)
	{
		setText( text, text );
	}
	
	public void setText(String text, String textRepresentation)
	{
		this.text = text;
		setTextRepresentation( textRepresentation );
		onTextModified();
	}
	
	public String getText()
	{
		return text;
	}
	
	
	
	private void onTextModified()
	{
		TextStyleParams textStyleParams = (TextStyleParams) styleParams;

		TextVisual v = TextVisual.getTextVisual( getRootElement(), text, textStyleParams.getFont(), textStyleParams.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			LayoutNodeText layout = (LayoutNodeText)getLayoutNode();
			layout.setVisual( visual );
			if ( isRealised() )
			{
				visual.realise( getRootElement() );
			}
			
			queueResize();
		}
	}
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		return visual.hitTest( pos );
	}
	
	
	
	protected void onRealise()
	{
		super.onRealise();
		visual.realise( getRootElement() );
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
		if ( testFlag( FLAG_HOVER ) )
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
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		int index = c.getMarker().getIndex();
		if ( index < 0  ||  ( text.length() > 0  ?  ( index > text.length() )  :  ( index > 1 ) ) )
		{
			throw new RuntimeException( "DPText.drawCaret(): caret marker is out of range; " + index + " is not within the range[0-" + text.length() + "]." );
		}
		AffineTransform current = pushGraphicsTransform( graphics );
		visual.drawCaret( graphics, index );
		popGraphicsTransform( graphics, current );
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		visual.drawCaretAtStart( graphics );
		popGraphicsTransform( graphics, current );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		visual.drawCaretAtEnd( graphics );
		popGraphicsTransform( graphics, current );
	}



	//
	//
	// SELECTION METHODS
	//
	//
	
	public void drawSelection(Graphics2D graphics, Marker from, Marker to)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		int startIndex = from != null  ?  from.getClampedIndex()  :  0;
		int endIndex = to != null  ?  to.getClampedIndex()  :  textRepresentation.length();
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
		int index = marker.getClampedIndex();
		return visual.getCharacterBoundaryPosition( index );
	}
	
	
	
	//
	//
	// TEXT MODIFICATION METHODS
	//
	//
	
	public void insertText(Marker marker, String x)
	{
		int index = marker.getIndex();
		index = Math.min( Math.max( index, 0 ), text.length() );
		text = text.substring( 0, index ) + x + text.substring( index );
		onTextModified();

		super.insertText( marker, x );
	}

	public void removeText(int index, int length)
	{
		index = Math.min( Math.max( index, 0 ), text.length() );
		length = Math.min( length, text.length() - index );
		text = text.substring( 0, index ) + text.substring( index + length );
		onTextModified();

		super.removeText( index, length );
	}
	

	public void replaceText(Marker marker, int length, String x)
	{
		int index = marker.getIndex();
		index = Math.min( Math.max( index, 0 ), text.length() );
		text = text.substring( 0, index )  +  x  +  text.substring( index + length );
		onTextModified();

		super.replaceText( marker, length, x );
	}
	
	public boolean clearText()
	{
		boolean bResult = super.clearText();
		
		setText( "" );
		
		return bResult;
	}
	
	
	
	public String toString()
	{
		return super.toString()  +  " <" + text + ">";
	}
}
