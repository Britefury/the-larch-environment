//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueVisitor;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class StreamEditListener extends EditListener
{
	protected abstract HandleEditResult handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment, EditEvent event, Object model, StreamValue value);
	
	
	
	@Override
	protected HandleEditResult handleEditEvent(DPElement element, DPElement sourceElement, EditEvent editEvent)
	{
		StreamValueVisitor visitor = editEvent.getStreamValueVisitor();
		// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
		// set by a SequentialClipboardHandler - so don't clear it.
		// Otherwise, clear all fixed values on a path from @sourceElement to @element
		if ( !( isSelectionEditEvent( editEvent )  &&  SequentialEditor.getEventSourceElement( editEvent ) == element ) )
		{
			editEvent.getStreamValueVisitor().ignoreElementFixedValue( element );
		}
		
		StreamValue value = visitor.getStreamValue( element );
		GSymFragmentView fragment = (GSymFragmentView)element.getFragmentContext();
		Object model = fragment.getModel();
		
		return handleValue( element, sourceElement, fragment, editEvent, model, value );
	}
}
