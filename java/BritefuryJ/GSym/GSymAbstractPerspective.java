//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.PresCom.ApplyPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;


public abstract class GSymAbstractPerspective
{
	public abstract Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState);
	
	public StyleSheet getStyleSheet()
	{
		return StyleSheet.instance;
	}
	
	public abstract SimpleAttributeTable getInitialInheritedState();
	public abstract EditHandler getEditHandler();

	
	public ApplyPerspective applyTo(Object x)
	{
		return new ApplyPerspective( this, x );
	}
	
	public ApplyPerspective __call__(Object x)
	{
		return new ApplyPerspective( this, x );
	}
}
