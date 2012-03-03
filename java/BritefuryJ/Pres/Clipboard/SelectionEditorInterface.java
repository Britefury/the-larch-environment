//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

abstract public class SelectionEditorInterface
{
	abstract public Class<? extends Selection> getSelectionClass();
	abstract protected boolean replaceSelectionWithText(Selection selection, Target target, String replacement);
	abstract protected boolean deleteSelection(Selection selection, Target target);
}
