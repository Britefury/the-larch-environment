//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.LSpace.LSElement;

public interface ContextMenuElementInteractor extends AbstractElementInteractor
{
	boolean contextMenu(LSElement element, PopupMenu menu);
}
