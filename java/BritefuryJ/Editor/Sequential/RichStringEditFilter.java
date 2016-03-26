//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.SequentialRichStringVisitor;
import BritefuryJ.Util.RichString.RichString;

public abstract class RichStringEditFilter extends EditFilter
{
	protected abstract HandleEditResult handleRichStringEdit(LSElement element, LSElement sourceElement, FragmentView fragment, EditEvent event, Object model, RichString value);
	
	
	
	@Override
	protected HandleEditResult handleEdit(LSElement element, LSElement sourceElement, EditEvent editEvent)
	{
		SequentialRichStringVisitor visitor = editEvent.getRichStringVisitor();
		
		RichString value = visitor.getRichString( element );
		FragmentView fragment = (FragmentView)element.getFragmentContext();
		Object model = fragment.getModel();
		
		return handleRichStringEdit( element, sourceElement, fragment, editEvent, model, value );
	}
}
