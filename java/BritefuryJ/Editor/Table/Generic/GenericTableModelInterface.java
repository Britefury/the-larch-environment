//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.List;

public interface GenericTableModelInterface
{
	int getWidth();
	int getHeight();
	List<Object> getRow(int rowIndex);
	Object get(int x, int y);
	void set(int x, int y, Object value);
	Object[][] getBlock(int x, int y, int w, int h);
	void putBlock(int x, int y, Object[][] block);
	void deleteBlock(int x, int y, int w, int h);
}
