package simpledb;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The SymmetricHashJoin operator implements the symmetric hash join operation.
 */
public class SymmetricHashJoin extends Operator {
    private JoinPredicate pred;
    private DbIterator child1, child2;
    private TupleDesc comboTD;
    private Tuple t1 = null;
    private boolean switched = false;
    Iterator<Tuple> t2Iterator = null;


    private HashMap<Object, ArrayList<Tuple>> leftMap = new HashMap<Object, ArrayList<Tuple>>();
    private HashMap<Object, ArrayList<Tuple>> rightMap = new HashMap<Object, ArrayList<Tuple>>();

    /**
     * Constructor. Accepts children to join and the predicate to join them on.
     *
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public SymmetricHashJoin(JoinPredicate p, DbIterator child1, DbIterator child2) {
        this.pred = p;
        this.child1 = child1;
        this.child2 = child2;
        comboTD = TupleDesc.merge(child1.getTupleDesc(), child2.getTupleDesc());
    }

    public TupleDesc getTupleDesc() {
        return comboTD;
    }

    /**
     * Opens the iterator.
     */
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // IMPLEMENT ME
        child1.open();
        child2.open();
        super.open();
    }

    /**
     * Closes the iterator.
     */
    public void close() {
        // IMPLEMENT ME
        child1.close();
        child2.close();
        super.close();
    }

    /**
     * Rewinds the iterator. You should not be calling this method for this join. 
     */
    public void rewind() throws DbException, TransactionAbortedException {
        child1.rewind();
        child2.rewind();
        this.leftMap.clear();
        this.rightMap.clear();
    }

    /**
     * Fetches the next tuple generated by the join, or null if there are no 
     * more tuples.  Logically, this is the next tuple in r1 cross r2 that
     * satifies the join predicate.
     *
     * Note that the tuples returned from this particular implementation are
     * simply the concatenation of joining tuples from the left and right
     * relation.  Therefore, there will be two copies of the join attribute in
     * the results.
     *
     * For example, joining {1,2,3} on equality of the first column with {1,5,6}
     * will return {1,2,3,1,5,6}.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // IMPLEMENT ME
        while (t1 != null || child1.hasNext() || child2.hasNext()) {
            if (t1 == null) {
//                System.out.println("t1 is null!! " + switched);

                if (!child1.hasNext()) {
                    switchRelations();
                }
                t1 = child1.next();
                insertLeft(t1);
            }

            Object key = t1.getField(pred.getField1());

            if (inRight(key)) {
                if (t2Iterator == null) {
                    t2Iterator = rightMap.get(key).iterator();
                }
                while (t2Iterator.hasNext()) {
//                    System.out.println("iterator!");
                    Tuple t2 = t2Iterator.next();
                    Tuple s1, s2;
                    if (switched) {
                        s1 = t2;
                        s2 = t1;
                    } else {
                        s1 = t1;
                        s2 = t2;
                    }
                    return combine(s1, s2);
                }
                t2Iterator = null;
            }
            t1 = null;
        }
        return null;
    }

    private Tuple combine(Tuple s1, Tuple s2) {
        int td1n = s1.getTupleDesc().numFields();
        int td2n = s2.getTupleDesc().numFields();

        // set fields in combined tuple
        Tuple t = new Tuple(comboTD);
        for (int i = 0; i < td1n; i++)
            t.setField(i, s1.getField(i));
        for (int i = 0; i < td2n; i++)
            t.setField(td1n + i, s2.getField(i));
        return t;
    }


    private boolean inLeft(Object key) {
        return leftMap.containsKey(key);
    }

    private boolean inRight(Object key) {
        return rightMap.containsKey(key);
    }

    private void insertLeft(Tuple tup) {
//        System.out.println(tup);
        Object key = tup.getField(pred.getField1());
        if (inLeft(key)) {
//            System.out.print(tup);
//            System.out.println(leftMap.get(key).size());

            leftMap.get(key).add(tup);
        } else {
            ArrayList<Tuple> newArr = new ArrayList<Tuple>();
            newArr.add(tup);
            leftMap.put(key, newArr);
        }
    }

    private void insertRight(Tuple tup) {
        Object key = tup.getField(pred.getField2());
        if (inRight(tup)) {
            rightMap.get(key).add(tup);
        } else {
            ArrayList<Tuple> newArr = new ArrayList<Tuple>();
            newArr.add(tup);
            rightMap.put(key, newArr);
        }
    }

    /**
     * Switches the inner and outer relation.
     */
    private void switchRelations() throws TransactionAbortedException, DbException {
        // IMPLEMENT ME
        System.out.println("Switched!");
        switched = !switched;

        HashMap<Object, ArrayList<Tuple>> temp;
        temp = leftMap;
        leftMap = rightMap;
        rightMap = temp;

        t1 = null;
        DbIterator tempI = child1;
        child1 = child2;
        child2 = tempI;
        pred = new JoinPredicate(pred.getField2(), pred.getOperator(), pred.getField1());
    }

    @Override
    public DbIterator[] getChildren() {
        return new DbIterator[]{this.child1, this.child2};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        this.child1 = children[0];
        this.child2 = children[1];
    }

}