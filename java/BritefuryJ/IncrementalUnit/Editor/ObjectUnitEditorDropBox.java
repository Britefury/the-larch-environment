//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IncrementalUnit.Editor;

import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.IncrementalUnit.LiteralUnit;
import BritefuryJ.IncrementalView.FragmentData;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Label;

public class ObjectUnitEditorDropBox extends LiteralUnitEditor
{
	protected class DropBoxEditor extends Editor
	{
		protected class Listener implements ObjectDndHandler.DropFn
		{
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
			{
				setCellValue( ((FragmentData)data).getModel() );
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
			Pres p;
			if ( value != null )
			{
				p = new ObjectBox( "DocModel", new Label( "Value received" ) );
			}
			else
			{
				p = new ObjectBox( "DocModel", new Label( "null" ) );
			}
			p = p.withDropDest( FragmentData.class, new Listener() );
			setPres( p );
		}
	}


	public ObjectUnitEditorDropBox(LiteralUnit cell)
	{
		super( cell );
	}
	
	
	protected Editor createEditor()
	{
		return new DropBoxEditor();
	}
}
