//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel.Resource;

import java.io.IOException;
import java.util.IdentityHashMap;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.modules.cPickle;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DMNodeClass;
import BritefuryJ.DocModel.DMPickleHelper;

public class DMPyResource extends DMResource
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected static DMNodeClass pyResourceNodeClass = new DMNodeClass( "DMPyResource" );

	
	private PyObject value[] = null;
	
	
	public DMPyResource()
	{
		super();
	}
	
	public DMPyResource(PyObject value)
	{
		this.serialised = serialise( value );
	}
	
	private DMPyResource(String serialised)
	{
		super( serialised );
	}
	
	
	
	public PyObject getPyFactory()
	{
		return DMPickleHelper.getDMPyResourceFactory();
	}
	
	
	public void become(DMNode node)
	{
		if ( node instanceof DMPyResource )
		{
			DMPyResource rsc = (DMPyResource)node;
			serialised = rsc.getSerialisedForm();
			value = null;
		}
		else
		{
			throw new CannotChangeNodeClassException( node.getClass(), getClass() );
		}
	}


	
	public static DMPyResource serialisedResource(String serialised)
	{
		return new DMPyResource( serialised );
	}
	
	
	public Object getValue()
	{
		if ( value == null )
		{
			DMPickleHelper.initialise();
			PyObject v;
			try
			{
				v = (PyObject)cPickle.loads( new PyString( serialised ) );
			}
			catch (Throwable t)
			{
				v = Py.None;
			}
			this.value = new PyObject[] { v };
			this.serialised = null;
		}
		
		return value[0];
	}
	
	
	
	public String getSerialisedForm()
	{
		return serialise( (PyObject)getValue() );
	}
	
	
	public static String serialise(PyObject x)
	{
		DMPickleHelper.initialise();
		PyString s;
		try
		{
			s = cPickle.dumps( x );
		}
		catch (Throwable t)
		{
			s = cPickle.dumps( Py.None );
		}
		return s.getString();
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof DMPyResource )
		{
			DMPyResource r = (DMPyResource)x;
			return getSerialisedForm().equals( r.getSerialisedForm() );
		}
		else
		{
			return false;
		}
	}



	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		String s = serialise( (PyObject)getValue() );
		out.writeUTF( s );
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		serialised = in.readUTF();
		value = null;
	}
	
	
	@Override
	protected Object createDeepCopy(IdentityHashMap<Object, Object> memo)
	{
		if ( serialised != null )
		{
			return new DMPyResource( serialised );
		}
		else
		{
			return new DMPyResource( (PyObject)getValue() );
		}
	}


	@Override
	public DMNodeClass getDMNodeClass()
	{
		return pyResourceNodeClass;
	}


	@Override
	public Iterable<Object> getChildren()
	{
		return childrenIterable;
	}
}
