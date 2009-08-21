//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;

public class InteractionNode extends UnaryBranchNode
{
	// Hover
	protected HoverMonitor hoverMonitor;
	protected DiagramNode hoverHighlight;
	
	// Interaction
	protected InteractionListener interactionListener;
	
	
	public InteractionNode(DiagramNode child, HoverMonitor hoverMonitor)
	{
		super( child );
		this.hoverMonitor = hoverMonitor;
	}

	public InteractionNode(DiagramNode child, DiagramNode hoverHighlight)
	{
		super( child );
		this.hoverHighlight = hoverHighlight;
	}

	public InteractionNode(DiagramNode child, InteractionListener interactionListener)
	{
		super( child );
		this.interactionListener = interactionListener;
	}
	
	private InteractionNode(InteractionNode i)
	{
		super( i.child );
		hoverMonitor = i.hoverMonitor;
		hoverHighlight = i.hoverHighlight;
		interactionListener = i.interactionListener;
	}

	

	public void realise(DiagramOwner owner)
	{
		super.realise( owner );
		
		if ( hoverHighlight != null )
		{
			hoverHighlight.realise( owner );
		}
	}
	
	public void unrealise()
	{
		if ( hoverHighlight != null )
		{
			hoverHighlight.unrealise();
		}
		
		super.unrealise();
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
		if ( hoverHighlight != null )
		{
			ArrayList<PointerInterface> pointers = owner != null  ?  owner.getPointersWithinDiagramNodeBounds( this )  :  null;
			if ( pointers != null  &&  pointers.size() > 0 )
			{
				hoverHighlight.draw( graphics, context );
			}
			else
			{
				child.draw( graphics, context );
			}
		}
		else
		{
			child.draw( graphics, context );
		}
	}



	protected boolean handlePointerButtonDown(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerButtonDown2(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerButtonDown3(PointerButtonEvent event)
	{
		return false;
	}
	
	protected boolean handlePointerButtonUp(PointerButtonEvent event)
	{
		return false;
	}
	
	protected void handlePointerMotion(PointerMotionEvent event)
	{
	}
	
	protected void handlePointerEnter(PointerMotionEvent event)
	{
		if ( hoverHighlight != null )
		{
			queueRedraw();
		}
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
		if ( hoverHighlight != null )
		{
			queueRedraw();
		}
	}
	
	protected void handlePointerEnterFromChild(PointerMotionEvent event, PointerInputElement childElement)
	{
	}
	
	protected void handlePointerLeaveIntoChild(PointerMotionEvent event, PointerInputElement childElement)
	{
	}
	
	protected boolean handlePointerScroll(PointerScrollEvent event)
	{
		return false;
	}
}
