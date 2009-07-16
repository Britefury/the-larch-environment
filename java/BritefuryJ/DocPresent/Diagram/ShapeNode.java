//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.Shape;

public class ShapeNode extends DiagramNode
{
	protected Shape shape;
	
	
	public ShapeNode(Shape shape)
	{
		super();
		this.shape = shape;
	}


	public void draw(Graphics2D graphics, DrawContext context)
	{
		if ( context.isFillEnabled() )
		{
			graphics.fill( shape );
		}
		if ( context.isStrokeEnabled() )
		{
			graphics.draw( shape );
		}
	}
}
