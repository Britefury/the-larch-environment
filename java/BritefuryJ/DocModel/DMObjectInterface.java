//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DMObjectInterface extends Map<String, Object>
{
	public Object copy();
	public Object get(String key, Object defaultValue);
	
	public List<List<Object>> items();
	public Iterator<List<Object>> iteritems();
	
	public Set<String> keys();
	public Iterator<String> iterkeys();

	public Iterator<Object> itervalues();
	
	public Object pop(String key);
	public Object pop(String key, Object defaultValue);
	public Object popitem();
	
	public Object setdefault(String key);
	public Object setdefault(String key, Object defaultValue);
	
	public void update(Map<String, Object> table);
	
	
	public DMObjectClass getDMClass();
	public Object getFieldValue(int index);
}
