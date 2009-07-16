//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;

public class GroupNode extends DiagramNode
{
	protected DiagramNode children[];
	
	
	public GroupNode(DiagramNode children[])
	{
		super();
		this.children = children;
	}




	public void draw(Graphics2D graphics, DrawContext context)
	{
		for (DiagramNode child: children)
		{
			child.draw( graphics, context );
		}
	}
}
