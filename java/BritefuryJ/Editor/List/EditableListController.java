//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2013.
//##************************
package BritefuryJ.Editor.List;

import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;

import java.awt.*;


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



	public enum MajorDirection
	{
		HORIZONTAL,
		VERTICAL
	}


	private MajorDirection direction;
	private ObjectDndHandler.DragSource dragSource;
	private ObjectDndHandler.DropDest dropDest;



	public EditableListController()
	{
		this( MajorDirection.HORIZONTAL );
	}

	public EditableListController(MajorDirection direction)
	{
		this.direction = direction;
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



	public boolean canInsertBefore(Object item, Object fromList, Object before, Object intoList, int action)
	{
		return true;
	}

	public boolean canInsertAfter(Object item, Object fromList, Object after, Object intoList, int action)
	{
		return true;
	}

	public abstract boolean insertBefore(Object item, Object fromList, Object before, Object intoList, int action);
	public abstract boolean insertAfter(Object item, Object fromList, Object after, Object intoList, int action);




	public Pres item(Object item, Pres p)
	{
		p = p.withProperty( ItemPropertyKey.instance, item );
		p = p.withDragSource( dragSource ).withDropDest( dropDest );
		return p;
	}

	public Pres editableList(Object list, Pres p)
	{
		p = p.withProperty( ListPropertyKey.instance, list );
		return p;
	}



	private boolean shouldDropAfter(LSElement element, Point2 targetPos)
	{
		Shape shapes[] = element.getShapes();
		if ( shapes.length > 0 )
		{
			if ( direction == MajorDirection.HORIZONTAL )
			{
				return targetPos.x > element.getActualWidth() * 0.5;
			}
		}
		else
		{
			return false;
		}
		return false;
	}



	private boolean canDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		return false;
	}

	private void drawHighlight(LSElement destElement, Graphics2D graphics, Point2 targetPosition, int action)
	{

	}

	private boolean drop(LSElement destElement, Point2 targetPosition, Object data, int action)
	{
		return false;
	}


	private Object createDragData(LSElement sourceElement, int aspect)
	{
		return null;
	}
}
