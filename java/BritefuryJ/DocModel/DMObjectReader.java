//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.util.Map;

public interface DMObjectReader
{
	DMObject readObject(Map<String, Object> fieldValues);
}
