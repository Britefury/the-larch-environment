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
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditableEntry
{
	protected TextVisual visual;
	protected String text;
	
	
	public DPText(ElementContext context, String text)
	{
		this( context, TextStyleSheet.defaultStyleSheet, text, text );
	}
	
	public DPText(ElementContext context, String text, String textRepresentation)
	{
		this( context, TextStyleSheet.defaultStyleSheet, text, textRepresentation );
	}
	
	public DPText(ElementContext context, TextStyleSheet styleSheet, String text)
	{
		this( context, styleSheet, text, text );
	}

	public DPText(ElementContext context, TextStyleSheet styleSheet, String text, String textRepresentation)
	{
		super( context, styleSheet, textRepresentation );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getPresentationArea(), this.text, styleSheet.getFont(), styleSheet.getMixedSizeCaps() );
		
		layoutNode = new LayoutNodeText( this );
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
			LayoutNodeText layout = (LayoutNodeText)getLayoutNode();
			layout.setVisual( visual );
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

		Paint prevPaint = graphics.getPaint();

		AffineTransform prevTransform = null;
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		if ( deltaY != 0.0 )
		{
			prevTransform = graphics.getTransform();
			graphics.translate( 0.0, deltaY );
		}

		
		Paint squiggleUnderlinePaint = textStyleSheet.getSquiggleUnderlinePaint();
		if ( squiggleUnderlinePaint != null )
		{
			graphics.setPaint( squiggleUnderlinePaint );
			visual.drawSquiggleUnderline( graphics );
		}

		graphics.setPaint( textStyleSheet.getTextPaint() );
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


	protected Point2 getMarkerPosition(Marker marker)
	{
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
