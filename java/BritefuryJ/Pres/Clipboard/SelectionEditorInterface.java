//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
