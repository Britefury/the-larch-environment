//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.List;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.InsertionPoint;
import BritefuryJ.LSpace.LSContainerSequence;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;

import java.awt.*;
import java.awt.geom.Rectangle2D;


public class EditableListController
{
	public static class ItemPropertyKey
	{
		private ItemPropertyKey()
		{
		}

		public static final ItemPropertyKey instance = new ItemPropertyKey();
	}


	public static class NestedListItemPropertyKey
	{
		private NestedListItemPropertyKey()
		{
		}

		public static final NestedListItemPropertyKey instance = new NestedListItemPropertyKey();
	}


	public static class ListPropertyKey
	{
		private ListPropertyKey()
		{
		}

		public static final ListPropertyKey instance = new ListPropertyKey();
	}



	public static final FilledOutlinePainter nestedListItemDndHighlightPainter = new FilledOutlinePainter( new Color( 1.0f, 0.2f, 0.0f, 0.2f ), new Color( 1.0f, 0.4f, 0.0f, 0.5f ) );

	private ObjectDndHandler.DragSource dragSource;
	private ObjectDndHandler.DropDest dropDest, nestedListItemDropDest;



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

		nestedListItemDropDest = new ObjectDndHandler.DropDest( EditableListDrag.class,
				new ObjectDndHandler.CanDropFn()
				{
					public boolean canDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
					{
						return EditableListController.this.canDropOntoNestedList( destElement, targetPosition, data, action );
					}
				},
				new ObjectDndHandler.DropHighlightFn()
				{
					public void drawDrawHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
					{
						EditableListController.this.drawNestedListHighlight( destElement, graphics, targetPosition, action );
					}
				},
				new ObjectDndHandler.DropFn()
				{
					public boolean acceptDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
					{
						return EditableListController.this.dropOntoNestedList( destElement, targetPosition, data, action );
					}
				} );
	}



	public boolean canInsert(Object item, Object fromList, int index, Object intoList, int action)
	{
		return true;
	}

	public boolean insert(Object item, Object fromList, int index, Object intoList, int action)
	{
		return false;
	}


	public boolean canAddtoNestedList(Object item, Object fromList, Object intoList, int action)
	{
		return true;
	}

	public boolean addToNestedList(Object item, Object fromList, Object intoList, int action)
	{
		return false;
	}



	public Pres item(Object item, Pres p)
	{
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withDragSource( dragSource );
		return p;
	}

	public Pres nestedListItem(Object item, Object subList, Pres p)
	{
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withProperty( NestedListItemPropertyKey.instance, subList );
		p = p.withDropDest( nestedListItemDropDest );
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
			return new EditableListDrag( this, ls.getValue(), it.getValue() );
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

				ins.draw( graphics );

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




	private boolean canDropOntoNestedList(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		LSElement.PropertyValue nestedLs = destElement.findPropertyInAncestors( NestedListItemPropertyKey.instance );

		if ( nestedLs != null )
		{
			AABox2 bounds = destElement.getLocalAABox();
			Vector2 offset = targetPosition.sub( bounds.getLower() );
			double w = bounds.getWidth(), h = bounds.getHeight();
			if ( offset.x >= w * 0.25  &&  offset.x <= w * 0.75  &&  offset.y >= h * 0.25  &&  offset.y <= h * 0.75 )
			{
				EditableListDrag drag = (EditableListDrag)data;
				return canAddtoNestedList( drag.getItem(), drag.getEditableList(), nestedLs.getValue(), action );
			}
		}
		return false;
	}

	private void drawNestedListHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
	{
		LSElement.PropertyValue nestedLs = destElement.findPropertyInAncestors( NestedListItemPropertyKey.instance );

		if ( nestedLs != null )
		{
			AABox2 bounds = destElement.getLocalAABox();
			Vector2 offset = targetPosition.sub( bounds.getLower() );
			double w = bounds.getWidth(), h = bounds.getHeight();
			if ( offset.x >= w * 0.25  &&  offset.x <= w * 0.75  &&  offset.y >= h * 0.25  &&  offset.y <= h * 0.75 )
			{
				Rectangle2D.Double r = new Rectangle2D.Double( bounds.getLowerX() + w * 0.25, bounds.getLowerY() + h * 0.25, w * 0.5, h * 0.5 );
				nestedListItemDndHighlightPainter.drawShape( graphics, r );
			}
		}
	}

	private boolean dropOntoNestedList(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		LSElement.PropertyValue nestedLs = destElement.findPropertyInAncestors( NestedListItemPropertyKey.instance );

		if ( nestedLs != null )
		{
			AABox2 bounds = destElement.getLocalAABox();
			Vector2 offset = targetPosition.sub( bounds.getLower() );
			double w = bounds.getWidth(), h = bounds.getHeight();
			if ( offset.x >= w * 0.25  &&  offset.x <= w * 0.75  &&  offset.y >= h * 0.25  &&  offset.y <= h * 0.75 )
			{
				EditableListDrag drag = (EditableListDrag)data;
				return addToNestedList( drag.getItem(), drag.getEditableList(), nestedLs.getValue(), action );
			}
		}
		return false;
	}
}
