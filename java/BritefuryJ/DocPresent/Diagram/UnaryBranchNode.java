//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public abstract class UnaryBranchNode extends DiagramNode
{
	// Child
	protected DiagramNode child;
	
	
	public UnaryBranchNode(DiagramNode child)
	{
		this.child = child;
	}
	
	
	public void realise(DiagramOwner owner)
	{
		super.realise( owner );
		child.realise( owner );
	}
	
	public void unrealise()
	{
		child.unrealise();
		super.unrealise();
	}

	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		child.draw( graphics, context );
	}

	
	public AABox2 getParentSpaceBoundingBox()
	{
		return child.getParentSpaceBoundingBox();
	}


	
	protected PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos)
	{
		return child;
	}
	
	protected PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos)
	{
		return child;
	}
	

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return child.containsParentSpacePoint( localPos );
	}

	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return child.containsParentSpacePoint( parentPos );
	}
}
