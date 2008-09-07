package BritefuryJ.Cell;


public class CellEvaluatorLiteral extends CellEvaluator
{
	private Object value;
	
		
	
	public CellEvaluatorLiteral(Object value)
	{
		super();
		this.value = value;
	}


	public Object evaluate()
	{
		return value;
	}



	public boolean isLiteral()
	{
		return true;
	}
}
