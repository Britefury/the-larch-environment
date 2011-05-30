//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Dispatch;

import java.util.HashMap;

import org.python.core.Py;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.Util.PolymorphicMap;

public class ObjectPyMethodDispatch
{
	private static HashMap<PyType, PolymorphicMap<ObjectDispatchPyMethodInvoker>> dispatchTableByType = new HashMap<PyType, PolymorphicMap<ObjectDispatchPyMethodInvoker>>();
	
	@SuppressWarnings("unchecked")
	public static ObjectDispatchPyMethodInvoker getMethodInvokerForNode(PyObject dispatchInstance, Object node)
	{
		// Get the type (class)
		PyType type = dispatchInstance.getType();
		
		PolymorphicMap<ObjectDispatchPyMethodInvoker> dispatchTable = dispatchTableByType.get( type );
		
		if ( dispatchTable == null )
		{
			dispatchTable = Py.tojava( getCreateDispatchTableFn().__call__( type ), PolymorphicMap.class );
			dispatchTableByType.put( type, dispatchTable );
		}
		
		return dispatchTable.getForInstance( node );
	}

	
	
	private static PyObject _createDispatchTableFn;
	
	public static PyObject getCreateDispatchTableFn()
	{
		if ( _createDispatchTableFn == null )
		{
		        PyObject fromlist = new PyTuple( Py.newString("__doc__") );
			PyModule DMObjectNodeMethodDispatch_base = (PyModule)__builtin__.__import__( "Britefury.Dispatch.ObjectMethodDispatch_base", Py.None, Py.None, fromlist );
			_createDispatchTableFn = DMObjectNodeMethodDispatch_base.__getattr__( "createDispatchTableForClass" );
		}
		return _createDispatchTableFn;
	}
	
	public static PyObject objectMethodDispatch(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "ObjectPyMethodDispatch.objectMethodDispatch() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		Object node = Py.tojava( values[1], Object.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );
		
		ObjectDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatch(): could not find method for nodes of type " +
					node.getClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}

	public static PyObject objectMethodDispatchAndGetName(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "ObjectPyMethodDispatch.objectMethodDispatchAndGetName() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		Object node = Py.tojava( values[1], Object.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );

		ObjectDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchAndGetName(): could not find method for nodes of type " +
					node.getClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		PyObject result = invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
		return new PyTuple( result, Py.newString( invoker.getName() ) );
	}


	
	
	private static PyObject[] argsFromJava(Object args[])
	{
		PyObject pyArgs[] = new PyObject[args.length];
		for (int i = 0; i < args.length; i++)
		{
			pyArgs[i] = Py.java2py( args[i] );
		}
		return pyArgs;
	}
	
	public static PyObject objectMethodDispatchFromJava(PyObject dispatchInstance, Object node, Object args[])
	{
		ObjectDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchFromJava(): could not find method for nodes of type " +
					node.getClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}


	public static PyObject objectMethodDispatchAndGetNameFromJava(PyObject dispatchInstance, Object node, Object args[], String name[])
	{
		ObjectDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchAndGetNameFromJava(): could not find method for nodes of type " +
					node.getClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		PyObject result = invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
		name[0] = invoker.getName();
		return result;
	}
}
