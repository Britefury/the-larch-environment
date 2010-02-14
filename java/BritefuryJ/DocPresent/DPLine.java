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

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeLine;
import BritefuryJ.DocPresent.StyleParams.LineStyleParams;
import BritefuryJ.DocPresent.StyleParams.LineStyleParams.Direction;

public class DPLine extends DPStatic
{
	public DPLine()
	{
		this( LineStyleParams.defaultStyleParams);
	}
	
	public DPLine(LineStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeLine( this );
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		LineStyleParams lineStyleParams = (LineStyleParams) styleParams;
		
		Paint prevPaint = graphics.getPaint();
		
		Direction direction = lineStyleParams.getDirection();
		double inset = lineStyleParams.getInset(), thickness = lineStyleParams.getThickness();
		Paint paint = lineStyleParams.getLinePaint();
		
		if ( direction == LineStyleParams.Direction.HORIZONTAL )
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
		else if ( direction == LineStyleParams.Direction.VERTICAL )
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
}
