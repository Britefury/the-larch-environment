//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.LSpace.ElementSearchBredthFirst;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TableElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Interactor.TargetElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class AbstractTableEditorInstance <ModelType>
{
	private class TargetInteractor implements TargetElementInteractor
	{
		public SelectionPoint targetDragBegin(PointerInputElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				Point2 p = event.getLocalPointerPos();
				LSElement elem = (LSElement)element;
				TableElement table = (TableElement)element;
				int pos[] = table.getCellPositionUnder( p );
				pos = table.getPositionOfChildCoveringCell( pos[0], pos[1] );
				
				int tx = elementXToTableX( pos[0] );
				int ty = elementYToTableY( pos[1] );
				
				int targetX = Math.max( tx, 0 );
				int targetY = Math.max( ty, 0 );
				
				Target target = elem.getRootElement().getTarget();
				if ( target instanceof TableTarget )
				{
					TableTarget tableTarget = (TableTarget)target;
					if ( targetX == tableTarget.x  &&  targetY == tableTarget.y )
					{
						return null;
					}
				}
				
				target = new TableTarget( AbstractTableEditorInstance.this, table, targetX, targetY );
				elem.getRootElement().setTarget( target );
				
				Marker marker = Marker.atPointIn( elem, p, true );
				if ( marker != null )
				{
					elem.getRootElement().getCaret().moveTo( marker );
				}
				
				return new TableSelectionPoint( AbstractTableEditorInstance.this, table, tx, ty );
			}
			
			return null;
		}

		public void targetDragEnd(PointerInputElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
		}

		public SelectionPoint targetDragMotion(PointerInputElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
		{
			Point2 p = event.getLocalPointerPos();
			TableElement table = (TableElement)element;
			int pos[] = table.getCellPositionUnder( p );
			pos = table.getPositionOfChildCoveringCell( pos[0], pos[1] );
			
			int tx = elementXToTableX( pos[0] );
			int ty = elementYToTableY( pos[1] );
			
			return new TableSelectionPoint( AbstractTableEditorInstance.this, table, tx, ty );
		}
	}
	
	
	
	private class TableEditorPres extends Pres
	{
		private Pres contents;
		
		
		public TableEditorPres(Object contents)
		{
			this.contents = Pres.coerce( contents );
		}

		@Override
		public LSElement present(PresentationContext ctx, StyleValues style)
		{
			TargetInteractor interactor = new TargetInteractor();
			
			LSElement element = contents.present( ctx, style );
			LSElement tableElement = ElementSearchBredthFirst.searchByType( element, TableElement.class );
			tableElement.addElementInteractor( interactor );
			return element;
		}
	}
	
	
	protected AbstractTableEditor<ModelType> editor;
	protected ModelType model;
	protected boolean editable;
	protected LiveFunction tableUnit;
	
	
	protected AbstractTableEditorInstance(AbstractTableEditor<ModelType> editor, ModelType model, boolean editable)
	{
		this.editor = editor;
		this.model = model;
		this.editable = editable;
		
		LiveFunction.Function eval = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				Pres t = presentTable();
				t = new TableEditorPres( t );
				return new Region( t, AbstractTableEditorInstance.this.editor.clipboardHandler );
			}
		};
		
		tableUnit = new LiveFunction( eval );
	}
	
	
	
	protected abstract Pres presentTable();
	
	

	protected Object[][] getSelectedData(TableSelection tableSelection, LSElement tableElement, int x0, int y0, int x1, int y1)
	{
		if ( tableElement.isRealised() )
		{
			return editor.getBlock( model, x0, y0, x1 + 1 - x0, y1 + 1 - y0 );
		}
		else
		{
			return null;
		}
	}
	
	protected Pres editTable()
	{
		return tableUnit;
	}

	
	protected int tableXToElementX(int x)
	{
		return editor.hasLeftHeader  ?  x + 1  :  x;
	}
	
	protected int tableYToElementY(int y)
	{
		return editor.hasTopHeader  ?  y + 1  :  y;
	}
	
	protected int elementXToTableX(int x)
	{
		return editor.hasLeftHeader  ?  x - 1  :  x;
	}
	
	protected int elementYToTableY(int y)
	{
		return editor.hasTopHeader  ?  y - 1  :  y;
	}
	
	
	protected boolean hasLeftHeader()
	{
		return editor.hasLeftHeader;
	}
	
	protected boolean hasTopHeader()
	{
		return editor.hasTopHeader;
	}
	
	
	protected boolean canGrowRight()
	{
		return editor.growRight && editable;
	}
	
	protected boolean canGrowDown()
	{
		return editor.growDown && editable;
	}
	
	
	protected int tableWToElementW(int w)
	{
		int elementW = w;
		if ( editor.hasLeftHeader )
		{
			elementW++;
		}
		if ( editor.growRight  &&  editable )
		{
			elementW++;
		}
		return elementW;
	}
	
	protected int tableHToElementH(int h)
	{
		int elementH = h;
		if ( editor.hasTopHeader )
		{
			elementH++;
		}
		if ( editor.growDown  &&  editable )
		{
			elementH++;
		}
		return elementH;
	}
	
	
	protected int tableWToHeaderW(int w)
	{
		int headerW = w;
		if ( editor.hasLeftHeader )
		{
			headerW++;
		}
		return headerW;
	}
	

	protected abstract int getHeight();
	protected abstract int getRowWidth(int row);
	protected abstract int getMaxRowWidth();
}
