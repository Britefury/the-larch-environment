//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
		if ( event instanceof TextEditEvent  ||  event instanceof SelectionEditTreeEvent  ||  getSequentialEditor().isEditEvent( event ) )
		{
			return HandleEditResult.HANDLED;
		}
		else
		{
			return HandleEditResult.NOT_HANDLED;
		}
	}
}
