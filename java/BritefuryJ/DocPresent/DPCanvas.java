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
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeCanvas;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.Math.Point2;

public class DPCanvas extends DPContentLeaf implements DrawingOwner
{
	protected static int FLAG_DIAGRAM_SHRINK_X = FLAGS_CONTENTLEAF_END * 0x1;
	protected static int FLAG_DIAGRAM_SHRINK_Y = FLAGS_CONTENTLEAF_END * 0x2;
	
	protected DrawingNode drawing;
	protected double drawingWidth, drawingHeight;
	
	
	public DPCanvas(String textRepresentation, DrawingNode drawing)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation, drawing, -1.0, -1.0, false, false );
	}
	
	public DPCanvas(ContentLeafStyleParams styleParams, String textRepresentation, DrawingNode drawing)
	{
		this(styleParams, textRepresentation, drawing, -1.0, -1.0, false, false );
	}
	
	public DPCanvas(String textRepresentation, DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation, drawing, width, height, bShrinkX, bShrinkY );
	}
	
	public DPCanvas(ContentLeafStyleParams styleParams, String textRepresentation, DrawingNode drawing, double width, double height, boolean bShrinkX, boolean bShrinkY)
	{
		super( styleParams, textRepresentation );
		
		layoutNode = new LayoutNodeCanvas( this );
		
		this.drawing = drawing;
		this.drawingWidth = width;
		this.drawingHeight = height;
		setFlagValue( FLAG_DIAGRAM_SHRINK_X, bShrinkX );
		setFlagValue( FLAG_DIAGRAM_SHRINK_Y, bShrinkY );
	}
	
	protected DPCanvas(DPCanvas element)
	{
		super( element );
		
		layoutNode = new LayoutNodeCanvas( this );
		
		this.drawing = element.drawing;
		this.drawingWidth = element.drawingWidth;
		this.drawingHeight = element.drawingHeight;
		setFlagValue( FLAG_DIAGRAM_SHRINK_X, element.testFlag( FLAG_DIAGRAM_SHRINK_X ) );
		setFlagValue( FLAG_DIAGRAM_SHRINK_Y, element.testFlag( FLAG_DIAGRAM_SHRINK_Y ) );
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPCanvas clone = new DPCanvas( this );
		clone.clonePostConstuct( this );
		return clone;
	}

	
	
	
	//
	//
	// Drawing
	//
	//
	
	public DrawingNode getDrawing()
	{
		return drawing;
	}
	
	public double getDrawingWidth()
	{
		return drawingWidth;
	}
	
	public double getDrawingHeight()
	{
		return drawingHeight;
	}
	
	public boolean isShinkXEnabled()
	{
		return testFlag( FLAG_DIAGRAM_SHRINK_X );
	}
	
	public boolean isShinkYEnabled()
	{
		return testFlag( FLAG_DIAGRAM_SHRINK_Y );
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
	
	protected void onUnrealise(DPElement unrealiseRoot)
	{
		drawing.unrealise();

		super.onUnrealise( unrealiseRoot );
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
	// DIAGRAM OWNER METHODS
	//
	//
	
	public void drawingQueueRedraw()
	{
		queueFullRedraw();
	}

	public ArrayList<PointerInterface> getPointersWithinDrawingNodeBounds(DrawingNode node)
	{
		return rootElement.getInputTable().getPointersWithinBoundsOfElement( node );
	}
}
