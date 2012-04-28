//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
