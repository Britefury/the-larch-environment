//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class AddDragSource extends Pres
{
	private ObjectDndHandler.DragSource source;
	private Pres child;
	
	
	public AddDragSource(Pres child, ObjectDndHandler.DragSource source)
	{
		this.source = source;
		this.child = child;
	}
	
	public AddDragSource(Pres child, Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		this( child, new ObjectDndHandler.DragSource( dataType, sourceAspects, sourceDataFn, exportDoneFn ) );
	}
	
	public AddDragSource(Pres child, Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		this( child, new ObjectDndHandler.DragSource( dataType, sourceAspects, sourceDataFn ) );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.addDragSource( source );
		return element;
	}
}
