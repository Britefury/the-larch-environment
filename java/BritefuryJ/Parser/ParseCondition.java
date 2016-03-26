//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.Map;

public interface ParseCondition
{
	public boolean test(Object input, int pos, int end, Object value, Map<String, Object> bindings);
}
