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
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.PresCom.InnerFragment;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericPerspective extends GSymObjectPresentationPerspective
{
	public GSymGenericPerspective(ObjectPresentationLocationResolver objPresLocationResolver, GSymGenericObjectPresenterRegistry genericPresenterRegistry)
	{
		super( "__present__", objPresLocationResolver );
		genericPresenterRegistry.registerPerspective( this );
	}

	

	protected Pres presentWithJavaInterface(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
	
	protected Pres presentJavaArray(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		int length = Array.getLength( x );
		Object members[] = new Object[length];
		for (int i = 0; i < length; i++)
		{
			members[i] = new InnerFragment( Array.get( x, i ) );
		}
		return GSymGenericObjectPresenterRegistry.arrayView( Arrays.asList( members ) );
	}
	
	protected Pres presentJavaObjectFallback(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presentJavaObjectAsString( x, fragment, inheritedState );
	}
	
	protected Pres presentPyObjectFallback(PyObject x, GSymFragmentView fragment, SimpleAttributeTable inhritedState)
	{
		return presentPythonObjectAsString( x, fragment, inhritedState );
	}
	
	protected Pres invokeObjectPresenter(ObjectPresenter presenter, Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	
	protected Pres invokePyObjectPresenter(PyObjectPresenter presenter, PyObject x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presenter.presentObject( x, fragment, inheritedState );
	}
	

	
	
	@Override
	public EditHandler getEditHandler()
	{
		return null;
	}


	
	
	private static Pres presentJavaObjectAsString(Object x, GSymFragmentView ctx, SimpleAttributeTable state)
	{
		return new ObjectBox( x.getClass().getName(), asStringStyle.applyTo( new StaticText( x.toString() ) ) );
	}
	
	private static Pres presentPythonObjectAsString(PyObject pyX, GSymFragmentView ctx, SimpleAttributeTable state)
	{
		PyType typeX = pyX.getType();
		return new ObjectBox( typeX.getName(), asStringStyle.applyTo( new StaticText( pyX.toString() ) ) );
	}
	
	
	
	
	
	private static final StyleSheet asStringStyle = StyleSheet.instance.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.fontSize, 14 );
}
