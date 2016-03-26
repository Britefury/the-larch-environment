//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;


public interface InfixChainOperatorParseAction
{
	public Object invoke(Object input, int begin, int end, Object x, Object opValue);
}
