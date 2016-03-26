//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
import BritefuryJ.Util.PolymorphicMap;

public class PyMethodDispatch
{
	private static HashMap<PyType, PolymorphicMap<ObjectDispatchPyMethodInvoker>> objectDispatchTableByType = new HashMap<PyType, PolymorphicMap<ObjectDispatchPyMethodInvoker>>();
	private static HashMap<PyType, DMPolymorphicMap<DMObjectNodeDispatchPyMethodInvoker>> dmObjectdispatchTableByType = new HashMap<PyType, DMPolymorphicMap<DMObjectNodeDispatchPyMethodInvoker>>();
	
	
	//
	//
	// Get method invoker for objects or DMObjects
	//
	//
	@SuppressWarnings("unchecked")
	public static DispatchPyMethodInvoker getMethodInvokerForObject(PyObject dispatchInstance, Object node)
	{
		// Get the type (class)
		PyType type = dispatchInstance.getType();
		
		// Determine which dispatch table we want to access  --  DMObject or Object
		if ( node instanceof DMObject )
		{
			// Access the DMObject dispatch table
			DMPolymorphicMap<DMObjectNodeDispatchPyMethodInvoker> dispatchTable = dmObjectdispatchTableByType.get( type );
			
			if ( dispatchTable == null )
			{
				dispatchTable = Py.tojava( getCreateDMObjectDispatchTableFn().__call__( type ), DMPolymorphicMap.class );
				dmObjectdispatchTableByType.put( type, dispatchTable );
			}
			
			return dispatchTable.get( (DMObject)node );
		}
		else
		{
			// Access the Object dispatch table
			PolymorphicMap<ObjectDispatchPyMethodInvoker> dispatchTable = objectDispatchTableByType.get( type );
			
			if ( dispatchTable == null )
			{
				dispatchTable = Py.tojava( getCreateObjectDispatchTableFn().__call__( type ), PolymorphicMap.class );
				objectDispatchTableByType.put( type, dispatchTable );
			}
			
			return dispatchTable.getForInstance( node );
		}
	}
	
	
	
	//
	//
	// Get dispatch table creation functions (from Python)
	//
	//	
	
	private static PyObject _createObjectDispatchTableFn;
	
	public static PyObject getCreateObjectDispatchTableFn()
	{
		if ( _createObjectDispatchTableFn == null )
		{
		        PyObject fromlist = new PyTuple( Py.newString("__doc__") );
			PyModule ObjectMethodDispatch_base = (PyModule)__builtin__.__import__( "Britefury.Dispatch.ObjectMethodDispatch_base", Py.None, Py.None, fromlist );
			_createObjectDispatchTableFn = ObjectMethodDispatch_base.__getattr__( "createDispatchTableForClass" );
		}
		return _createObjectDispatchTableFn;
	}
	
	
	
	private static PyObject _createDMObjectDispatchTableFn;
	
	public static PyObject getCreateDMObjectDispatchTableFn()
	{
		if ( _createDMObjectDispatchTableFn == null )
		{
		        PyObject fromlist = new PyTuple( Py.newString("__doc__") );
			PyModule DMObjectNodeMethodDispatch_base = (PyModule)__builtin__.__import__( "Britefury.Dispatch.DMObjectNodeMethodDispatch_base", Py.None, Py.None, fromlist );
			_createDMObjectDispatchTableFn = DMObjectNodeMethodDispatch_base.__getattr__( "createDispatchTableForClass" );
		}
		return _createDMObjectDispatchTableFn;
	}
	

	
	
	
	
	//
	//
	// Dispatch
	//
	//
	
	public static PyObject methodDispatch(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "ObjectPyMethodDispatch.objectMethodDispatch() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		Object node = Py.tojava( values[1], Object.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );
		
		DispatchPyMethodInvoker invoker = getMethodInvokerForObject( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatch(): could not find method for nodes of type " +
					className( node ) + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}

	public static PyObject methodDispatchAndGetName(PyObject values[])
	{
		if ( values.length < 2 )
		{
			throw Py.TypeError( "ObjectPyMethodDispatch.objectMethodDispatchAndGetName() needs at least 2 arguments" );
		}
		
		PyObject dispatchInstance = values[0];
		Object node = Py.tojava( values[1], Object.class );
		PyObject args[] = new PyObject[values.length-2];
		System.arraycopy( values, 2, args, 0, values.length - 2 );

		DispatchPyMethodInvoker invoker = getMethodInvokerForObject( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchAndGetName(): could not find method for nodes of type " +
					className( node ) + " in class " + dispatchInstance.getType().getName() );
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
	
	public static PyObject methodDispatchFromJava(PyObject dispatchInstance, Object node, Object args[])
	{
		DispatchPyMethodInvoker invoker = getMethodInvokerForObject( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchFromJava(): could not find method for nodes of type " +
					className( node ) + " in class " + dispatchInstance.getType().getName() );
		}
		return invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
	}


	public static PyObject methodDispatchAndGetNameFromJava(PyObject dispatchInstance, Object node, Object args[], String name[])
	{
		DispatchPyMethodInvoker invoker = getMethodInvokerForObject( dispatchInstance, node );
		if ( invoker == null )
		{
			throw new DispatchError( "objectMethodDispatchAndGetNameFromJava(): could not find method for nodes of type " +
					className( node ) + " in class " + dispatchInstance.getType().getName() );
		}
		PyObject result = invoker.invoke( node, dispatchInstance, argsFromJava( args ) );
		name[0] = invoker.getName();
		return result;
	}
	
	
	
	private static String className(Object node)
	{
		if ( node instanceof DMObject )
		{
			return "DMObject[" + ( (DMObject)node ).getDMObjectClass().getName() + "]";
		}
		else
		{
			return node.getClass().getName();
		}
	}
}
