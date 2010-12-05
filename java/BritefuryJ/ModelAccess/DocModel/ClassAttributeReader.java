//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ModelAccess.DocModel;

import BritefuryJ.DocModel.DMClassAttribute;
import BritefuryJ.DocModel.DMNode;

public class ClassAttributeReader extends DMModelReader
{
	private DMClassAttribute attr;
	
	
	public ClassAttributeReader(DMClassAttribute attr)
	{
		this.attr = attr;
	}
	
	@Override
	public Object readFromModel(Object model)
	{
		if ( model instanceof DMNode )
		{
			return attr.get( (DMNode)model );
		}
		else
		{
			throw new RuntimeException( "ClassAttributeReader cannot read from objects that are not instances of DMNode" );
		}
	}
}
