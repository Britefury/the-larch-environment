//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;


public interface GSymPerspective
{
	GSymViewFragmentFunction getFragmentViewFunction();
	StyleSheet getStyleSheet();
	AttributeTable getInitialState();
	EditHandler getEditHandler();

	GSymSubject resolveLocation(GSymSubject enclosingSubject, Location.TokenIterator locationIterator);
}
