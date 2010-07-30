//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.View.GSymFragmentView;

public class PresentObject extends Pres
{
	private Object value;
	
	
	public PresentObject(Object value)
	{
		this.value = value;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		GSymAbstractPerspective perspective = ctx.getPerspective();
		GSymFragmentView fragment = ctx.getFragment();
		AttributeTable inheritedState = ctx.getInheritedState();
		
		if ( perspective == null )
		{
			throw new RuntimeException( "Cannot present general object without valid perspective" );
		}
		
		if ( fragment == null )
		{
			throw new RuntimeException( "Cannot present general object without valid fragment" );
		}
		
		if ( style == null )
		{
			throw new RuntimeException( "Cannot present general object without valid style" );
		}
		
		if ( inheritedState == null )
		{
			throw new RuntimeException( "Cannot present general object without valid inherited state" );
		}
		
		throw new RuntimeException( "Not implemented" );
		//return perspective.present( value, fragment, style, inheritedState );
	}
}
