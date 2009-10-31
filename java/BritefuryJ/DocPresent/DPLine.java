//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;

import BritefuryJ.DocPresent.StyleSheets.LineStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.LineStyleSheet.Direction;

public class DPLine extends DPStatic
{
	public DPLine(ElementContext context)
	{
		this( context, LineStyleSheet.defaultStyleSheet );
	}
	
	public DPLine(ElementContext context, LineStyleSheet styleSheet)
	{
		super( context, styleSheet );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		LineStyleSheet lineStyleSheet = (LineStyleSheet)styleSheet;
		
		Paint prevPaint = graphics.getPaint();
		
		Direction direction = lineStyleSheet.getDirection();
		double inset = lineStyleSheet.getInset(), thickness = lineStyleSheet.getThickness();
		Paint paint = lineStyleSheet.getLinePaint();
		
		if ( direction == LineStyleSheet.Direction.HORIZONTAL )
		{
			double y = getAllocationY() * 0.5;
			double w = getAllocationX();
			
			if ( w  >  inset * 2.0 )
			{
				graphics.setPaint( paint );
				graphics.setStroke( new BasicStroke( (float)thickness ) );
				graphics.draw( new Line2D.Double( inset, y, w - inset, y ) );
			}
		}
		else if ( direction == LineStyleSheet.Direction.VERTICAL )
		{
			double x = getAllocationX() * 0.5;
			double h = getAllocationY();
			
			if ( h  >  inset * 2.0 )
			{
				graphics.setPaint( paint );
				graphics.setStroke( new BasicStroke( (float)thickness ) );
				graphics.draw( new Line2D.Double( x, inset, x, h - inset ) );
			}
		}
		else
		{
			throw new RuntimeException( "Invalid direction" );
		}

		graphics.setPaint( prevPaint );
	}
	
	

	
	protected void updateRequisitionX()
	{
		LineStyleSheet lineStyleSheet = (LineStyleSheet)styleSheet;

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
		LineStyleSheet lineStyleSheet = (LineStyleSheet)styleSheet;

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
