//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.StyleSheet.StyleValues;

public class AddDropDest extends Pres
{
	private ObjectDndHandler.DropDest dest;
	private Pres child;
	
	
	public AddDropDest(Pres child, ObjectDndHandler.DropDest dest)
	{
		this.dest = dest;
		this.child = child;
	}
	
	public AddDropDest(Pres child, Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropHighlightFn highlightFn, ObjectDndHandler.DropFn dropFn)
	{
		this( child, new ObjectDndHandler.DropDest( dataType, canDropFn, highlightFn, dropFn ) );
	}
	
	public AddDropDest(Pres child, Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		this( child, new ObjectDndHandler.DropDest( dataType, dropFn) );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addDropDest( dest );
		return element;
	}
}
