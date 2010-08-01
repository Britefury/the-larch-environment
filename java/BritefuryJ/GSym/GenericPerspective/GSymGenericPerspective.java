//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import org.python.core.PyObject;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericPerspective extends GSymObjectPresentationPerspective
{
	public GSymGenericPerspective(ObjectPresentationLocationResolver objPresLocationResolver, GSymGenericObjectPresenterRegistry genericPresenterRegistry)
	{
		super( "__present__", objPresLocationResolver );
		genericPresenterRegistry.registerPerspective( this );
	}

	

	protected Pres presentWithJavaInterface(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
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
	
	protected Pres presentJavaObjectFallback(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
	{
		return presentJavaObjectAsString( x, fragment, inheritedState );
	}
	
	protected Pres presentPyObjectFallback(PyObject x, GSymFragmentView fragment, AttributeTable inhritedState)
	{
		return presentPythonObjectAsString( x, fragment, inhritedState );
	}
	
	@SuppressWarnings("unchecked")
	protected Pres invokeObjectPresenter(ObjectPresenter<? extends StyleSheet> presenter, Object x, GSymFragmentView fragment, AttributeTable inheritedState)
	{
		ObjectPresenter<GenericPerspectiveStyleSheet> genericPresenter = (ObjectPresenter<GenericPerspectiveStyleSheet>)presenter;
		return genericPresenter.presentObject( x, fragment, inheritedState );
	}
	
	@SuppressWarnings("unchecked")
	protected Pres invokePyObjectPresenter(PyObjectPresenter<? extends StyleSheet> presenter, PyObject x, GSymFragmentView fragment, AttributeTable inheritedState)
	{
		PyObjectPresenter<GenericPerspectiveStyleSheet> genericPresenter = (PyObjectPresenter<GenericPerspectiveStyleSheet>)presenter;
		return genericPresenter.presentObject( x, fragment, inheritedState );
	}
	

	
	
	@Override
	public AttributeTable getInitialInheritedState()
	{
		return AttributeTable.instance;
	}
	
	@Override
	public EditHandler getEditHandler()
	{
		return null;
	}


	
	
	private static Pres presentJavaObjectAsString(Object x, GSymFragmentView ctx, AttributeTable state)
	{
		return new ObjectBox( x.getClass().getName(), asStringStyle.applyTo( new StaticText( x.toString() ) ) );
	}
	
	private static Pres presentPythonObjectAsString(PyObject pyX, GSymFragmentView ctx, AttributeTable state)
	{
		PyType typeX = pyX.getType();
		return new ObjectBox( typeX.getName(), asStringStyle.applyTo( new StaticText( pyX.toString() ) ) );
	}
	
	
	
	
	
	private static final StyleSheet2 asStringStyle = StyleSheet2.instance.withAttr( Primitive.fontItalic, true ).withAttr( Primitive.fontSize, 14 );
}
