//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class TextNode extends DiagramNode
{
	protected static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );
	
	protected String text;
	protected Font font;
	protected boolean bMixedSizeCaps;
	protected HAlignment hAlignment;

	protected AABox2 parentSpaceBox;
	
	protected TextVisual visual;
	
	
	public TextNode(String text)
	{
		this( text, defaultFont, false, HAlignment.CENTRE );
	}

	public TextNode(String text, Font font)
	{
		this( text, font, false, HAlignment.CENTRE );
	}

	public TextNode(String text, HAlignment hAlignment)
	{
		this( text, defaultFont, false, hAlignment );
	}

	public TextNode(String text, Font font, boolean bMixedSizeCaps, HAlignment hAlignment)
	{
		super();
		this.text = text;
		this.font = font;
		this.bMixedSizeCaps = bMixedSizeCaps;
		this.hAlignment = hAlignment;
		parentSpaceBox = new AABox2();
	}


	public void realise(DiagramOwner owner)
	{
		super.realise( owner );

		DPPresentationArea area = owner.getDiagramPresentationArea();
		visual = TextVisual.getTextVisual( area, text, font, bMixedSizeCaps );
		visual.realise( area );
		LReqBox req = visual.getRequisition( 0 );
		parentSpaceBox = new AABox2( 0.0, 0.0, req.getPrefWidth(), req.getReqHeight() );
	}
	
	public void unrealise()
	{
		visual = null;
		parentSpaceBox = new AABox2();
		super.unrealise();
	}

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		Paint strokePaint = context.getStrokePaint();
		if ( strokePaint != null  &&  visual != null )
		{
			graphics.setPaint( strokePaint );
			if ( hAlignment == HAlignment.CENTRE )
			{
				graphics.translate( -parentSpaceBox.getWidth() * 0.5, 0.0 );
			}
			else if ( hAlignment == HAlignment.RIGHT )
			{
				graphics.translate( -parentSpaceBox.getWidth(), 0.0 );
			}
				
			visual.drawText( graphics );
		}
	}


	public AABox2 getParentSpaceBoundingBox()
	{
		return parentSpaceBox;
	}

	
	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return parentSpaceBox.containsPoint( localPos );
	}


	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return parentSpaceBox.containsPoint( parentPos );
	}
}
