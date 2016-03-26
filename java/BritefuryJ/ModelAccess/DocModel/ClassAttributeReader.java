//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
