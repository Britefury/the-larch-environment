//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.List;

public interface GenericTableModelInterface
{
	int getWidth();
	int getHeight();
	List<Object> getRow(int rowIndex);
	void insertRow(int i, Object[] row);
	void removeRow(int i);
	Object get(int x, int y);
	void set(int x, int y, Object value);
	Object[][] getBlock(int x, int y, int w, int h);
	void putBlock(int x, int y, Object[][] block);
	void deleteBlock(int x, int y, int w, int h);
}
