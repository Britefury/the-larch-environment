//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.PresCom;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.View.GSymFragmentView;

public class LocationAsInnerFragement extends Pres
{
	private Location location;
	
	
	public LocationAsInnerFragement(Location location)
	{
		this.location = location;
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymFragmentView fragment = ctx.getFragment();
		return fragment.presentLocationAsElement( location, ctx.getPerspective(), style, ctx.getInheritedState() );
	}
}
