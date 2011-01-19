//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import java.awt.datatransfer.DataFlavor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.StyleSheet.StyleValues;

public class AddNonLocalDropDest extends Pres
{
	private ObjectDndHandler.NonLocalDropDest dest;
	private Pres child;
	
	
	public AddNonLocalDropDest(Pres child, ObjectDndHandler.NonLocalDropDest dest)
	{
		this.dest = dest;
		this.child = child;
	}
	
	public AddNonLocalDropDest(Pres child, DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		this( child, new ObjectDndHandler.NonLocalDropDest( dataFlavor, dropFn ) );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.addNonLocalDropDest( dest );
		return element;
	}
}
