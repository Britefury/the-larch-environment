//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPLine;
import BritefuryJ.DocPresent.StyleSheets.LineStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.LineStyleSheet.Direction;

public class LayoutNodeLine extends StaticLayoutNode
{
	public LayoutNodeLine(DPLine element)
	{
		super( element );
	}
	
	

	
	protected void updateRequisitionX()
	{
		DPLine line = (DPLine)element;
		LineStyleSheet lineStyleSheet = (LineStyleSheet)line.getStyleSheet();

		Direction direction = lineStyleSheet.getDirection();
		if ( direction == LineStyleSheet.Direction.HORIZONTAL )
		{
			layoutReqBox.setRequisitionX( 0.0, 0.0 );
		}
		else if ( direction == LineStyleSheet.Direction.VERTICAL )
		{
			double x = lineStyleSheet.getThickness()  +  lineStyleSheet.getPadding() * 2.0;
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
		LineStyleSheet lineStyleSheet = (LineStyleSheet)line.getStyleSheet();

		Direction direction = lineStyleSheet.getDirection();
		if ( direction == LineStyleSheet.Direction.HORIZONTAL )
		{
			double y = lineStyleSheet.getThickness()  +  lineStyleSheet.getPadding() * 2.0;
			layoutReqBox.setRequisitionY( y, 0.0 );
		}
		else if ( direction == LineStyleSheet.Direction.VERTICAL )
		{
			layoutReqBox.setRequisitionY( 0.0, 0.0 );
		}
		else
		{
			throw new RuntimeException( "Invalid direction" );
		}
	}
}
