//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPLine;
import BritefuryJ.DocPresent.StyleParams.LineStyleParams;
import BritefuryJ.DocPresent.StyleParams.LineStyleParams.Direction;

public class LayoutNodeLine extends StaticLayoutNode
{
	public LayoutNodeLine(DPLine element)
	{
		super( element );
	}
	
	

	
	protected void updateRequisitionX()
	{
		DPLine line = (DPLine)element;
		LineStyleParams lineStyleParams = (LineStyleParams)line.getStyleParams();

		Direction direction = lineStyleParams.getDirection();
		if ( direction == LineStyleParams.Direction.HORIZONTAL )
		{
			layoutReqBox.setRequisitionX( 0.0, 0.0 );
		}
		else if ( direction == LineStyleParams.Direction.VERTICAL )
		{
			double x = lineStyleParams.getThickness()  +  lineStyleParams.getPadding() * 2.0;
			layoutReqBox.setRequisitionX( x, x );
		}
		else
		{
			throw new RuntimeException( "Invalid direction" );
		}
	}

	protected void updateRequisitionY()
	{
		DPLine line = (DPLine)element;
		LineStyleParams lineStyleParams = (LineStyleParams)line.getStyleParams();

		Direction direction = lineStyleParams.getDirection();
		if ( direction == LineStyleParams.Direction.HORIZONTAL )
		{
			double y = lineStyleParams.getThickness()  +  lineStyleParams.getPadding() * 2.0;
			layoutReqBox.setRequisitionY( y, 0.0 );
		}
		else if ( direction == LineStyleParams.Direction.VERTICAL )
		{
			layoutReqBox.setRequisitionY( 0.0, 0.0 );
		}
		else
		{
			throw new RuntimeException( "Invalid direction" );
		}
	}
}
