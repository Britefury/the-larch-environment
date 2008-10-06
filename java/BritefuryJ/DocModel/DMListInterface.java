//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;

import org.python.core.PySlice;

public interface DMListInterface extends List<Object>
{
	public void append(Object x);
	public void extend(List<Object> xs);
	public void insert(int i, Object x);
	
	public Object __getitem__(int i);
	public List<Object> __getitem__(PySlice i);
	public void __setitem__(int i, Object x);
	public void __setitem__(PySlice i, List<Object> xs);
	public void __delitem__(int i);
	public void __delitem__(PySlice i);
	public int index(Object x);
	
	DMListInterface __add__(List<Object> xs);
}
