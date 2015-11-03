package simpledb;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query. 
 */
public class TableStats {

    /**
     * These static variables and methods helps the database access TableStats
     * for any given table.
     */
    private static final ConcurrentHashMap<String, TableStats> statsMap = new ConcurrentHashMap<String, TableStats>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }
    
    public static void setStatsMap(HashMap<String,TableStats> s) {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics() {
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    static final int NUM_HIST_BINS = 100;
    private final int ioCostPerPage;
    private final TupleDesc tupleDesc;
    private int numPages, numTuples;
    private int numStats, numHists;
    private List<Object> info = new ArrayList <Object>();

    // TODO: add any fields that you may need

    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     * 
     * @param tableid
     *            The table over which to compute statistics
     * @param ioCostPerPage
     *            The cost per page of IO. This doesn't differentiate between
     *            sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage) {
        // For this function, we use the DbFile for the table in question,
        // then scan through its tuples and calculate the values that you
        // to build the histograms.

        // TODO: Fill out the rest of the constructor.
        // Feel free to change anything already written, it's only a guideline

        // Create a histogram or statistic for every attribute in the table. 
        // If the attribute is a string attribute, create a StringHistogram. 
        // If it is an integer, create a IntStatistics.
        // Scan the table, selecting out all of the fields of all of the 
        //      tuples and use them to populate the corresponding statistic or histogram.

        this.ioCostPerPage = ioCostPerPage;
        DbFile file = Database.getCatalog().getDbFile(tableid);
        tupleDesc = file.getTupleDesc();
        numPages = ((HeapFile) file).numPages();
        numTuples = 0;

        int numFields = tupleDesc.numFields();

        // TODO: what goes here?

        // ArrayList<IntStatistics> statList = new ArrayList<IntStatistics>();
        // ArrayList<StringHistogram> histList = new ArrayList<StringHistogram>();
        // ArrayList<Object> list = new ArrayList<Object>();

        
        // List<Object> info = new ArrayList <Object>();

        for (int i = 0; i < numFields; i++){
            if (tupleDesc.getFieldType(i) == Type.INT_TYPE){
                info.add(new IntStatistics(NUM_HIST_BINS));
                // IntStatistics statistic = new IntStatistics(NUM_HIST_BINS);
            }
            else{
                info.add(new StringHistogram(NUM_HIST_BINS));  
            }
        }

        final DbFileIterator iter = file.iterator(null);
        try {
            iter.open();

            while (iter.hasNext()) {
                Tuple t = iter.next();
                numTuples++;
                // TODO: and here?
                int statCounter = 0;
                int histCounter = 0;
                for (int i = 0; i < numFields; i++){
                    if (tupleDesc.getFieldType(i) == Type.INT_TYPE){
                        IntField intField = (IntField)t.getField(i);
                        ((IntStatistics)info.get(statCounter)).addValue(intField.getValue());
                        statCounter++;
                    }
                    else{
                        ((StringHistogram)info.get(histCounter)).addValue(t.getField(i).toString());
                        histCounter++;
                    }
                }
                if ((statCounter + histCounter) != numFields){
                   System.out.println("there is something wrong here!!!!!!");
                }
            }
            iter.close();
        } catch (DbException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is ioCostPerPage. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * 
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     * 
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
        // TODO: some code goes here
        return numPages * ioCostPerPage;
        // return 0;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     * 
     * @param selectivityFactor
     *            The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // TODO: some code goes here
        double result = numTuples * selectivityFactor;
        int result1 = (int)result;
        return result1;
        // return 0;
    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     * 
     * @param field
     *            The field over which the predicate ranges
     * @param op
     *            The logical operation in the predicate
     * @param constant
     *            The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // TODO: some code goes here

        // Using your statistics (e.g., an IntStatistics or StringHistogram 
        //     depending on the type of the field - SimpleDB only has integers 
        //     or string field types!), 
        // estimate the selectivity of predicate field op constant on the table.
        if (tupleDesc.getFieldType(field) == Type.INT_TYPE){
            IntStatistics estimate = info.get(field);
            return estimate.estimateSelectivity(op, constant);
        }
        else{
            StringHistogram estimate = info.get(field);
            return estimate.estimateSelectivity(op, constant);
        }
        return 0.0;

    }

    /**
     * The average selectivity of the field under op.
     * @param field
     *        the index of the field
     * @param op
     *        the operator in the predicate
     * The semantic of the method is that, given the table, and then given a
     * tuple, of which we do not know the value of the field, return the
     * expected selectivity. You may estimate this value from the histograms.
     * */
    public double avgSelectivity(int field, Predicate.Op op) {
        // optional: implement for a more nuanced estimation, or for skillz
        return 1.0;
    }

}