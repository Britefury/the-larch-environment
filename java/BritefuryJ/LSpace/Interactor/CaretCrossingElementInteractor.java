//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TextFocus.Caret;

public interface CaretCrossingElementInteractor extends AbstractElementInteractor
{
	public void caretEnter(LSElement element, Caret c);
	public void caretLeave(LSElement element, Caret c);
}
