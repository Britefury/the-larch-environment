//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public abstract class GSymLightweightPerspective extends GSymPerspective implements GSymViewFragmentFunction
{
	public abstract DPElement present(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable inheritedState);
	
	
	@Override
	public EditHandler getEditHandler()
	{
		return null;
	}

	@Override
	public GSymViewFragmentFunction getFragmentViewFunction()
	{
		return this;
	}

	@Override
	public AttributeTable getInitialInheritedState()
	{
		return AttributeTable.instance;
	}

	@Override
	public StyleSheet getStyleSheet()
	{
		return GenericPerspectiveStyleSheet.instance;
	}

	@Override
	public GSymSubject resolveLocation(GSymSubject enclosingSubject, Location.TokenIterator locationIterator)
	{
		return null;
	}

	@Override
	public DPElement createViewFragment(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		return present( x, ctx, styleSheet, inheritedState );
	}
}
