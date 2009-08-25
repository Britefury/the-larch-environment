//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class TransformationNode extends UnaryBranchNode
{
	// Transformation
	protected AffineTransform localToParent, parentToLocal;
	protected AABox2 parentSpaceBox;
	
	
	public TransformationNode(DiagramNode child, AffineTransform transform)
	{
		super( child );
		
		localToParent = transform;
		try
		{
			parentToLocal = transform.createInverse();
		}
		catch (NoninvertibleTransformException e)
		{
			parentToLocal = new AffineTransform();
		}

		parentSpaceBox = new AABox2();
	}
	
	
	
	public void realise(DiagramOwner owner)
	{
		super.realise( owner );

		AABox2 childBox = child.getParentSpaceBoundingBox();
		parentSpaceBox = new AABox2();
		
		Point2D.Double p = new Point2D.Double();
		
		p.setLocation( childBox.getLowerX(), childBox.getLowerY() );
		localToParent.transform( p, p );
		parentSpaceBox.addPoint( p.x, p.y );

		p.setLocation( childBox.getUpperX(), childBox.getLowerY() );
		localToParent.transform( p, p );
		parentSpaceBox.addPoint( p.x, p.y );

		p.setLocation( childBox.getLowerX(), childBox.getUpperY() );
		localToParent.transform( p, p );
		parentSpaceBox.addPoint( p.x, p.y );

		p.setLocation( childBox.getUpperX(), childBox.getUpperY() );
		localToParent.transform( p, p );
		parentSpaceBox.addPoint( p.x, p.y );
}
	
	public void unrealise()
	{
		parentSpaceBox = new AABox2();
		super.unrealise();
	}

	
	// User API
	public DiagramNode transform(AffineTransform t)
	{
		AffineTransform x = (AffineTransform)t.clone();
		x.concatenate( localToParent );
		return new TransformationNode( child, x );
	}
	

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		AffineTransform t = graphics.getTransform();
		AffineTransform x = (AffineTransform)t.clone();
		x.concatenate( localToParent );
		graphics.setTransform( x );

		child.draw( graphics, context );
		
		graphics.setTransform( t );
	}



	public AABox2 getParentSpaceBoundingBox()
	{
		return parentSpaceBox;
	}




	protected PointerEvent transformParentToLocalEvent(PointerEvent event)
	{
		return event.transformed( parentToLocal );
	}
	
	protected PointerInterface transformParentToLocalPointer(PointerInterface pointer)
	{
		return pointer.transformed( parentToLocal );
	}
	
	public Point2 transformParentToLocalPoint(Point2 parentPos)
	{
		Point2D.Double localPos = new Point2D.Double( parentPos.x, parentPos.y );
		parentToLocal.transform( localPos, localPos );
		return new Point2( localPos.x, localPos.y );
	}




	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		Point2D.Double localPos = new Point2D.Double( parentPos.x, parentPos.y );
		parentToLocal.transform( localPos, localPos );
		return child.containsParentSpacePoint( new Point2( localPos.x, localPos.y ) );
	}
	
	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return child.containsParentSpacePoint( localPos );
	}



	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])				// targetPos is an output parameter
	{
		Point2D.Double childPos = new Point2D.Double( localPos.x, localPos.y );
		parentToLocal.transform( childPos, childPos );
		PointerInputElement element = child.getDndElement( new Point2( childPos.x, childPos.y ), targetPos );
		if ( element != null )
		{
			if ( targetPos != null )
			{
				Point2D.Double targetParent = new Point2D.Double( targetPos[0].x, targetPos[0].y );
				localToParent.transform( targetParent, targetParent );
				targetPos[0] = new Point2( targetParent.x, targetParent.y );
			}
			return element;
		}
		
		return null;
	}
}
