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
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericPerspective extends GSymObjectPresentationPerspective
{
	public GSymGenericPerspective(ObjectPresentationLocationResolver objPresLocationResolver)
	{
		super( "__present__", objPresLocationResolver );
		SystemObjectPresenters.registerPresenters( this );
	}

	

	protected DPElement presentWithJavaInterface(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		if ( x instanceof Presentable )
		{
			Presentable p = (Presentable)x;
			return p.present( ctx, GenericPerspectiveStyleSheet.asGenericPerspectiveStyleSheetOrDefault( styleSheet ), state );
		}
		else
		{
			return null;
		}
	}
	
	protected DPElement presentJavaObjectFallback(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		return presentJavaObjectAsString( x, ctx, GenericPerspectiveStyleSheet.asGenericPerspectiveStyleSheetOrDefault( styleSheet ), state );
	}
	
	protected DPElement presentPyObjectFallback(PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		return presentPythonObjectAsString( x, ctx, GenericPerspectiveStyleSheet.asGenericPerspectiveStyleSheetOrDefault( styleSheet ), state );
	}
	
	@SuppressWarnings("unchecked")
	protected DPElement invokeObjectPresenter(ObjectPresenter<? extends StyleSheet> presenter, Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		ObjectPresenter<GenericPerspectiveStyleSheet> genericPresenter = (ObjectPresenter<GenericPerspectiveStyleSheet>)presenter;
		return genericPresenter.presentObject( x, ctx, GenericPerspectiveStyleSheet.asGenericPerspectiveStyleSheetOrDefault( styleSheet ), state );
	}
	
	@SuppressWarnings("unchecked")
	protected DPElement invokePyObjectPresenter(PyObjectPresenter<? extends StyleSheet> presenter, PyObject x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		PyObjectPresenter<GenericPerspectiveStyleSheet> genericPresenter = (PyObjectPresenter<GenericPerspectiveStyleSheet>)presenter;
		return genericPresenter.presentObject( x, ctx, GenericPerspectiveStyleSheet.asGenericPerspectiveStyleSheetOrDefault( styleSheet ), state );
	}
	

	
	
	@Override
	public StyleSheet getStyleSheet()
	{
		return GenericPerspectiveStyleSheet.instance;
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


	
	
	private static DPElement presentJavaObjectAsString(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		return styleSheet.objectBox( x.getClass().getName(), asStringStyle.staticText( x.toString() ) );
	}
	
	private static DPElement presentPythonObjectAsString(PyObject pyX, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		PyType typeX = pyX.getType();
		return styleSheet.objectBox( typeX.getName(), asStringStyle.staticText( pyX.toString() ) );
	}
	
	
	
	
	
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFontItalic( true ).withFontSize( 14 );
}
