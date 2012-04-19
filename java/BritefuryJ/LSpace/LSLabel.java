//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextHitInfo;
import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.LayoutTree.LayoutNode;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeLabel;
import BritefuryJ.LSpace.StyleParams.LabelStyleParams;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LSLabel extends LSElement
{
	protected TextVisual visual;
	
	
	public LSLabel(String text)
	{
		this( LabelStyleParams.defaultStyleParams, text );
	}
	
	public LSLabel(LabelStyleParams styleParams, String text)
	{
		super( styleParams );
		
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		visual = TextVisual.getTextVisual( text, styleParams.getFont(), styleParams.getUnderline(), styleParams.getStrikethrough(), styleParams.getMixedSizeCaps() );
		
		layoutNode = new LayoutNodeLabel( this );
	}
	
	

	//
	// Text access / modification
	//
	
	public void setText(String text)
	{
		if ( text == null )
		{
			throw new RuntimeException( "Text cannot be null" );
		}

		onTextModified( text );
	}
	
	public String getText()
	{
		return visual.getText();
	}
	
	
	
	private void onTextModified(String text)
	{
		LabelStyleParams textStyleParams = (LabelStyleParams) styleParams;

		TextVisual v = TextVisual.getTextVisual( text, textStyleParams.getFont(), textStyleParams.getUnderline(), textStyleParams.getStrikethrough(), textStyleParams.getMixedSizeCaps() );
		if ( v != visual )
		{
			visual = v;
			LayoutNodeLabel layout = (LayoutNodeLabel)getLayoutNode();
			layout.setVisual( visual );
			
			queueResize();
		}
	}
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		return visual.hitTest( pos.sub( new Vector2( 0.0, deltaY ) ) );
	}
	
	
	
	public boolean isRedrawRequiredOnHover()
	{
		LabelStyleParams s = (LabelStyleParams)styleParams;
		return super.isRedrawRequiredOnHover()  ||  s.getHoverTextPaint() != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		LabelStyleParams textStyleParams = (LabelStyleParams)styleParams;

		Paint prevPaint = graphics.getPaint();

		AffineTransform prevTransform = null;
		LayoutNode layout = getLayoutNode();
		double deltaY = layout.getAllocationBox().getAllocRefY()  -  layout.getRequisitionBox().getReqRefY();
		if ( deltaY != 0.0 )
		{
			prevTransform = graphics.getTransform();
			graphics.translate( 0.0, deltaY );
		}

		
		Paint squiggleUnderlinePaint = textStyleParams.getSquiggleUnderlinePaint();
		if ( squiggleUnderlinePaint != null )
		{
			graphics.setPaint( squiggleUnderlinePaint );
			visual.drawSquiggleUnderline( graphics );
		}


		Paint textPaint;
		if ( isHoverActive() )
		{
			Paint hoverSymbolPaint = textStyleParams.getHoverTextPaint();
			textPaint = hoverSymbolPaint != null  ?  hoverSymbolPaint  :  textStyleParams.getTextPaint();
		}
		else
		{
			textPaint = textStyleParams.getTextPaint();
		}

		graphics.setPaint( textPaint );
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
	
	
	@Override
	public Object getDefaultValue()
	{
		return null;
	}


	@Override
	public String toString()
	{
		return super.toString()  +  " <" + getText() + ">";
	}
}
