//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextHitInfo;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldCascading;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditableEntry
{
	protected static ElementStyleSheetField fontField = DPStaticText.fontField;
	protected static ElementStyleSheetField paintField = DPStaticText.paintField;
	protected static ElementStyleSheetField squiggleUndelinePaintField = ElementStyleSheetField.newField( "squiggleUndelinePaint", Paint.class );
	protected static ElementStyleSheetField bMixedSizeCapsField = DPStaticText.bMixedSizeCapsField;
	
	protected static StyleSheetValueFieldCascading fontValueField = DPStaticText.fontValueField;
	protected static StyleSheetValueFieldCascading paintValueField = DPStaticText.paintValueField;
	protected static StyleSheetValueFieldCascading squiggleUndelinePaintValueField = StyleSheetValueFieldCascading.newField( "squiggleUndelinePaint", Paint.class, null, squiggleUndelinePaintField );
	protected static StyleSheetValueFieldCascading bMixedSizeCapsValueField = DPStaticText.bMixedSizeCapsValueField;
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_Text = useStyleSheetFields_Element.join( fontValueField, paintValueField, squiggleUndelinePaintValueField, bMixedSizeCapsValueField );
	
	
	protected TextVisual visual;
	protected String text;
	
	
	public DPText(String text)
	{
		this( null, text, text );
	}
	
	public DPText(String text, String textRepresentation)
	{
		this( null, text, textRepresentation );
	}
	
	public DPText(ElementStyleSheet styleSheet, String text)
	{
		this( styleSheet, text, text );
	}

	public DPText(ElementStyleSheet styleSheet, String text, String textRepresentation)
	{
		super( styleSheet, textRepresentation );
		
		this.text = text;
		
		visual = null;
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
	
	
	
	protected TextVisual createTextVisual()
	{
		return TextVisual.getTextVisual( getPresentationArea(), text, (Font)styleSheetValues.get( fontValueField ), (Boolean)styleSheetValues.get( bMixedSizeCapsValueField ) );
	}
	
	protected Paint getTextPaint()
	{
		return (Paint)styleSheetValues.get( paintValueField );
	}
	
	
	private void onTextModified()
	{
		if ( isRealised() )
		{
			TextVisual v = createTextVisual();
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
		else
		{
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
		visual = createTextVisual();
		visual.realise( getPresentationArea() );
	}
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
		visual = null;
		super.onUnrealise( unrealiseRoot );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		if ( visual != null )
		{
			Paint prevPaint = graphics.getPaint();
	
			Paint squiggleUnderlinePaint = (Paint)styleSheetValues.get( squiggleUndelinePaintValueField );;
			if ( squiggleUnderlinePaint != null )
			{
				graphics.setPaint( squiggleUnderlinePaint );
				visual.drawSquiggleUnderline( graphics );
			}
	
			graphics.setPaint( (Paint)styleSheetValues.get( paintValueField ) );
			visual.drawText( graphics );
			
			graphics.setPaint( prevPaint );
		}
	}
	
	

	
	protected void updateRequisitionX()
	{
		layoutReqBox = visual != null  ?  visual.getRequisition()  :  new LReqBox();
	}

	protected void updateRequisitionY()
	{
		layoutReqBox = visual != null  ?  visual.getRequisition()  :  new LReqBox();
	}

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		if ( visual != null )
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
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		if ( visual != null )
		{
			AffineTransform current = pushGraphicsTransform( graphics );
			visual.drawCaretAtStart( graphics );
			popGraphicsTransform( graphics, current );
		}
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		if ( visual != null )
		{
			AffineTransform current = pushGraphicsTransform( graphics );
			visual.drawCaretAtEnd( graphics );
			popGraphicsTransform( graphics, current );
		}
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
	
	
	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_Text;
	}

	
	public static ElementStyleSheet styleSheet(Font font, Paint paint)
	{
		return new ElementStyleSheet( new String[] { "font", "paint" }, new Object[] { font, paint } );
	}

	public static ElementStyleSheet styleSheet(Font font, Paint paint, Paint squiggleUnderlinePaint)
	{
		return new ElementStyleSheet( new String[] { "font", "paint", "squiggleUndelinePaint" }, new Object[] { font, paint, squiggleUnderlinePaint } );
	}

	public static ElementStyleSheet styleSheet(Font font, Paint paint, boolean bMixedSizeCaps)
	{
		return new ElementStyleSheet( new String[] { "font", "paint", "bMixedSizeCaps" }, new Object[] { font, paint, bMixedSizeCaps } );
	}

	public static ElementStyleSheet styleSheet(Font font, Paint paint, Paint squiggleUnderlinePaint, boolean bMixedSizeCaps)
	{
		return new ElementStyleSheet( new String[] { "font", "paint", "squiggleUndelinePaint", "bMixedSizeCaps" }, new Object[] { font, paint, squiggleUnderlinePaint, bMixedSizeCaps } );
	}
}
