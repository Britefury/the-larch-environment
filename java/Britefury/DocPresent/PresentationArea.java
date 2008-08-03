package Britefury.DocPresent;

import java.util.HashMap;

import Britefury.DocPresent.Event.PointerButtonEvent;
import Britefury.DocPresent.Event.PointerMotionEvent;
import Britefury.DocPresent.Input.PointerInterface;





public class PresentationArea extends Bin {
	private HashMap<PointerInterface, DndDrag> dndTable;
	
	
	
	private void _dndButtonDown(PointerButtonEvent event)
	{
		DndDrag drag = handleDndButtonDown( event );
		
		if ( drag != null )
		{
			dndTable.put( event.pointer, drag );
		}
	}
	
	private void _dndMotion(PointerMotionEvent event)
	{
		DndDrag drag = dndTable.get( event.pointer.concretePointer() );

		if ( drag != null )
		{
			if ( !drag.bInProgress )
			{
				drag.srcWidget.handleDndBegin( event, drag );
				drag.bInProgress = true;
				setCursorDrag( event.pointer );
			}
			
			handleDndMotion( event, drag );			
		}
	}

	private void _dndButtonUp(PointerButtonEvent event)
	{
		DndDrag drag = dndTable.get( event.pointer.concretePointer() );
		
		if ( drag != null )
		{
			handleDndButtonUp( event, drag );
			drag.bInProgress = false;
			dndTable.remove( event.pointer );
			setCursorArrow( event.pointer );
		}
	}
	
}
