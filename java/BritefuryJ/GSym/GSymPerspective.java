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
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymPerspective extends GSymAbstractPerspective
{
	private GSymViewFragmentFunction fragmentViewFn;
	private StyleSheet styleSheet;
	private SimpleAttributeTable initialInheritedState;
	private EditHandler editHandler;
	
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn, StyleSheet styleSheet, SimpleAttributeTable initialInheritedState, EditHandler editHandler)
	{
		this.fragmentViewFn = fragmentViewFn;
		this.styleSheet = styleSheet;
		this.initialInheritedState = initialInheritedState;
		this.editHandler = editHandler;
	}
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn)
	{
		this( fragmentViewFn, StyleSheet.instance, SimpleAttributeTable.instance, null );
	}
	
	
	
	@Override
	public Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return fragmentViewFn.createViewFragment( x, fragment, inheritedState );
	}

	@Override
	public StyleSheet getStyleSheet()
	{
		return styleSheet;
	}

	@Override
	public SimpleAttributeTable getInitialInheritedState()
	{
		return initialInheritedState;
	}

	@Override
	public EditHandler getEditHandler()
	{
		return editHandler;
	}
}
