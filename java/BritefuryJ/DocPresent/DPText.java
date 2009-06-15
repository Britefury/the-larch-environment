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
		this( TextStyleSheet.defaultStyleSheet, text );
	}
	
	public DPText(TextStyleSheet styleSheet, String text)
	{
		super( styleSheet );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getPresentationArea(), text, styleSheet.getFont(), styleSheet.getMixedSizeCaps() );
	}
	
	
	
	public void setText(String text)
	{
		this.text = text;
		
		TextStyleSheet textStyleSheet = (TextStyleSheet)styleSheet;

		TextVisual v = TextVisual.getTextVisual( getPresentationArea(), text, textStyleSheet.getFont(), textStyleSheet.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			if ( isRealised() )
			{
				visual.realise( getPresentationArea() );
			}
	
			queueResize();
		}
	}
	
	public String getText()
	{
		return "'" + text + "'";
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
		int endIndex = to != null  ?  to.getIndex()  :  text.length();
		visual.drawSelection( graphics, startIndex, endIndex );
		popGraphicsTransform( graphics, current );
	}
	

	
	
	protected void updateRequisitionX()
	{
		visual.setBoxRequisitionX( layoutBox );
	}

	protected void updateRequisitionY()
	{
		visual.setBoxRequisitionY( layoutBox );
	}
	
	
	public int getMarkerRange()
	{
		return text.length();
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
}
