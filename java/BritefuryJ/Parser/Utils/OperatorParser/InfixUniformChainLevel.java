//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.UnknownFieldNameException;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Sequence;

public class InfixUniformChainLevel extends OperatorLevel
{
	private static class GetSecondParseAction implements ParseAction
	{
		@Override
		public Object invoke(Object input, int pos, int end, Object x, Map<String, Object> bindings)
		{
			@SuppressWarnings("unchecked")
			List<Object> xs = (List<Object>)x;
			return xs.get( 1 );
		}
	}
	
	private static GetSecondParseAction _getSecond = new GetSecondParseAction();
	
	
	// Build AST Node infix chain action
	protected static class BuildASTNodeInfixUniformChainAction implements InfixUniformChainParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeInfixUniformChainAction(DMObjectClass nodeClass, String fieldName)
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { fieldName };

			if ( nodeClass.getFieldIndex( fieldName ) == -1 )
			{
				throw new UnknownFieldNameException( fieldName );
			}
		}
		
		
		public Object invoke(Object input, int begin, int end, List<Object> xs)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { xs } );
			}
			catch (UnknownFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}

	// Python infix chain action
	protected static class PyInfixUniformChainParseAction implements InfixUniformChainParseAction
	{
		private PyObject callable;
		
		
		public PyInfixUniformChainParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(Object input, int begin, int end, List<Object> xs)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), new PyInteger( begin ), new PyInteger( end ), Py.java2py( xs ) } );
		}
	}

	
	
	// A parse action that wraps an InfixChainParseAction
	private static class InfixUniformChainAction implements ParseAction
	{
		private InfixUniformChainParseAction action;
		
		
		public InfixUniformChainAction(InfixUniformChainParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			List<Object> ys = (List<Object>)xs.get( 1 );
			
			ArrayList<Object> zs = new ArrayList<Object>();
			zs.ensureCapacity( ys.size() + 1 );
			zs.add( xs.get( 0 ) );
			zs.addAll( ys );

			return action.invoke( input, begin, end, zs );
		}
	}

	
	
	private ParserExpression opExpression;
	private InfixUniformChainAction action;
	
	
	
	public InfixUniformChainLevel(ParserExpression opExpression, InfixUniformChainParseAction action)
	{
		this.opExpression = opExpression;
		
		this.action = new InfixUniformChainAction( action );
	}
	
	public InfixUniformChainLevel(ParserExpression opExpression, DMObjectClass nodeClass, String fieldName)
	{
		this( opExpression, new BuildASTNodeInfixUniformChainAction( nodeClass, fieldName ) );
	}

	public InfixUniformChainLevel(ParserExpression opExpression, PyObject callable)
	{
		this( opExpression, new PyInfixUniformChainParseAction( callable ) );
	}

	
	public InfixUniformChainLevel(String operator, InfixUniformChainParseAction action)
	{
		this( ParserExpression.coerce( operator ), action );
	}
	
	public InfixUniformChainLevel(String operator, DMObjectClass nodeClass, String fieldName)
	{
		this( ParserExpression.coerce( operator ), new BuildASTNodeInfixUniformChainAction( nodeClass, fieldName ) );
	}

	public InfixUniformChainLevel(String operator, PyObject callable)
	{
		this( ParserExpression.coerce( operator ), new PyInfixUniformChainParseAction( callable ) );
	}

	
	
	@Override
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression rightSubexp = previousLevelParser;
		
		ParserExpression rightChoice = new Sequence( new ParserExpression[] { opExpression, rightSubexp } ).action( _getSecond );

		// <thisLeverParser> <rightChoice>+
		ParserExpression p = new Sequence( new ParserExpression[] { previousLevelParser, rightChoice.oneOrMore() } );
		// => action
		return p.action( action );
	}

	@Override
	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		return null;
	}
}
