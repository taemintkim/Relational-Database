package simpledb;

public class QueryPlans {

	public QueryPlans(){
	}

	//SELECT * FROM T1, T2 WHERE T1.column0 = T2.column0;
	public Operator queryOne(DbIterator t1, DbIterator t2) {
		// IMPLEMENT ME
		JoinPredicate pred = new JoinPredicate(0, Predicate.Op.EQUALS, 0);
		return new Join(pred, t1, t2);
	}

	//SELECT * FROM T1, T2 WHERE T1. column0 > 1 AND T1.column1 = T2.column1;
	public Operator queryTwo(DbIterator t1, DbIterator t2) {
		// IMPLEMENT ME
		Predicate pred1 = new Predicate(0, Predicate.Op.GREATER_THAN, new IntField(1));
		Filter f1 = new Filter(pred1, t1);
		JoinPredicate jpred = new JoinPredicate(1, Predicate.Op.EQUALS, 1);
		return new Join(jpred, f1, t2);
	}

	//SELECT column0, MAX(column1) FROM T1 WHERE column2 > 1 GROUP BY column0;
	public Operator queryThree(DbIterator t1) {
		// IMPLEMENT ME
		Predicate pred1 = new Predicate(2, Predicate.Op.GREATER_THAN, new IntField(1));
		Filter f1 = new Filter(pred1, t1);
		return new Aggregate(f1, 1, 0, Aggregator.Op.MAX);
	}

	// SELECT ​​* FROM T1, T2
	// WHERE T1.column0 < (SELECT COUNT(*​​) FROM T3)
	// AND T2.column0 = (SELECT AVG(column0) FROM T3)
	// AND T1.column1 >= T2. column1
	// ORDER BY T1.column0 DESC;
	public Operator queryFour(DbIterator t1, DbIterator t2, DbIterator t3) throws TransactionAbortedException, DbException {
		// IMPLEMENT ME
		t1.open();
		t2.open();
		t3.open();

		Aggregate ag1 = new Aggregate(t3, 0, -1, Aggregator.Op.COUNT);
		ag1.open();
		Predicate p1 = new Predicate(0, Predicate.Op.LESS_THAN, ag1.fetchNext().getField(0));
		Filter f1 = new Filter(p1, t1);
		f1.open();

		Aggregate ag2 = new Aggregate(t3, 0, -1, Aggregator.Op.AVG);
		ag2.open();

		Predicate p2 = new Predicate(0, Predicate.Op.EQUALS, ag2.fetchNext().getField(0));
		Filter f2 = new Filter(p2, t2);
		f2.open();

		JoinPredicate jpred3 = new JoinPredicate(1, Predicate.Op.GREATER_THAN_OR_EQ, 1);
		Join j3 = new Join(jpred3, f1, f2);
		j3.open();

		ag1.close();
		ag2.close();
		f1.close();
		f2.close();
		j3.close();
		t1.close();
		t2.close();
		t3.close();

		return new OrderBy(0, false, j3);
	}

}