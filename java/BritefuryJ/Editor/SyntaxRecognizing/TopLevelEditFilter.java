//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.Editor.Sequential.SelectionEditTreeEvent;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;
import BritefuryJ.LSpace.TextEditEvent;

public abstract class TopLevelEditFilter extends SREditFilter
{
	protected void handleTopLevelEdit(LSElement element, LSElement sourceElement, EditEvent event)
	{
	}
	
	
	@Override
	protected HandleEditResult handleEdit(LSElement element, LSElement sourceElement, EditEvent event)
	{
		handleTopLevelEdit( element, sourceElement, event );
		if ( event instanceof TextEditEvent  ||  event instanceof SelectionEditTreeEvent  ||  getSequentialController().isEditEvent( event ) )
		{
			return HandleEditResult.HANDLED;
		}
		else
		{
			return HandleEditResult.NOT_HANDLED;
		}
	}
}
