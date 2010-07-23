//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Math.Point2;

public class ObjectCellEditorDropBox extends LiteralCellEditor
{
	protected class DropBoxEditor extends Editor
	{
		protected class Listener implements ObjectDndHandler.DropFn
		{
			@Override
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				setCellValue( ((GSymFragmentView.FragmentModel)data).getModel() );
				return true;
			}
		}
		
		public DropBoxEditor()
		{
			refreshEditor();
		}
		
		protected void refreshEditor()
		{
			Object value = getCellValue( Object.class );
			DPElement e;
			if ( value != null )
			{
				e =styleSheet.objectBox( "DocModel", PrimitiveStyleSheet.instance.staticText( "Value received" ) );
			}
			else
			{
				e = styleSheet.objectBox( "DocModel", PrimitiveStyleSheet.instance.staticText( "null" ) );
			}
			e.addDropDest( GSymFragmentView.FragmentModel.class, new Listener() );
			setElement( e );
		}
	};
	
	
	private GenericPerspectiveStyleSheet styleSheet;
	
	
	public ObjectCellEditorDropBox(LiteralCell cell, GenericPerspectiveStyleSheet styleSheet)
	{
		super( cell );
		this.styleSheet = styleSheet;
	}
	
	
	protected Editor createEditor()
	{
		return new DropBoxEditor();
	}
}
