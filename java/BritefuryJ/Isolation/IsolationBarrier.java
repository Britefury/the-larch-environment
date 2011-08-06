//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.StyleSheet.StyleSheet;

public class IsolationBarrier <ValueType> implements Presentable, Trackable
{
	protected static IsolationPicklerState isolationPicklerState;
	protected static IsolationUnpicklerState isolationUnpicklerState;
	
	
	private ValueType value = null;
	private transient IsolationUnpicklerState unpickler = null;
	private int index = -1;
	private ChangeHistory changeHistory = null;
	
	
	public IsolationBarrier()
	{
	}
	
	public IsolationBarrier(ValueType value)
	{
		this.value = value;
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else
		{
			if ( x instanceof IsolationBarrier )
			{
				@SuppressWarnings("unchecked")
				IsolationBarrier<ValueType> i = (IsolationBarrier<ValueType>)x;
				ValueType v = getValue();
				ValueType iv = i.getValue();
				if ( v != null  &&  iv != null )
				{
					return v.equals( iv );
				}
				else
				{
					return ( v != null )  ==  ( iv != null );
				}
			}
			
			return false;
		}
	}
	
	public int hashCode()
	{
		ValueType v = getValue();
		return v != null  ?  v.hashCode()  :  0;
	}
	
	
	public PyObject __getstate__()
	{
		if ( isolationPicklerState != null )
		{
			return Py.newInteger( isolationPicklerState.isolatedValue( getValue() ) + 1 );
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

	
	
	@SuppressWarnings("unchecked")
	public ValueType getValue()
	{
		if ( index != -1 )
		{
			value = (ValueType)unpickler.getIsolatedValue( index );
			unpickler = null;
			index = -1;
			if ( changeHistory != null )
			{
				changeHistory.track( value );
			}
		}
		return value;
	}
	
	
	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return style.applyTo( new ObjectBox( "ISOLATION BARRIER", Pres.coerceNonNull( getValue() ) ) );
	}


	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}

	public void setChangeHistory(ChangeHistory h)
	{
		changeHistory = h;
	}

	public List<Object> getTrackableContents()
	{
		if ( index == -1 )
		{
			return Arrays.asList( new Object[] { value } );
		}
		else
		{
			return null;
		}
	}


	private static final StyleSheet style = StyleSheet.style( ObjectPresStyle.objectBorderPaint.as( new Color( 0.25f, 0.0f, 0.5f ) ) );
}