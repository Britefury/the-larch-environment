//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ParserHelpers;

public interface ParseResultInterface
{
	public boolean isValid();
	public Object getValue();
	public int getBegin();
	public int getEnd();
}
