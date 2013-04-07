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
	private static class ItemPropertyKey
	{
		private ItemPropertyKey()
		{
		}

		public static final ItemPropertyKey instance = new ItemPropertyKey();
	}


	private static class NestedListPropertyKey
	{
		private NestedListPropertyKey()
		{
		}

		public static final NestedListPropertyKey instance = new NestedListPropertyKey();
	}


	private static class ListPropertyKey
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
		dragSource = new ObjectDndHandler.DragSource( AbstractEditableListDrag.class,
				new ObjectDndHandler.SourceDataFn()
				{
					public Object createSourceData(LSElement sourceElement, int aspect)
					{
						return createDragData( sourceElement, aspect );
					}
				} );

		dropDest = new ObjectDndHandler.DropDest( AbstractEditableListDrag.class,
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

		nestedListItemDropDest = new ObjectDndHandler.DropDest( AbstractEditableListDrag.class,
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



	public boolean testController(EditableListController controller)
	{
		return controller == this;
	}


	public boolean canReorder(Object item, Object destList, int index)
	{
		return true;
	}

	public boolean canMove(Object item, Object srcList, int index, Object destList)
	{
		return true;
	}

	public boolean canCopy(Object item, Object srcList, int index, Object destList)
	{
		return true;
	}


	public boolean reorder(Object item, Object destList, int index)
	{
		return true;
	}

	public boolean move(Object item, Object srcList, int index, Object destList)
	{
		return true;
	}

	public boolean copy(Object item, Object srcList, int index, Object destList)
	{
		return true;
	}



	public boolean canReorderToEnd(Object item, Object destList)
	{
		return true;
	}

	public boolean canMoveToEnd(Object item, Object srcList, Object destList)
	{
		return true;
	}

	public boolean canCopyToEnd(Object item, Object srcList, Object destList)
	{
		return true;
	}



	public boolean reorderToEnd(Object item, Object destList)
	{
		return true;
	}

	public boolean moveToEnd(Object item, Object srcList, Object destList)
	{
		return true;
	}

	public boolean copyToEnd(Object item, Object srcList, Object destList)
	{
		return true;
	}



	public Pres item(Object item, Pres p)
	{
		if ( item == null )
		{
			throw new RuntimeException( "item cannot be null" );
		}
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withDragSource( dragSource );
		return p;
	}

	public Pres nestedListItem(Object item, Object nestedList, Pres p)
	{
		if ( item == null )
		{
			throw new RuntimeException( "item cannot be null" );
		}
		if ( nestedList == null )
		{
			throw new RuntimeException( "nestedList cannot be null" );
		}
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withProperty( NestedListPropertyKey.instance, nestedList );
		p = p.withDropDest( nestedListItemDropDest );
		p = p.withDragSource( dragSource );
		return p;
	}

	public Pres editableList(Object list, Pres p)
	{
		if ( list == null )
		{
			throw new RuntimeException( "list cannot be null" );
		}
		p = p.withProperty( ListPropertyKey.instance, list );
		p = p.withDropDest( dropDest );
		return p;
	}



	public static Object getList(LSElement element)
	{
		LSElement.PropertyValue prop = element.findPropertyInAncestors( ListPropertyKey.instance );
		return prop != null  ?  prop.getValue()  :  null;
	}

	public static Object getNestedList(LSElement element)
	{
		LSElement.PropertyValue prop = element.findPropertyInAncestors( NestedListPropertyKey.instance );
		return prop != null  ?  prop.getValue()  :  null;
	}

	public static Object getItem(LSElement element)
	{
		LSElement.PropertyValue prop = element.findPropertyInAncestors( ItemPropertyKey.instance );
		return prop != null  ?  prop.getValue()  :  null;
	}



	private Object createDragData(LSElement sourceElement, int aspect)
	{
		Object ls = getList( sourceElement );
		Object it = getItem( sourceElement );
		if ( ls != null  &&  it != null )
		{
			return new EditableListDrag( this, ls, it );
		}
		else
		{
			return null;
		}
	}

	private boolean canDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		Object ls = getList( destElement );

		if ( ls != null  &&  destElement instanceof LSContainerSequence )
		{
			InsertionPoint ins = ((LSContainerSequence)destElement).getInsertionPointClosestToLocalPoint( targetPosition );
			if ( ins != null )
			{
				AbstractEditableListDrag drag = (AbstractEditableListDrag)data;

				if ( testController( drag.getController() ) )
				{
					Object item = drag.getItem();
					Object list = drag.getEditableList();

					if ( action == ObjectDndHandler.MOVE )
					{
						if ( list == ls )
						{
							return canReorder( item, list, ins.getIndex() );
						}
						else
						{
							return canMove( item, list, ins.getIndex(), ls );
						}
					}
					else
					{
						return canCopy( item, list, ins.getIndex(), ls );
					}
				}
			}
		}
		return false;
	}

	private void drawHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
	{
		ObjectDndHandler.dndHighlightPainter.drawShapes( graphics, destElement.getShapes() );

		Object ls = getList( destElement );

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
		Object ls = getList( destElement );

		if ( ls != null  &&  destElement instanceof LSContainerSequence )
		{
			InsertionPoint ins = ((LSContainerSequence)destElement).getInsertionPointClosestToLocalPoint( targetPosition );
			if ( ins != null )
			{
				AbstractEditableListDrag drag = (AbstractEditableListDrag)data;
				if ( testController( drag.getController() ) )
				{
					Object item = drag.getItem();
					Object list = drag.getEditableList();

					if ( action == ObjectDndHandler.MOVE )
					{
						if ( list == ls )
						{
							return reorder( item, list, ins.getIndex() );
						}
						else
						{
							return move( item, list, ins.getIndex(), ls );
						}
					}
					else
					{
						return copy( item, list, ins.getIndex(), ls );
					}
				}
			}
		}

		return false;
	}




	private boolean canDropOntoNestedList(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		Object nestedLs = getNestedList( destElement );

		if ( nestedLs != null )
		{
			AABox2 bounds = destElement.getLocalAABox();
			Vector2 offset = targetPosition.sub( bounds.getLower() );
			double w = bounds.getWidth(), h = bounds.getHeight();
			if ( offset.x >= w * 0.25  &&  offset.x <= w * 0.75  &&  offset.y >= h * 0.25  &&  offset.y <= h * 0.75 )
			{
				AbstractEditableListDrag drag = (AbstractEditableListDrag)data;

				Object item = drag.getItem();
				Object list = drag.getEditableList();

				if ( action == ObjectDndHandler.MOVE )
				{
					if ( list == nestedLs )
					{
						return canReorderToEnd(item, nestedLs);
					}
					else
					{
						return canMoveToEnd(item, list, nestedLs);
					}
				}
				else
				{
					return canCopyToEnd(item, list, nestedLs);
				}
			}
		}
		return false;
	}

	private void drawNestedListHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
	{
		Object nestedLs = getNestedList( destElement );

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
		Object nestedLs = getNestedList( destElement );

		if ( nestedLs != null )
		{
			AABox2 bounds = destElement.getLocalAABox();
			Vector2 offset = targetPosition.sub( bounds.getLower() );
			double w = bounds.getWidth(), h = bounds.getHeight();
			if ( offset.x >= w * 0.25  &&  offset.x <= w * 0.75  &&  offset.y >= h * 0.25  &&  offset.y <= h * 0.75 )
			{
				AbstractEditableListDrag drag = (AbstractEditableListDrag)data;

				Object item = drag.getItem();
				Object list = drag.getEditableList();

				if ( action == ObjectDndHandler.MOVE )
				{
					if ( list == nestedLs )
					{
						return reorderToEnd(item, nestedLs);
					}
					else
					{
						return moveToEnd(item, list, nestedLs);
					}
				}
				else
				{
					return copyToEnd(item, list, nestedLs);
				}
			}
		}
		return false;
	}
}
