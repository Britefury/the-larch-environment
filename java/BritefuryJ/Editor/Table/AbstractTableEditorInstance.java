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
				
				Target target = elem.getRootElement().getTarget();
				if ( target instanceof TableTarget )
				{
					TableTarget tableTarget = (TableTarget)target;
					if ( tx == tableTarget.x  &&  ty == tableTarget.y )
					{
						return null;
					}
				}
				
				target = new TableTarget( AbstractTableEditorInstance.this, table, tx, ty );
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
		private ModelType model;
		
		
		public TableEditorPres(Object contents, ModelType model)
		{
			this.contents = Pres.coerce( contents );
			this.model = model;
		}

		@Override
		public DPElement present(PresentationContext ctx, StyleValues style)
		{
			TargetInteractor interactor = new TargetInteractor();
			
			DPElement element = contents.present( ctx, style );
			DPElement tableElement = element.bredthFirstSearchByType( TableElement.class );
			tableElement.addElementInteractor( interactor );
			tableElement.setFixedValue( model );
			return element;
		}
		
	}
	
	
	protected AbstractTableEditor<ModelType> editor;
	
	
	protected AbstractTableEditorInstance(AbstractTableEditor<ModelType> editor)
	{
		this.editor = editor;
	}
	
	
	
	protected abstract Pres presentTable(ModelType model);
	
	

	protected Object[][] getSelectedData(TableSelection tableSelection, DPElement tableElement, int x0, int y0, int x1, int y1)
	{
		if ( tableElement.isRealised() )
		{
			@SuppressWarnings("unchecked")
			ModelType model = (ModelType)tableElement.getFixedValue();
			return editor.getBlock( model, x0, y0, x1 + 1 - x0, y1 + 1 - y0 );
		}
		else
		{
			return null;
		}
	}
	
	protected Pres editTable(ModelType model)
	{
		Pres table = presentTable( model );
		table = table.withFixedValue( model );
		Pres region = new Region( table, editor.clipboardHandler );
		Pres t = new TableEditorPres( region, model );
		return t;
	}

	
	public int tableXToElementX(int x)
	{
		return x;
	}
	
	public int tableYToElementY(int y)
	{
		return y;
	}
	
	public int elementXToTableX(int x)
	{
		return x;
	}
	
	public int elementYToTableY(int y)
	{
		return y;
	}
}
