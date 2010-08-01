//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Browser.Location.TokenIterator;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymPerspective extends GSymAbstractPerspective
{
	private GSymViewFragmentFunction fragmentViewFn;
	private StyleSheet2 styleSheet;
	private AttributeTable initialInheritedState;
	private EditHandler editHandler;
	private GSymRelativeLocationResolver locationResolver;
	
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn, StyleSheet2 styleSheet, AttributeTable initialInheritedState, EditHandler editHandler,
			GSymRelativeLocationResolver locationResolver)
	{
		this.fragmentViewFn = fragmentViewFn;
		this.styleSheet = styleSheet;
		this.initialInheritedState = initialInheritedState;
		this.editHandler = editHandler;
		this.locationResolver = locationResolver;
	}
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn, GSymRelativeLocationResolver locationResolver)
	{
		this( fragmentViewFn, StyleSheet2.instance, AttributeTable.instance, null, locationResolver );
	}
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn)
	{
		this( fragmentViewFn, StyleSheet2.instance, AttributeTable.instance, null, null );
	}
	
	
	
	@Override
	public Pres present(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
	{
		return fragmentViewFn.createViewFragment( x, fragment, inheritedState );
	}

	@Override
	public StyleSheet2 getStyleSheet()
	{
		return styleSheet;
	}

	@Override
	public AttributeTable getInitialInheritedState()
	{
		return initialInheritedState;
	}

	@Override
	public EditHandler getEditHandler()
	{
		return editHandler;
	}

	@Override
	public GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, TokenIterator locationIterator)
	{
		if ( locationResolver != null )
		{
			return locationResolver.resolveRelativeLocation( enclosingSubject.withPerspective( this ), locationIterator );
		}
		else
		{
			if ( locationIterator.getSuffix().equals( "" ) )
			{
				return enclosingSubject.withPerspective( this );
			}
			else
			{
				return null;
			}
		}
	}

}
