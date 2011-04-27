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

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMPolymorphicMap;

public class DMObjectNodePyMethodDispatch
{
	private static HashMap<PyType, DMPolymorphicMap<DMDispatchPyMethodInvoker>> dispatchTableByType = new HashMap<PyType, DMPolymorphicMap<DMDispatchPyMethodInvoker>>();
	
	@SuppressWarnings("unchecked")
	public static DMDispatchPyMethodInvoker getMethodInvokerForNode(PyObject dispatchInstance, DMObject node)
	{
		// Get the type (class)
		PyType type = dispatchInstance.getType();
		
		DMPolymorphicMap<DMDispatchPyMethodInvoker> dispatchTable = dispatchTableByType.get( type );
		
		if ( dispatchTable == null )
		{
			dispatchTable = Py.tojava( getCreateDispatchTableFn().__call__( type ), DMPolymorphicMap.class );
			dispatchTableByType.put( type, dispatchTable );
		}
		
		return dispatchTable.get( node );
	}

	
	
	private static PyObject _createDispatchTableFn;
	
	public static PyObject getCreateDispatchTableFn()
	{
		if ( _createDispatchTableFn == null )
		{
		        PyObject fromlist = new PyTuple( Py.newString("__doc__") );
			PyModule DMObjectNodeMethodDispatch_base = (PyModule)__builtin__.__import__( "Britefury.Dispatch.DMObjectNodeMethodDispatch_base", Py.None, Py.None, fromlist );
			_createDispatchTableFn = DMObjectNodeMethodDispatch_base.__getattr__( "createDispatchTableForClass" );
		}
		return _createDispatchTableFn;
	}
	
	public static PyObject dmObjectNodeMethodDispatch(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "DMObjectNodeMethodDispatch.dmObjectNodeMethodDispatch() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		DMObject node = Py.tojava( values[1], DMObject.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );
		
		DMDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "dmObjectNodeMethodDispatch(): could not find method for nodes of type " + node.getDMNodeClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}

	public static PyObject dmObjectNodeMethodDispatchAndGetName(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "DMObjectNodeMethodDispatch.dmObjectNodeMethodDispatchAndGetName() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		DMObject node = Py.tojava( values[1], DMObject.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );

		DMDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "dmObjectNodeMethodDispatch(): could not find method for nodes of type " + node.getDMNodeClass().getName() + " in class " + dispatchInstance.getType().getName() );
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
	
	public static PyObject dmObjectNodeMethodDispatchFromJava(PyObject dispatchInstance, DMObject node, Object args[])
	{
		DMDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "dmObjectNodeMethodDispatch(): could not find method for nodes of type " + node.getDMNodeClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}


	public static PyObject dmObjectNodeMethodDispatchAndGetNameFromJava(PyObject dispatchInstance, DMObject node, Object args[], String name[])
	{
		DMDispatchPyMethodInvoker invoker = getMethodInvokerForNode( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "dmObjectNodeMethodDispatch(): could not find method for nodes of type " + node.getDMNodeClass().getName() + " in class " + dispatchInstance.getType().getName() );
		}
		PyObject result = invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
		name[0] = invoker.getName();
		return result;
	}
}
