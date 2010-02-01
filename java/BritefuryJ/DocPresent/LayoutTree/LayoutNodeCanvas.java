//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPCanvas;
import BritefuryJ.DocPresent.Canvas.DrawingNode;

public class LayoutNodeCanvas extends StaticLayoutNode
{
	public LayoutNodeCanvas(DPCanvas element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		DPCanvas canvas = (DPCanvas)element;
		DrawingNode drawing = canvas.getDrawing();
		double drawingWidth = canvas.getDrawingWidth();
		
		double width = drawing.getParentSpaceBoundingBox().getUpperX();
		if ( drawingWidth >= 0.0 )
		{
			if ( canvas.isShinkXEnabled() )
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
		DPCanvas canvas = (DPCanvas)element;
		DrawingNode drawing = canvas.getDrawing();
		double drawingHeight = canvas.getDrawingHeight();
		
		double height = drawing.getParentSpaceBoundingBox().getUpperY();
		if ( drawingHeight >= 0.0 )
		{
			if ( canvas.isShinkYEnabled() )
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
}
