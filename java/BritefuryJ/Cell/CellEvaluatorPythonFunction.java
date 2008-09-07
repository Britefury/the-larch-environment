package BritefuryJ.Cell;


import org.python.core.PyObject;


public class CellEvaluatorPythonFunction extends CellEvaluator
{
	private PyObject func;
	
		
	
	public CellEvaluatorPythonFunction(PyObject func)
	{
		super();
		this.func = func;
	}


	public Object evaluate()
	{
		return func.__call__();
	}
}
