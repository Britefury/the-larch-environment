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
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Point2;

public class InteractionNode extends UnaryBranchNode
{
	// Hover
	protected HoverMonitor hoverMonitor;
	protected DiagramNode hoverHighlight;
	
	// Interaction
	protected InteractionListener interactionListener;
	
	// Dnd
	protected DndHandler dndHandler;
	
	
	
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
	
	public InteractionNode(DiagramNode child, DndHandler dndHandler)
	{
		super( child );
		this.dndHandler = dndHandler;
	}
	
	private InteractionNode(InteractionNode i)
	{
		super( i.child );
		hoverMonitor = i.hoverMonitor;
		hoverHighlight = i.hoverHighlight;
		interactionListener = i.interactionListener;
		dndHandler = i.dndHandler;
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
	
	public DiagramNode enableDnd(DndHandler dndHandler)
	{
		if ( this.dndHandler == null )
		{
			InteractionNode i = new InteractionNode( this );
			i.dndHandler = dndHandler;
			return i;
		}
		else
		{
			return new InteractionNode( this, dndHandler );
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
		if ( interactionListener != null )
		{
			return interactionListener.onButtonDown( event );
		}
		else
		{
			return false;
		}
	}
	
	protected boolean handlePointerButtonDown2(PointerButtonEvent event)
	{
		if ( interactionListener != null )
		{
			return interactionListener.onButtonDown2( event );
		}
		else
		{
			return false;
		}
	}
	
	protected boolean handlePointerButtonDown3(PointerButtonEvent event)
	{
		if ( interactionListener != null )
		{
			return interactionListener.onButtonDown3( event );
		}
		else
		{
			return false;
		}
	}
	
	protected boolean handlePointerButtonUp(PointerButtonEvent event)
	{
		if ( interactionListener != null )
		{
			return interactionListener.onButtonUp( event );
		}
		else
		{
			return false;
		}
	}
	
	protected void handlePointerMotion(PointerMotionEvent event)
	{
		if ( interactionListener != null )
		{
			interactionListener.onMotion( event );
		}
	}
	
	protected void handlePointerEnter(PointerMotionEvent event)
	{
		if ( hoverHighlight != null )
		{
			queueRedraw();
		}
		if ( hoverMonitor != null )
		{
			hoverMonitor.onEnter();
		}
		if ( interactionListener != null )
		{
			interactionListener.onEnter( event );
		}
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
		if ( interactionListener != null )
		{
			interactionListener.onLeave( event );
		}
		if ( hoverMonitor != null )
		{
			hoverMonitor.onLeave();
		}
		if ( hoverHighlight != null )
		{
			queueRedraw();
		}
	}
	
	protected void handlePointerEnterFromChild(PointerMotionEvent event, PointerInputElement childElement)
	{
		if ( interactionListener != null )
		{
			interactionListener.onEnterFromChild( event, childElement );
		}
	}
	
	protected void handlePointerLeaveIntoChild(PointerMotionEvent event, PointerInputElement childElement)
	{
		if ( interactionListener != null )
		{
			interactionListener.onLeaveIntoChild( event, childElement );
		}
	}
	
	protected boolean handlePointerScroll(PointerScrollEvent event)
	{
		if ( interactionListener != null )
		{
			return interactionListener.onScroll( event );
		}
		else
		{
			return false;
		}
	}



	public PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])				// targetPos is an output parameter
	{
		DiagramNode childNode = null;
		if ( hoverHighlight != null )
		{
			ArrayList<PointerInterface> pointers = owner != null  ?  owner.getPointersWithinDiagramNodeBounds( this )  :  null;
			if ( pointers != null  &&  pointers.size() > 0 )
			{
				childNode = hoverHighlight;
			}
			else
			{
				childNode = child;
			}
		}
		else
		{
			childNode = child;
		}

		PointerInputElement element = childNode.getDndElement( localPos, targetPos );
		if ( element != null )
		{
			return element;
		}
		
		if ( dndHandler != null )
		{
			if ( targetPos != null )
			{
				targetPos[0] = localPos;
			}
			return this;
		}
		else
		{
			return null;
		}
	}
	
	public DndHandler getDndHandler()
	{
		return dndHandler;
	}
}
