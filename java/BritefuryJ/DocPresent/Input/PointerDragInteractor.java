//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.util.Stack;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.DragElementInteractor;
import BritefuryJ.Math.Point2;

public class PointerDragInteractor extends AbstractPointerDragInteractor
{
	private PointerInputElement dragElement = null;
	private DragElementInteractor dragInteractor = null;

	
	
	@Override
	public boolean dragBegin(PointerButtonEvent event)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		PointerInterface pointer = event.getPointer();
		pointer.concretePointer().getLastElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			
			if ( element.isPointerInputElementRealised() )
			{
				Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( DragElementInteractor.class );
				if ( interactors != null )
				{
					for (AbstractElementInteractor interactor: interactors )
					{
						DragElementInteractor pressInt = (DragElementInteractor)interactor;
						boolean bHandled = pressInt.dragBegin( element, event );
						if ( bHandled )
						{
							dragElement = element;
							dragInteractor = pressInt;
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( dragElement != null )
		{
			dragInteractor.dragEnd( dragElement, event, dragStartPos, dragButton );
			dragElement = null;
		}
	}

	@Override
	public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( dragElement != null )
		{
			dragInteractor.dragMotion( dragElement, event, dragStartPos, dragButton );
		}
	}
}