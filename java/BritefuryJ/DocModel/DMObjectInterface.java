//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Map;

import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;

public interface DMObjectInterface extends DMNodeInterface
{
	public DMObjectClass getDMObjectClass();
	public boolean isInstanceOf(DMObjectClass cls);
	
	public int getFieldIndex(String key);
	
	public Object get(int index);
	public Object get(String key) throws InvalidFieldNameException;
	
	public void set(int index, Object value);
	public void set(String key, Object value) throws InvalidFieldNameException;
	
	
	public String[] getFieldNames();
	
	public void update(Map<String, Object> table) throws InvalidFieldNameException;
	public void become(DMObject obj);
	
	
	public Object __getitem__(int fieldIndex);
	public Object __getitem__(String key);
	public void __setitem__(int index, Object value);
	public void __setitem__(String key, Object value);
}
