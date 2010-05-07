//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationPanEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationZoomEvent;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class MousePointer extends Pointer
{
	private int navigationButton = 0;
	private Point2 navigationDragStartPos = new Point2();
	private Point2 navigationDragCurrentPos = new Point2();
	private boolean bNavigationDragInProgress = false;
	
	
	public MousePointer(InputTable inputTable, PointerInputElement rootElement, PointerDndController dndController, JComponent component)
	{
		super( inputTable, rootElement, dndController, component );

	}



	private boolean testNavigationModifiers(int modifiers)
	{
		int keys = modifiers & Modifier.KEYS_MASK;
		return keys == Modifier.ALT  ||  keys == Modifier.ALT_GRAPH;
	}
	
	
	
	public boolean buttonDown(Point2 pos, int button)
	{
		if ( testNavigationModifiers( getModifiers() ) )
		{
			if ( !bNavigationDragInProgress )
			{
				PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN );
				navigationButton = button;
				navigationDragStartPos = pos.clone();
				navigationDragCurrentPos = pos.clone();
				bNavigationDragInProgress = true;
				rootEntry.handleNavigationGestureBegin( this, event );
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return super.buttonDown( pos, button );
		}
	}

	public boolean buttonClicked(Point2 pos, int button, int clickCount)
	{
		if ( !bNavigationDragInProgress )
		{
			return super.buttonClicked( pos, button, clickCount );
		}
		else
		{
			return false;
		}
	}

	public boolean buttonUp(Point2 pos, int button)
	{
		if ( bNavigationDragInProgress )
		{
			if ( button == navigationButton ) 
			{
				PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.UP );
				rootEntry.handleNavigationGestureEnd( this, event );
				bNavigationDragInProgress = false;
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return super.buttonUp( pos, button );
		}
	}


	public void drag(Point2 pos, MouseEvent mouseEvent)
	{
		if ( bNavigationDragInProgress )
		{
			Vector2 delta = pos.sub( navigationDragCurrentPos );
			navigationDragCurrentPos = pos.clone();
			
			PointerNavigationEvent event = null;
			
			if ( navigationButton == 1  ||  navigationButton == 2 )
			{
				event = new PointerNavigationPanEvent( this, delta );
			}
			else if ( navigationButton == 3 )
			{
				double scaleDeltaPixels = delta.x + delta.y;
				double scaleDelta = Math.pow( 2.0, scaleDeltaPixels / 200.0 );
				
				event = new PointerNavigationZoomEvent( this, navigationDragStartPos, scaleDelta );
			}
		
			if ( event != null )
			{
				rootEntry.handleNavigationGestureDrag( this, event );
			}
		}
		else
		{
			super.drag( pos, mouseEvent );
		}
	}
	
	
	public boolean scroll(int scrollX, int scrollY)
	{
		if ( testNavigationModifiers( getModifiers() ) )
		{
			double delta = (double)scrollY;
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			rootEntry.handleNavigationGestureClick( this, new PointerNavigationZoomEvent( this, localPos, scaleDelta ) );
			return true;
		}
		else
		{
			return super.scroll( scrollX, scrollY );
		}
	}
	
}
