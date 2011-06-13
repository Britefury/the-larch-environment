//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.lang.reflect.Array;
import java.util.Arrays;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultObjectPresenterRegistry;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.EditPerspective.EditPerspective;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Projection.AbstractPerspective;

public class CellEditPerspective extends ObjectPresentationPerspective
{
	private CellEditPerspective(AbstractPerspective fallbackPerspective)
	{
		super( "__edit_cell_present__", fallbackPerspective );
		CellEditObjectPresenterRegistry.instance.registerPerspective( this );
	}

	
	
	@Override
	protected Pres presentWithJavaInterface(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x instanceof CellEditPresentable )
		{
			CellEditPresentable p = (CellEditPresentable)x;
			return p.editCellPresent( fragment, inheritedState );
		}
		else
		{
			return null;
		}
	}

	@Override
	protected Pres presentJavaArray(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		int length = Array.getLength( x );
		Object members[] = new Object[length];
		for (int i = 0; i < length; i++)
		{
			members[i] = new InnerFragment( Array.get( x, i ) );
		}
		return DefaultObjectPresenterRegistry.arrayView( Arrays.asList( members ) );
	}

	@Override
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return null;
	}
	
	
	
	public static void notifySetCellValue(DPElement cellElement, Object value)
	{
		CellSetValueEvent cellEvent = new CellSetValueEvent( value );
		cellElement.postTreeEvent( cellEvent );
	}



	public static final CellEditPerspective instance = new CellEditPerspective( EditPerspective.instance );
}
