//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ModelAccess.DocModel;

import BritefuryJ.ModelAccess.ModelReader;

public abstract class DMModelReader implements ModelReader
{
	public StringToIntegerReader stringToInteger()
	{
		return new StringToIntegerReader( this );
	}

	public StringToIntegerReader stringToInteger(int defaultValue)
	{
		return new StringToIntegerReader( this, defaultValue );
	}
}
