//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Transformation;

import java.util.ArrayList;

import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMObject;

public class DefaultIdentityTransformationFunction extends TransformationFunction
{
	public DefaultIdentityTransformationFunction()
	{
	}
	
	

	// Pass null as 'xform' to get a shallow copy
	public Object apply(Object x, TransformationFunction xform)
	{
		DMObject dx = (DMObject)x;
		Object[] fieldData = dx.getFieldValuesImmutable();
		Object[] newData = new Object[fieldData.length];
		for (int i = 0; i < fieldData.length; i++)
		{
			newData[i] = applyToChild( fieldData[i], xform );
		}
		return new DMObject( dx.getDMClass(), newData );
	}
	
	
	private Object applyToChild(Object x, TransformationFunction innerNodeXform)
	{
		if ( x == null  ||  x instanceof String  ||  x instanceof PyString  ||  x instanceof PyUnicode )
		{
			return x;
		}
		else if ( x instanceof DMList )
		{
			return applyToChild( (DMList)x, innerNodeXform );
		}
		else if ( x instanceof DMObject )
		{
			if ( innerNodeXform != null )
			{
				return innerNodeXform.apply( x, innerNodeXform );
			}
			else
			{
				return x;
			}
		}
		else
		{
			throw new RuntimeException( "Could not transform node of type " + x.getClass().getName() );
		}
	}
	
	private Object applyToChild(DMList xs, TransformationFunction innerNodeXform)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		values.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			values.add( applyToChild( x, innerNodeXform ) );
		}
		return new DMList( values );
	}
}
