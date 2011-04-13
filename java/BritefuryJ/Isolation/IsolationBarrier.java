//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.awt.Color;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.Pres.GenericStyle;
import BritefuryJ.DefaultPerspective.Pres.ObjectBox;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.StyleSheet.StyleSheet;

public class IsolationBarrier implements Presentable
{
	protected static IsolationPicklerState isolationPicklerState;
	protected static IsolationUnpicklerState isolationUnpicklerState;
	
	
	private Object value = null;
	private transient IsolationUnpicklerState unpickler = null;
	private int index = -1;
	
	
	public IsolationBarrier()
	{
	}
	
	public IsolationBarrier(Object value)
	{
		this.value = value;
	}
	
	
	public PyObject __getstate__()
	{
		if ( isolationPicklerState != null )
		{
			return Py.newInteger( isolationPicklerState.isolatedValue( value ) + 1 );
		}
		else
		{
			throw Py.TypeError( "No island pickler available" );
		}
	}
	
	public void __setstate__(PyObject state)
	{
		if ( isolationUnpicklerState != null )
		{
			if ( state instanceof PyInteger )
			{
				value = null;
				unpickler = isolationUnpicklerState;
				index = state.asInt() - 1;
			}
			else
			{
				throw Py.TypeError( "Pickle state should be a Python integer" );
			}
		}
		else
		{
			throw Py.TypeError( "No island unpickler available" );
		}
	}
	
	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}

	
	
	public Object getValue()
	{
		if ( index != -1 )
		{
			value = unpickler.getIsolatedValue( index );
			unpickler = null;
			index = -1;
		}
		return value;
	}
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return style.applyTo( new ObjectBox( "ISOLATION BARRIER", Pres.coerceNonNull( getValue() ) ) );
	}
	
	
	private static final StyleSheet style = StyleSheet.instance.withAttr( GenericStyle.objectBorderPaint, new Color( 0.25f, 0.0f, 0.5f ) );
}