//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.List;

import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.InsertionPoint;
import BritefuryJ.LSpace.LSContainerSequence;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;

import java.awt.*;
import java.awt.geom.Path2D;


public abstract class EditableListController
{
	public static class ItemPropertyKey
	{
		private ItemPropertyKey()
		{
		}

		public static final ItemPropertyKey instance = new ItemPropertyKey();
	}


	public static class ListPropertyKey
	{
		private ListPropertyKey()
		{
		}

		public static final ListPropertyKey instance = new ListPropertyKey();
	}



	private ObjectDndHandler.DragSource dragSource;
	private ObjectDndHandler.DropDest dropDest;



	public EditableListController()
	{
		dragSource = new ObjectDndHandler.DragSource( EditableListDrag.class,
				new ObjectDndHandler.SourceDataFn()
				{
					public Object createSourceData(LSElement sourceElement, int aspect)
					{
						return createDragData( sourceElement, aspect );
					}
				} );

		dropDest = new ObjectDndHandler.DropDest( EditableListDrag.class,
				new ObjectDndHandler.CanDropFn()
				{
					public boolean canDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
					{
						return EditableListController.this.canDrop( destElement, targetPosition, data, action );
					}
				},
				new ObjectDndHandler.DropHighlightFn()
				{
					public void drawDrawHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
					{
						EditableListController.this.drawHighlight( destElement, graphics, targetPosition, action );
					}
				},
				new ObjectDndHandler.DropFn()
				{
					public boolean acceptDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
					{
						return EditableListController.this.drop( destElement, targetPosition, data, action );
					}
				} );
	}



	public boolean canInsert(Object item, Object fromList, int index, Object intoList, int action)
	{
		return true;
	}

	public abstract boolean insert(Object item, Object fromList, int index, Object intoList, int action);




	public Pres item(Object item, Pres p)
	{
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withDragSource( dragSource );
		return p;
	}

	public Pres editableList(Object list, Pres p)
	{
		p = p.withProperty( ListPropertyKey.instance, list );
		p = p.withDropDest( dropDest );
		return p;
	}



	private Object createDragData(LSElement sourceElement, int aspect)
	{
		LSElement.PropertyValue ls = sourceElement.findPropertyInAncestors( ListPropertyKey.instance );
		LSElement.PropertyValue it = sourceElement.findPropertyInAncestors( ItemPropertyKey.instance );
		if ( ls != null  &&  it != null )
		{
			return new EditableListDrag( ls.getValue(), it.getValue() );
		}
		else
		{
			return null;
		}
	}

	private boolean canDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		LSElement.PropertyValue ls = destElement.findPropertyInAncestors( ListPropertyKey.instance );

		if ( ls != null  &&  destElement instanceof LSContainerSequence )
		{
			InsertionPoint ins = ((LSContainerSequence)destElement).getInsertionPointClosestToLocalPoint( targetPosition );
			if ( ins != null )
			{
				EditableListDrag drag = (EditableListDrag)data;
				return canInsert( drag.getItem(), drag.getEditableList(), ins.getIndex(), ls.getValue(), action );
			}
		}
		return false;
	}

	private void drawHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
	{
		ObjectDndHandler.dndHighlightPainter.drawShapes( graphics, destElement.getShapes() );

		LSElement.PropertyValue ls = destElement.findPropertyInAncestors( ListPropertyKey.instance );

		if ( ls != null  &&  destElement instanceof LSContainerSequence )
		{
			InsertionPoint ins = ((LSContainerSequence)destElement).getInsertionPointClosestToLocalPoint( targetPosition );
			if ( ins != null )
			{
				Paint oldPaint = graphics.getPaint();
				graphics.setPaint( new Color( 0.8f, 0.3f, 0.0f ) );

				// Build the insertion shape
				Point2 start = ins.getStartPoint();
				Point2 end = ins.getEndPoint();
				Vector2 u = end.sub( start ).getNormalised();
				Vector2 v = u.rotated90CCW();

				Point2 verts[] = new Point2[] {
						start.add( v.mul( -4.0 ) ),
						start.add( v.mul( 4.0 ) ),
						start.add( v.mul( 1.0 ) ).add( u.mul( 8.0 ) ),
						end.add( v.mul( 1.0 ) ).sub( u.mul( 8.0 ) ),
						end.add( v.mul( 4.0 ) ),
						end.add( v.mul( -4.0 ) ),
						end.add( v.mul( -1.0 ) ).sub( u.mul( 8.0 ) ),
						start.add( v.mul( -1.0 ) ).add( u.mul( 8.0 ) )
				};

				Path2D.Double path = new Path2D.Double();
				Point2 first = verts[0];
				path.moveTo( first.x, first.y );
				for (int i = 1; i < verts.length; i++)
				{
					Point2 vtx = verts[i];
					path.lineTo( vtx.x, vtx.y );
				}

				path.closePath();

				graphics.fill( path );

				graphics.setPaint( oldPaint );
			}
		}
	}

	private boolean drop(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		LSElement.PropertyValue ls = destElement.findPropertyInAncestors( ListPropertyKey.instance );

		if ( ls != null  &&  destElement instanceof LSContainerSequence )
		{
			InsertionPoint ins = ((LSContainerSequence)destElement).getInsertionPointClosestToLocalPoint( targetPosition );
			if ( ins != null )
			{
				EditableListDrag drag = (EditableListDrag)data;
				return insert( drag.getItem(), drag.getEditableList(), ins.getIndex(), ls.getValue(), action );
			}
		}

		return false;
	}
}
