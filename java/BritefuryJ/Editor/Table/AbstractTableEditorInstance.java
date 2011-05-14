//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TableElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.TargetElementInteractor;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.SelectionPoint;
import BritefuryJ.DocPresent.Target.Target;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class AbstractTableEditorInstance <ModelType>
{
	private class TargetInteractor implements TargetElementInteractor
	{
		@Override
		public SelectionPoint targetDragBegin(PointerInputElement element, PointerButtonEvent event)
		{
			if ( event.getButton() == 1 )
			{
				Point2 p = event.getLocalPointerPos();
				DPElement elem = (DPElement)element;
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
				
				Marker marker = elem.getEditableMarkerClosestToLocalPoint( p );
				if ( marker != null )
				{
					elem.getRootElement().getCaret().moveTo( marker );
				}
				
				return new TableSelectionPoint( AbstractTableEditorInstance.this, table, tx, ty );
			}
			
			return null;
		}

		@Override
		public void targetDragEnd(PointerInputElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
		}

		@Override
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
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			TargetInteractor interactor = new TargetInteractor();
			
			DPElement element = contents.present( ctx, style );
			DPElement tableElement = element.bredthFirstSearchByType( TableElement.class );
			tableElement.addElementInteractor( interactor );
			return element;
		}
	}
	
	
	protected AbstractTableEditor<ModelType> editor;
	protected ModelType model;
	
	
	protected AbstractTableEditorInstance(AbstractTableEditor<ModelType> editor, ModelType model)
	{
		this.editor = editor;
		this.model = model;
	}
	
	
	
	protected abstract Pres presentTable();
	
	

	protected Object[][] getSelectedData(TableSelection tableSelection, DPElement tableElement, int x0, int y0, int x1, int y1)
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
		Pres table = presentTable();
		Pres region = new Region( table, editor.clipboardHandler );
		Pres t = new TableEditorPres( region );
		return t;
	}

	
	protected boolean hasHeaderRow()
	{
		return editor.hasHeaderRow();
	}
	
	protected boolean hasHeaderColumn()
	{
		return editor.hasHeaderColumn();
	}
	
	protected int tableXToElementX(int x)
	{
		return hasHeaderColumn()  ?  x + 1  :  x;
	}
	
	protected int tableYToElementY(int y)
	{
		return hasHeaderRow()  ?  y + 1  :  y;
	}
	
	protected int elementXToTableX(int x)
	{
		return hasHeaderColumn()  ?  x - 1  :  x;
	}
	
	protected int elementYToTableY(int y)
	{
		return hasHeaderRow()  ?  y - 1  :  y;
	}
	
	protected abstract int getHeight();
	protected abstract int getRowWidth(int row);
	protected abstract int getMaxRowWidth();
}
