//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Inspect;

import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DocModelPresenter;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Projection.AbstractPerspective;

public class InspectorPerspective extends AbstractPerspective
{
	private InspectorPerspective()
	{
	}

	
	@Override
	protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres result;
		
		// Python object presentation protocol
		if ( x instanceof PyObject )
		{
			// @x is a Python object - if it offers a __present__ method, use that
			PyObject pyX = (PyObject)x;
			
			if ( PrimitivePresenter.isPrimitivePy( pyX ) )
			{
				result = PrimitivePresenter.presentPrimitivePy( pyX );
			}
			else
			{
				PyType typeX = pyX.getType();
				result = new ObjectBox( typeX.getName(), Inspector.presentPythonObjectInspector( pyX, fragment, inheritedState ) );
			}

			result.setDebugName( pyX.getType().getName() );
			return result;
		}
		else if ( x instanceof DMNode )
		{
			return DocModelPresenter.presentDMNode( (DMNode)x, fragment, inheritedState );
		}
		else
		{
			return JavaInspectorPerspective.instance.presentModel( x, fragment, inheritedState );
		}
	}


	@Override
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return null;
	}


	public static final InspectorPerspective instance = new InspectorPerspective();
}
