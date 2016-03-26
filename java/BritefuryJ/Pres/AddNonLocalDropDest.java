//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import java.awt.datatransfer.DataFlavor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
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
	
	public AddNonLocalDropDest(Pres child, DataFlavor dataFlavor, ObjectDndHandler.DropHighlightFn highlightFn, ObjectDndHandler.DropFn dropFn)
	{
		this( child, new ObjectDndHandler.NonLocalDropDest( dataFlavor, highlightFn, dropFn ) );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addNonLocalDropDest( dest );
		return element;
	}
}
