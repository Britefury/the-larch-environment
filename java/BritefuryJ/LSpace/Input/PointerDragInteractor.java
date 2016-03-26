//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Stack;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.Math.Point2;

public class PointerDragInteractor extends AbstractPointerDragInteractor
{
	private LSElement dragElement = null;
	private AffineTransform dragElementRootToLocalXform = null;
	private DragElementInteractor dragInteractor = null;

	
	
	@Override
	public boolean dragBegin(PointerButtonEvent event)
	{
		PointerInterface pointer = event.getPointer();
		Stack<LSElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );
		
		while ( !elements.isEmpty() )
		{
			LSElement element = elements.peek();
			PointerButtonEvent elementSpaceEvent = events.peek();
			
			if ( element.isRealised() )
			{
				List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( DragElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors)
					{
						DragElementInteractor pressInt = (DragElementInteractor)interactor;
						boolean bHandled = false;
						try
						{
							bHandled = pressInt.dragBegin( element, elementSpaceEvent );
						}
						catch (Throwable e)
						{
							element.notifyExceptionDuringElementInteractor( pressInt, "dragBegin", e );
						}
						if ( bHandled )
						{
							dragElement = element;
							dragElementRootToLocalXform = Pointer.rootToLocalTransform( elements );
							dragInteractor = pressInt;
							return true;
						}
					}
				}
			}
			
			elements.pop();
			events.pop();
		}
		
		return false;
	}

	@Override
	public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( dragElement != null )
		{
			try
			{
				dragInteractor.dragEnd( dragElement, (PointerButtonEvent)event.transformed( dragElementRootToLocalXform ), dragStartPos.transform( dragElementRootToLocalXform ), dragButton );
			}
			catch (Throwable e)
			{
				dragElement.notifyExceptionDuringElementInteractor( dragInteractor, "dragEnd", e );
			}
			dragElement = null;
			dragElementRootToLocalXform = null;
			dragInteractor = null;
		}
	}

	@Override
	public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( dragElement != null )
		{
			try
			{
				dragInteractor.dragMotion( dragElement, (PointerMotionEvent)event.transformed( dragElementRootToLocalXform ), dragStartPos.transform( dragElementRootToLocalXform ), dragButton );
			}
			catch (Throwable e)
			{
				dragElement.notifyExceptionDuringElementInteractor( dragInteractor, "dragMotion", e );
			}
		}
	}
}
