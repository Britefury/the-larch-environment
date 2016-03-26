//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.StyleSheet.StyleValues;

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
	
	public AddDragSource(Pres child, Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		this( child, new ObjectDndHandler.DragSource( dataType, sourceDataFn, exportDoneFn ) );
	}
	
	public AddDragSource(Pres child, Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		this( child, new ObjectDndHandler.DragSource( dataType, sourceDataFn ) );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addDragSource( source );
		return element;
	}
}
