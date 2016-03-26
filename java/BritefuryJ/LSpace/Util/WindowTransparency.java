//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Util;

import java.awt.Shape;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WindowTransparency
{
	private static Class<?> awtUtilitiesClass;
	private static Method mSetWindowOpacity, mGetWindowOpacity, mSetWindowShape, mGetWindowShape, mSetWindowOpaque, mIsWindowOpaque;

	private static boolean initialised = false;
	
	static
	{
		try
		{
			awtUtilitiesClass = Class.forName( "com.sun.awt.AWTUtilities" );
			mSetWindowOpacity = awtUtilitiesClass.getMethod( "setWindowOpacity", Window.class, float.class );
			mGetWindowOpacity = awtUtilitiesClass.getMethod( "getWindowOpacity", Window.class );
			mSetWindowShape = awtUtilitiesClass.getMethod( "setWindowShape", Window.class, Shape.class );
			mGetWindowShape = awtUtilitiesClass.getMethod( "getWindowShape", Window.class );
			mSetWindowOpaque = awtUtilitiesClass.getMethod( "setWindowOpaque", Window.class, boolean.class );
			mIsWindowOpaque = awtUtilitiesClass.getMethod( "isWindowOpaque", Window.class );


			initialised = true;
		}
		catch (NoSuchMethodException ex)
		{
			System.out.println( "Failed to initialise WindowTransparency: NoSuchMethodException" );
		}
		catch (SecurityException ex)
		{
			System.out.println( "Failed to initialise WindowTransparency: SecurityException" );
		}
		catch (ClassNotFoundException ex)
		{
			System.out.println( "Failed to initialise WindowTransparency: ClassNotFoundException" );
		}
		catch (IllegalArgumentException ex)
		{
			System.out.println( "Failed to initialise WindowTransparency: IllegalArgumentException" );
		}
	}
	
	
	public static void setWindowOpacity(Window window, float opacity)
	{
		if ( initialised )
		{
			try
			{
				mSetWindowOpacity.invoke( null, window, opacity );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
	}

	public static float getWindowOpacity(Window window)
	{
		if ( initialised )
		{
			try
			{
				return (Float)mGetWindowOpacity.invoke( null, window );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
		return 1.0f;
	}

	public static void setWindowShape(Window window, Shape shape)
	{
		if ( initialised )
		{
			try
			{
				mSetWindowShape.invoke( null, window, shape );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
	}

	public static Shape getWindowShape(Window window)
	{
		if ( initialised )
		{
			try
			{
				return (Shape)mGetWindowShape.invoke( null, window );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
		return null;
	}

	public static void setWindowOpaque(Window window, boolean opaque)
	{
		if ( initialised )
		{
			try
			{
				mSetWindowOpaque.invoke( null, window, opaque );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
	}

	public static boolean isWindowOpaque(Window window)
	{
		if ( initialised )
		{
			try
			{
				return (Boolean)mIsWindowOpaque.invoke( null, window );
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (IllegalAccessException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
		return true;
	}
}
