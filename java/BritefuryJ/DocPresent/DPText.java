//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.font.TextHitInfo;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditable implements TextVisual.TextVisualListener
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
		
		visual = new TextVisual( text, styleSheet, this );
	}
	
	
	
	public void setText(String text)
	{
		this.text = text;
		visual.setText( text );
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
		visual.realise( presentationArea );
	}
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
		super.onUnrealise( unrealiseRoot );
		visual.unrealise();
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		visual.draw( graphics );
	}
	
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		visual.drawCaret( graphics, c.getMarker().getIndex() );
	}

	public void drawCaretAtStart(Graphics2D graphics)
	{
		visual.drawCaretAtStart( graphics );
	}

	public void drawCaretAtEnd(Graphics2D graphics)
	{
		visual.drawCaretAtEnd( graphics );
	}



	
	protected HMetrics computeMinimumHMetrics()
	{
		return visual.computeHMetrics();
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		return visual.computeHMetrics();
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return visual.computeVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return visual.computeVMetrics();
	}

	
	public void textVisualRequestRedraw(TextVisual t)
	{
		queueFullRedraw();
	}


	public void textVisualRequestResize(TextVisual t)
	{
		queueResize();
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
