//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSViewport;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.Util.Range;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Viewport extends Pres
{
	private Pres child;
	private Range xRange = null, yRange = null;
	private PersistentState persistentState;
	
	
	public Viewport(Object child, PersistentState persistentState)
	{
		this.child = coerce( child );
		this.persistentState = persistentState;
	}
	
	public Viewport(Object child, Range xRange, Range yRange, PersistentState persistentState)
	{
		this.child = coerce( child );
		this.xRange = xRange;
		this.yRange = yRange;
		this.persistentState = persistentState;
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSViewport element = new LSViewport( Primitive.containerParams.get( style ), xRange, yRange, persistentState );
		StyleValues childStyle = Primitive.useContainerParams.get( style );
		element.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
		return element;
	}
}
