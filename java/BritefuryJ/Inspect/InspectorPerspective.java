//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Inspect;

import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DocModelPresenter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
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
