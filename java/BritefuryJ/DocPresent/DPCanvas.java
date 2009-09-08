//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Canvas.DrawingNode;
import BritefuryJ.DocPresent.Canvas.DrawingOwner;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.Point2;

public class DPCanvas extends DPStatic implements DrawingOwner
{
	protected static int FLAG_DIAGRAM_SHRINK_X = FLAGS_ELEMENT_END * 0x1;
	protected static int FLAG_DIAGRAM_SHRINK_Y = FLAGS_ELEMENT_END * 0x2;
	
	protected DrawingNode drawing;
	protected double drawingWidth, drawingHeight;
	
	
	public DPCanvas(DrawingNode drawing)
	{
		this( WidgetStyleSheet.defaultStyleSheet, drawing, -1.0, -1.0, false, false );
	}
	
	public DPCanvas(WidgetStyleSheet styleSheet, DrawingNode drawing)
	{
		this( styleSheet, drawing, -1.0, -1.0, false, false );
	}
	
	public DPCanvas(DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		this( WidgetStyleSheet.defaultStyleSheet, drawing, width, height, bShrinkX, bShrinkY );
	}
	
	public DPCanvas(WidgetStyleSheet styleSheet, DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		super( styleSheet );
		
		this.drawing = drawing;
		this.drawingWidth = width;
		this.drawingHeight = height;
		setFlagValue( FLAG_DIAGRAM_SHRINK_X, bShrinkX );
		setFlagValue( FLAG_DIAGRAM_SHRINK_Y, bShrinkY );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		drawing.draw( graphics );
	}
	
	
	
	
	protected void onRealise()
	{
		super.onRealise();
		
		drawing.realise( this );
	}
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
		drawing.unrealise();

		super.onUnrealise( unrealiseRoot );
	}
	
	

	
	protected void updateRequisitionX()
	{
		double width = drawing.getParentSpaceBoundingBox().getUpperX();
		if ( drawingWidth >= 0.0 )
		{
			if ( testFlag( FLAG_DIAGRAM_SHRINK_X ) )
			{
				width = Math.min( width, drawingWidth );
			}
			else
			{
				width = drawingWidth;
			}
		}
		layoutReqBox.setRequisitionX( width, width );
	}

	protected void updateRequisitionY()
	{
		double height = drawing.getParentSpaceBoundingBox().getUpperY();
		if ( drawingHeight >= 0.0 )
		{
			if ( testFlag( FLAG_DIAGRAM_SHRINK_Y ) )
			{
				height = Math.min( height, drawingHeight );
			}
			else
			{
				height = drawingHeight;
			}
		}
		layoutReqBox.setRequisitionY( height, 0.0 );
	}


	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		return drawing;
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		return drawing;
	}
	

	
	//
	//
	// DRAG AND DROP METHODS
	//
	//
	
	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])
	{
		if ( drawing != null )
		{
			PointerInputElement element = drawing.getDndElement( localPos, targetPos );
			if ( element != null )
			{
				if ( targetPos != null )
				{
					targetPos[0] = localPos;
				}
				return element;
			}
		}
		
		if ( dndHandler != null )
		{
			if ( targetPos != null )
			{
				targetPos[0] = localPos;
			}
			return this;
		}
		else
		{
			return null;
		}
	}

	
	
	//
	//
	// DIAGRAM OWNER METHODS
	//
	//
	
	public void drawingQueueRedraw()
	{
		queueFullRedraw();
	}

	public DPPresentationArea getDrawingPresentationArea()
	{
		return presentationArea;
	}

	public ArrayList<PointerInterface> getPointersWithinDrawingNodeBounds(DrawingNode node)
	{
		return presentationArea.getInputTable().getPointersWithinBoundsOfElement( node );
	}
}