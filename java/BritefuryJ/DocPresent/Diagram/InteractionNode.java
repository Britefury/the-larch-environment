//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;

public class InteractionNode extends DiagramNode
{
	// Hover
	protected HoverMonitor hoverMonitor;
	protected DiagramNode hoverHighlight;
	
	// Interaction
	protected InteractionListener interactionListener;

	
	// Child
	protected DiagramNode child;
	
	
	public InteractionNode(DiagramNode child, HoverMonitor hoverMonitor)
	{
		this.child = child;
		this.hoverMonitor = hoverMonitor;
	}

	public InteractionNode(DiagramNode child, DiagramNode hoverHighlight)
	{
		this.child = child;
		this.hoverHighlight = hoverHighlight;
	}

	public InteractionNode(DiagramNode child, InteractionListener interactionListener)
	{
		this.child = child;
		this.interactionListener = interactionListener;
	}
	
	
	
	private InteractionNode(InteractionNode i)
	{
		child = i.child;
		hoverMonitor = i.hoverMonitor;
		hoverHighlight = i.hoverHighlight;
		interactionListener = i.interactionListener;
	}

	
	// User API
	public DiagramNode hoverMonitor(HoverMonitor hoverMonitor)
	{
		if ( this.hoverMonitor == null )
		{
			InteractionNode i = new InteractionNode( this );
			i.hoverMonitor = hoverMonitor;
			return i;
		}
		else
		{
			return new InteractionNode( this, hoverMonitor );
		}
	}
	
	public DiagramNode hoverHighlight(DiagramNode hoverHighlight)
	{
		InteractionNode i = new InteractionNode( this );
		i.hoverHighlight = hoverHighlight;
		return i;
	}
	
	public DiagramNode onInteraction(InteractionListener interactionListener)
	{
		if ( this.interactionListener == null )
		{
			InteractionNode i = new InteractionNode( this );
			i.interactionListener = interactionListener;
			return i;
		}
		else
		{
			return new InteractionNode( this, interactionListener );
		}
	}
	
	
	public void draw(Graphics2D graphics, DrawContext context)
	{
		child.draw( graphics, context );
	}
}
