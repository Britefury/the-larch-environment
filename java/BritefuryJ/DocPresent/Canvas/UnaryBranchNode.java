//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Canvas;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public abstract class UnaryBranchNode extends DrawingNode
{
	// Child
	protected DrawingNode child;
	
	
	public UnaryBranchNode(DrawingNode child)
	{
		this.child = child;
	}
	
	
	public void realise(DrawingOwner owner)
	{
		super.realise( owner );
		child.realise( owner );
	}
	
	public void unrealise()
	{
		child.unrealise();
		super.unrealise();
	}
	
	
	protected DrawingNode getVisibleChild()
	{
		return child;
	}

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		getVisibleChild().draw( graphics, context );
	}

	
	public AABox2 getParentSpaceBoundingBox()
	{
		return getVisibleChild().getParentSpaceBoundingBox();
	}


	
	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		return getVisibleChild();
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		return getVisibleChild();
	}
	

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return getVisibleChild().containsParentSpacePoint( localPos );
	}

	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return getVisibleChild().containsParentSpacePoint( parentPos );
	}
}
