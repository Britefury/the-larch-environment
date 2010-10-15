//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Canvas;

import java.util.ArrayList;

import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.DndHandler;
import BritefuryJ.DocPresent.Input.PointerInterface;

public class InteractionNode extends UnaryBranchNode
{
	// Hover
	protected HoverMonitor hoverMonitor;
	protected DrawingNode hoverHighlight;
		
	// Dnd
	protected DndHandler dndHandler;
	
	
	
	public InteractionNode(DrawingNode child, HoverMonitor hoverMonitor)
	{
		super( child );
		this.hoverMonitor = hoverMonitor;
	}

	public InteractionNode(DrawingNode child, DrawingNode hoverHighlight)
	{
		super( child );
		this.hoverHighlight = hoverHighlight;
	}

	public InteractionNode(DrawingNode child, DndHandler dndHandler)
	{
		super( child );
		this.dndHandler = dndHandler;
	}
	
	private InteractionNode(InteractionNode i)
	{
		super( i.child );
		hoverMonitor = i.hoverMonitor;
		hoverHighlight = i.hoverHighlight;
		dndHandler = i.dndHandler;
	}

	

	public void realise(DrawingOwner owner)
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
	public DrawingNode hoverMonitor(HoverMonitor hoverMonitor)
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
	
	public DrawingNode hoverHighlight(DrawingNode hoverHighlight)
	{
		InteractionNode i = new InteractionNode( this );
		i.hoverHighlight = hoverHighlight;
		return i;
	}
	
	public DrawingNode enableDnd(DndHandler dndHandler)
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
	
	
	protected DrawingNode getVisibleChild()
	{
		if ( hoverHighlight != null )
		{
			ArrayList<PointerInterface> pointers = owner != null  ?  owner.getPointersWithinDrawingNodeBounds( this )  :  null;
			if ( pointers != null  &&  pointers.size() > 0 )
			{
				return hoverHighlight;
			}
			else
			{
				return child;
			}
		}
		else
		{
			return child;
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
	}
	
	protected void handlePointerLeave(PointerMotionEvent event)
	{
		if ( hoverMonitor != null )
		{
			hoverMonitor.onLeave();
		}
		if ( hoverHighlight != null )
		{
			queueRedraw();
		}
	}
	


	public DndHandler getDndHandler()
	{
		return dndHandler;
	}
}
