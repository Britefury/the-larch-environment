//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.StyleSheet.StyleValues;

public class SetDebugName extends Pres
{
	private String debugName;
	private Pres child;
	
	
	public SetDebugName(Pres child, String debugName)
	{
		this.debugName = debugName;
		this.child = child;
	}
	
	
	public SetDebugName setDebugName(String debugName)
	{
		return new SetDebugName( child, debugName );
	}
	

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setDebugName( debugName );
		return element;
	}
}
