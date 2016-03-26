//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;

public abstract class UnaryOperatorLevel extends OperatorLevel
{
	protected ArrayList<UnaryOperator> operators;
	
	
	//
	//
	// Constructor
	//
	//
	
	public UnaryOperatorLevel(List<UnaryOperator> ops)
	{
		operators = new ArrayList<UnaryOperator>();
		operators.addAll( ops );
	}
}
