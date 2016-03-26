//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

public interface ObjectListInterface
{
	int size();
	Object get(int i);
	void append(Object x);
	void removeRange(int start, int end);
}
