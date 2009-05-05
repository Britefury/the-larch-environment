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
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
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
		System.out.println( "DPText.setText()" );
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
		Color prevColour = graphics.getColor();
		TextStyleSheet textStyleSheet = (TextStyleSheet)styleSheet;
		graphics.setColor( textStyleSheet.getColour() );
		visual.draw( graphics );
		graphics.setColor( prevColour );
	}
	
	
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



	
	protected HMetrics computeMinimumHMetrics()
	{
		return visual.getHMetrics();
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		return visual.getHMetrics();
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return visual.getVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return visual.getVMetrics();
	}

	
	protected int getMarkerRange()
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
