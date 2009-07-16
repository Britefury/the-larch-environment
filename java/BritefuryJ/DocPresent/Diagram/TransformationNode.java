//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class TransformationNode extends DiagramNode
{
	// Transformation
	protected AffineTransform transform, transformInverse;
	
	// Child
	protected DiagramNode child;
	
	
	public TransformationNode(DiagramNode child, AffineTransform transform)
	{
		this.child = child;
		this.transform = transform;
	}

	
	
	// User API
	public DiagramNode transform(AffineTransform t)
	{
		AffineTransform x = (AffineTransform)transform.clone();
		x.concatenate( t );
		return new TransformationNode( child, x );
	}
	

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		AffineTransform t = graphics.getTransform();
		AffineTransform x = (AffineTransform)t.clone();
		x.concatenate( transform );
		graphics.setTransform( x );

		child.draw( graphics, context );
		
		graphics.setTransform( t );
	}
}
