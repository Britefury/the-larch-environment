//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DefaultPerspective;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Pres.ObjectBox;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;

public class DefaultPerspective extends ObjectPresentationPerspective
{
	public DefaultPerspective(ObjectPresentationLocationResolver objPresLocationResolver, DefaultObjectPresenterRegistry genericPresenterRegistry)
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
		return DefaultObjectPresenterRegistry.arrayView( Arrays.asList( members ) );
	}
	
	protected Pres presentJavaObjectFallback(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBox( x.getClass().getName(), PrimitivePresenter.presentJavaObjectInspector( x, fragment, inheritedState ) );
	}
	
	protected Pres presentPyObjectFallback(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		PyType typeX = x.getType();
		return new ObjectBox( typeX.getName(), PrimitivePresenter.presentPythonObjectInspector( x, fragment, inheritedState ) );
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
	public ClipboardHandlerInterface getClipboardHandler()
	{
		return null;
	}
}
