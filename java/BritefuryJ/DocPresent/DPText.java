//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextHitInfo;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditableEntry
{
	protected TextVisual visual;
	protected String text;
	
	
	public DPText(String text)
	{
		this( TextStyleSheet.defaultStyleSheet, text, text );
	}
	
	public DPText(String text, String textRepresentation)
	{
		this( TextStyleSheet.defaultStyleSheet, text, textRepresentation );
	}
	
	public DPText(TextStyleSheet styleSheet, String text)
	{
		this( styleSheet, text, text );
	}

	public DPText(TextStyleSheet styleSheet, String text, String textRepresentation)
	{
		super( styleSheet, textRepresentation );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getPresentationArea(), text, styleSheet.getFont(), styleSheet.getMixedSizeCaps() );
		
		layoutReqBox = visual.getRequisition();
	}
	
	
	
	public void setText(String text)
	{
		this.text = text;
		onTextModified();
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
		TextStyleSheet textStyleSheet = (TextStyleSheet)styleSheet;

		TextVisual v = TextVisual.getTextVisual( getPresentationArea(), text, textStyleSheet.getFont(), textStyleSheet.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			layoutReqBox = visual.getRequisition();
			if ( isRealised() )
			{
				visual.realise( getPresentationArea() );
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
		visual.realise( getPresentationArea() );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		TextStyleSheet textStyleSheet = (TextStyleSheet)styleSheet;

		Color prevColour = graphics.getColor();

		Color squiggleUnderlineColour = textStyleSheet.getSquiggleUnderlineColour();
		if ( squiggleUnderlineColour != null )
		{
			graphics.setColor( squiggleUnderlineColour );
			visual.drawSquiggleUnderline( graphics );
		}

		graphics.setColor( textStyleSheet.getColour() );
		visual.drawText( graphics );
		
		graphics.setColor( prevColour );
	}
	
	

	
	protected void updateRequisitionX()
	{
		layoutReqBox = visual.getRequisition();
	}

	protected void updateRequisitionY()
	{
		layoutReqBox = visual.getRequisition();
	}

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		AffineTransform current = pushGraphicsTransform( graphics );
		visual.drawCaret( graphics, c.getMarker().getIndex() );
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
		int startIndex = from != null  ?  from.getIndex()  :  0;
		int endIndex = to != null  ?  to.getIndex()  :  textRepresentation.length();
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


	protected Point2 getMarkerPosition(Marker marker)
	{
		int index = marker.getIndex();
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

		super.insertText( marker, x );

		onTextModified();
	}

	public void removeText(int index, int length)
	{
		index = Math.min( Math.max( index, 0 ), text.length() );
		length = Math.min( length, text.length() - index );
		text = text.substring( 0, index ) + text.substring( index + length );

		super.removeText( index, length );

		onTextModified();
	}
	

	public void replaceText(Marker marker, int length, String x)
	{
		int index = marker.getIndex();
		index = Math.min( Math.max( index, 0 ), text.length() );
		text = text.substring( 0, index )  +  x  +  text.substring( index + length );

		super.replaceText( marker, length, x );

		onTextModified();
	}
	
	public boolean clearText()
	{
		boolean bResult = super.clearText();
		
		setText( "" );
		
		return bResult;
	}
}
