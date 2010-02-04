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

import BritefuryJ.DocPresent.LayoutTree.LayoutNode;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeStaticText;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;

public class DPStaticText extends DPStatic
{
	protected TextVisual visual;
	protected String text;
	
	
	public DPStaticText(ElementContext context, String text)
	{
		this( context, StaticTextStyleSheet.defaultStyleSheet, text );
	}
	
	public DPStaticText(ElementContext context, StaticTextStyleSheet styleSheet, String text)
	{
		super( context, styleSheet );
		
		this.text = text;
		
		visual = TextVisual.getTextVisual( getPresentationArea(), text, styleSheet.getFont(), styleSheet.getMixedSizeCaps() );

		layoutNode = new LayoutNodeStaticText( this );
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
			LayoutNodeStaticText layout = (LayoutNodeStaticText)getLayoutNode();
			layout.setVisual( visual );
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
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getRefY()  -  layout.getRequisitionBox().getRefY();
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
	
	

		
	
	public TextVisual getVisual()
	{
		return visual;
	}
	
	
	
	public String toString()
	{
		return super.toString()  +  " <" + text + ">";
	}
}
