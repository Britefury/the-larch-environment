//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.setDebugName( debugName );
		return element;
	}
}
