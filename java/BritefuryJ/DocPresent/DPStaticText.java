//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;

import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheetField;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldCascading;
import BritefuryJ.DocPresent.StyleSheets.StyleSheetValueFieldSet;
import BritefuryJ.DocPresent.Util.TextVisual;

public class DPStaticText extends DPStatic
{
	protected static ElementStyleSheetField fontField = ElementStyleSheetField.newField( "font", Font.class );
	protected static ElementStyleSheetField paintField = ElementStyleSheetField.newField( "paint", Paint.class );
	protected static ElementStyleSheetField bMixedSizeCapsField = ElementStyleSheetField.newField( "bMixedSizeCaps", Boolean.class );
	
	protected static StyleSheetValueFieldCascading fontValueField = StyleSheetValueFieldCascading.newField( "font", Font.class, new Font( "Sans serif", Font.PLAIN, 14 ), fontField );
	protected static StyleSheetValueFieldCascading paintValueField = StyleSheetValueFieldCascading.newField( "paint", Paint.class, Color.black, paintField );
	protected static StyleSheetValueFieldCascading bMixedSizeCapsValueField = StyleSheetValueFieldCascading.newField( "bMixedSizeCaps", Boolean.class, false, bMixedSizeCapsField );
	
	
	protected static StyleSheetValueFieldSet useStyleSheetFields_StaticText = useStyleSheetFields_Element.join( fontValueField, paintValueField, bMixedSizeCapsValueField );

	
	protected TextVisual visual;
	protected String text;
	
	
	public DPStaticText(String text)
	{
		this( null, text );
	}
	
	public DPStaticText(ElementStyleSheet styleSheet, String text)
	{
		super( styleSheet );
		
		this.text = text;
		
		visual = null;
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
		Paint prevColour = graphics.getPaint();

		graphics.setPaint( getTextPaint() );
		visual.drawText( graphics );
		
		graphics.setPaint( prevColour );
	}
	
	

	
	protected void updateRequisitionX()
	{
		layoutReqBox = visual != null  ?  visual.getRequisition()  :  new LReqBox();
	}

	protected void updateRequisitionY()
	{
		layoutReqBox = visual != null  ?  visual.getRequisition()  :  new LReqBox();
	}
	
	
	
	
	protected StyleSheetValueFieldSet getUsedStyleSheetValueFields()
	{
		return useStyleSheetFields_StaticText;
	}

	
	public static ElementStyleSheet styleSheet(Font font, Paint paint)
	{
		return new ElementStyleSheet( new String[] { "font", "paint" }, new Object[] { font, paint } );
	}

	public static ElementStyleSheet styleSheet(Font font, Paint paint, boolean bMixedSizeCaps)
	{
		return new ElementStyleSheet( new String[] { "font", "paint", "bMixedSizeCaps" }, new Object[] { font, paint, bMixedSizeCaps } );
	}
}
