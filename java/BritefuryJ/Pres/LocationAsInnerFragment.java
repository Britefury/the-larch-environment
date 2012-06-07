//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.Browser.Location;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class LocationAsInnerFragment extends Pres
{
	private Location location;
	
	
	public LocationAsInnerFragment(Location location)
	{
		this.location = location;
	}
	
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		FragmentView fragment = ctx.getFragment();
		return fragment.presentLocationAsElement( location, style, ctx.getInheritedState() );
	}
}
