//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.Editor.Sequential.SelectionEditTreeEvent;

public abstract class TopLevelEditListener extends SREditListener
{
	protected boolean canCatchEditEvent(EditEvent event)
	{
		return false;
	}
	
	
	protected void handleTopLevelEditEvent(DPElement element, DPElement sourceElement, EditEvent event)
	{
	}
	
	
	@Override
	protected HandleEditResult handleEditEvent(DPElement element, DPElement sourceElement, EditEvent event)
	{
		handleTopLevelEditEvent( element, sourceElement, event );
		if ( event instanceof TextEditEvent  ||  event instanceof SelectionEditTreeEvent  ||  canCatchEditEvent( event ) )
		{
			return HandleEditResult.HANDLED;
		}
		else
		{
			return HandleEditResult.NOT_HANDLED;
		}
	}
}
