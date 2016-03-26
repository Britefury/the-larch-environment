//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Span;

public class UnescapedStringAsSpan extends UnescapedString
{
	public UnescapedStringAsSpan(String value)
	{
		super( value );
	}
	
	
	@Override
	protected Pres createContainer(ArrayList<Object> contents)
	{
		return new Span( contents );
	}
}
