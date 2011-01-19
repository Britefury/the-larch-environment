//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.IncrementalView.FragmentView;

public class GSymGenericPerspective extends GSymObjectPresentationPerspective
{
	public GSymGenericPerspective(ObjectPresentationLocationResolver objPresLocationResolver, GSymGenericObjectPresenterRegistry genericPresenterRegistry)
	{
		super( "__present__", objPresLocationResolver );
		genericPresenterRegistry.registerPerspective( this );
	}

	

	protected Pres presentWithJavaInterface(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x instanceof Presentable )
		{
			Presentable p = (Presentable)x;
			return p.present( fragment, inheritedState );
		}
		else
		{
			return null;
		}
	}
	
	protected Pres presentJavaArray(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		int length = Array.getLength( x );
		Object members[] = new Object[length];
		for (int i = 0; i < length; i++)
		{
			members[i] = new InnerFragment( Array.get( x, i ) );
		}
		return GSymGenericObjectPresenterRegistry.arrayView( Arrays.asList( members ) );
	}
	
	protected Pres presentJavaObjectFallback(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( x.getClass().getName(), GSymPrimitivePresenter.presentJavaObjectInspector( x, fragment, inheritedState ) );
	}
	
	protected Pres presentPyObjectFallback(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		PyType typeX = x.getType();
		return new ObjectBox( typeX.getName(), GSymPrimitivePresenter.presentPythonObjectInspector( x, fragment, inheritedState ) );
	}
	
	protected Pres invokeObjectPresenter(ObjectPresenter presenter, Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	
	protected Pres invokePyObjectPresenter(PyObjectPresenter presenter, PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	

	
	
	@Override
	public ClipboardHandler getClipboardHandler()
	{
		return null;
	}
}
