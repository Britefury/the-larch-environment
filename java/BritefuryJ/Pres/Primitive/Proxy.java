//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Proxy extends Pres
{
	private Pres child;
	
	
	public Proxy(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement childElement = child.present( ctx, style );
		DPProxy proxy = new DPProxy();
		proxy.setChild( childElement );
		proxy.copyAlignmentFlagsFrom( childElement );
		return proxy;
	}
}
