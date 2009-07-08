//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;

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
		
		layoutReqBox = visual.getRequisition();
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
	
	
	
	private void onTextModified()
	{
		StaticTextStyleSheet textStyleSheet = (StaticTextStyleSheet)styleSheet;

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
	
	
	
	protected void onRealise()
	{
		super.onRealise();
		visual.realise( getPresentationArea() );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		StaticTextStyleSheet textStyleSheet = (StaticTextStyleSheet)styleSheet;

		Color prevColour = graphics.getColor();

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
}
