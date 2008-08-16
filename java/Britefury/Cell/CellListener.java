package BritefuryJ.Cell;




public interface CellListener {
	public void onCellChanged(CellInterface cell);
	public void onCellEvaluator(CellInterface cell, CellEvaluator oldEval, CellEvaluator newEval);
	public void onCellValidity(CellInterface cell);
}
