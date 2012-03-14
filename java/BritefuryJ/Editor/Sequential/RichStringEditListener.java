//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.Util.RichString.RichString;

public abstract class RichStringEditListener extends EditListener
{
	protected abstract HandleEditResult handleValue(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value);
	
	
	
	@Override
	protected HandleEditResult handleEditEvent(LSElement element, LSElement sourceElement, EditEvent editEvent)
	{
		SequentialRichStringVisitor visitor = editEvent.getRichStringVisitor();
		
		RichString value = visitor.getRichString( element );
		FragmentView fragment = (FragmentView)element.getFragmentContext();
		Object model = fragment.getModel();
		
		return handleValue( element, sourceElement, fragment, editEvent, model, value );
	}
}
