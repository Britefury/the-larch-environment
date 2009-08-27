//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;

public class DPStaticText extends DPStatic
{
	protected TextVisual visual;
	protected String text;
	
	
	public DPStaticText(String text)
	{
		this( StaticTextStyleSheet.defaultStyleSheet, text );
	}
	
	public DPStaticText(StaticTextStyleSheet styleSheet, String text)
	{
		super( styleSheet );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getPresentationArea(), text, styleSheet.getFont(), styleSheet.getMixedSizeCaps() );
		
		layoutReqBox = visual.getRequisition( 0 );
	}
	
	
	
	public void setText(String text)
	{
		this.text = text;
		onTextModified();
	}
	
	public String getText()
	{
		return text;
	}
	
	
	
	public DPWidget align(HAlignment hAlign, VAlignment vAlign)
	{
		layoutReqBox = visual.getRequisition( ElementAlignment.intValue( hAlign, vAlign ) );
		return this;
	}

	public DPWidget alignH(HAlignment hAlign)
	{
		layoutReqBox = visual.getRequisition( ElementAlignment.intValue( hAlign, ElementAlignment.getVAlignment( layoutReqBox.getAlignmentIntValue() ) ) );
		return this;
	}
	
	public DPWidget alignV(VAlignment vAlign)
	{
		layoutReqBox = visual.getRequisition( ElementAlignment.intValue( ElementAlignment.getHAlignment( layoutReqBox.getAlignmentIntValue() ), vAlign ) );
		return this;
	}

	
	
	private void onTextModified()
	{
		StaticTextStyleSheet textStyleSheet = (StaticTextStyleSheet)styleSheet;

		TextVisual v = TextVisual.getTextVisual( getPresentationArea(), text, textStyleSheet.getFont(), textStyleSheet.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			layoutReqBox = visual.getRequisition( layoutReqBox.getAlignmentIntValue() );
			if ( isRealised() )
			{
				visual.realise( getPresentationArea() );
			}
	
			queueResize();
		}
	}
	
	
	
	protected void onRealise()
	{
		super.onRealise();
		visual.realise( getPresentationArea() );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		StaticTextStyleSheet textStyleSheet = (StaticTextStyleSheet)styleSheet;

		Paint prevColour = graphics.getPaint();

		AffineTransform prevTransform = null;
		double deltaY = layoutAllocBox.getAllocationAscent()  -  layoutReqBox.getReqAscent();
		if ( deltaY != 0.0 )
		{
			prevTransform = graphics.getTransform();
			graphics.translate( 0.0, deltaY );
		}
		
		
		graphics.setPaint( textStyleSheet.getTextPaint() );
		visual.drawText( graphics );
		
		
		if ( deltaY != 0.0 )
		{
			graphics.setTransform( prevTransform );
		}
		
		graphics.setPaint( prevColour );
	}
	
	

	
	protected void updateRequisitionX()
	{
		layoutReqBox = visual.getRequisition( layoutReqBox.getAlignmentIntValue() );
	}

	protected void updateRequisitionY()
	{
		layoutReqBox = visual.getRequisition( layoutReqBox.getAlignmentIntValue() );
	}
}
