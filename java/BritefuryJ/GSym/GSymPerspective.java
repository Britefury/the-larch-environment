//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location.TokenIterator;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymPerspective extends GSymAbstractPerspective
{
	private GSymViewFragmentFunction fragmentViewFn;
	private StyleSheet styleSheet;
	private AttributeTable initialInheritedState;
	private EditHandler editHandler;
	private GSymRelativeLocationResolver locationResolver;
	
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn, StyleSheet styleSheet, AttributeTable initialInheritedState, EditHandler editHandler,
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
		this( fragmentViewFn, GenericPerspectiveStyleSheet.instance, AttributeTable.instance, null, locationResolver );
	}
	
	public GSymPerspective(GSymViewFragmentFunction fragmentViewFn)
	{
		this( fragmentViewFn, GenericPerspectiveStyleSheet.instance, AttributeTable.instance, null, null );
	}
	
	
	
	@Override
	public DPElement present(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		return fragmentViewFn.createViewFragment( x, ctx, styleSheet, inheritedState );
	}

	@Override
	public StyleSheet getStyleSheet()
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
