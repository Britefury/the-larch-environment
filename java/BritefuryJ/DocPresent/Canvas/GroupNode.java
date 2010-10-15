//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Canvas;

import java.awt.Graphics2D;
import java.util.List;

import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class GroupNode extends DrawingNode
{
	protected DrawingNode children[];
	protected AABox2 parentSpaceBox;
	
	
	public GroupNode(DrawingNode children[])
	{
		super();
		this.children = children;
		parentSpaceBox = new AABox2();
	}

	public GroupNode(List<DrawingNode> children)
	{
		super();
		this.children = children.toArray( new DrawingNode[0] );
		parentSpaceBox = new AABox2();
	}




	public void realise(DrawingOwner owner)
	{
		super.realise( owner );
		
		for (DrawingNode child: children)
		{
			child.realise( owner );
		}

		parentSpaceBox = new AABox2();
		for (DrawingNode node: children)
		{
			parentSpaceBox.addBox( node.getParentSpaceBoundingBox() );
		}
	}
	
	public void unrealise()
	{
		parentSpaceBox = new AABox2();

		for (DrawingNode child: children)
		{
			child.unrealise();
		}

		super.unrealise();
	}

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		for (DrawingNode child: children)
		{
			child.draw( graphics, context );
		}
	}


	public AABox2 getParentSpaceBoundingBox()
	{
		return parentSpaceBox;
	}




	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		for (DrawingNode child: children)
		{
			if ( child.getParentSpaceBoundingBox().containsPoint( localPos ) )
			{
				if ( child.containsParentSpacePoint( localPos ) )
				{
					return child;
				}
			}
		}
		return null;
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		for (int i = children.length - 1; i >= 0; i--)
		{
			DrawingNode child = children[i];
			if ( child.getParentSpaceBoundingBox().containsPoint( localPos ) )
			{
				if ( child.containsParentSpacePoint( localPos ) )
				{
					return child;
				}
			}
		}
		return null;
	}
	

	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return containsLocalSpacePoint( parentPos );
	}
	
	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		for (DrawingNode child: children)
		{
			if ( child.getParentSpaceBoundingBox().containsPoint( localPos ) )
			{
				if ( child.containsParentSpacePoint( localPos ) )
				{
					return true;
				}
			}
		}
		return false;
	}
}
