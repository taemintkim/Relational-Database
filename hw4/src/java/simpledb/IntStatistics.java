package simpledb;

/** A class to represent statistics for a single integer-based field.
 */
public class IntStatistics {

    // You made add any other fields you think are necessary.

    private int numTuples;
    private int numDistinctTuples;
    private final boolean[] distinctInts;

    //added variables
    public int maxVal;
    public int minVal;

    // TODO: IMPLEMENT ME

    /**
     * Create a new IntStatistic.
     * 
     * This IntStatistic should maintain a statistics about the integer values that it receives.
     * 
     * The integer values will be provided one-at-a-time through the "addValue()" function.
     */
    public IntStatistics(int bins) {
        numTuples = 0;
        numDistinctTuples = 0;
        distinctInts = new boolean[bins];
        maxVal = -(Integer.MAX_VALUE);
        minVal = Integer.MAX_VALUE;
        // totalVals = 0;

        // TODO: IMPLEMENT ME
        /*
        must be less values than there are bins
        */
    }

    /**
     * Add a value to the set of values that you are tracking statistics for
     * @param v Value to add to the statistics
     */
    public void addValue(int v) {
        // TODO: IMPLEMENT ME

        // hashes the value and keeps an estimate to the number of distinct tuples we've seen
        int index = (hashCode(v) % distinctInts.length + distinctInts.length) % distinctInts.length;
        // System.out.println("value " + v + ". w/ index: " + index);
        if (distinctInts[index] == false) {
            distinctInts[index] = true;
            numDistinctTuples++;
        }
        if (v < minVal){
            minVal = v;
        }
        if (v > maxVal){
            maxVal = v;
        }
        numTuples++;
        // System.out.println("length is: " + distinctInts.length);
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {
        // the approximate number of distinct tuples we've seen in total
        // System.out.println("numTuples is " + numTuples + ". numDistinctTuples " + numDistinctTuples + ". distinct length " + distinctInts.length);
        double val = v * 1.0;
        double numDistinct = ((double) numTuples) * numDistinctTuples / distinctInts.length;
        if(numTuples < distinctInts.length){
            numDistinct = ((double) numTuples) * numDistinctTuples / numTuples;
        }

        // TODO: IMPLEMENT ME
        //find value for equals - to be used in other cases

        // col = value : RF = 1/NDistinct(T)
        // col1 = col2 : RF = 1/MAX(NDistinct(T1), NDistinct(T2))
        // col > value : RF = (High(T)-value)/(High(T)-Low(T))
        // System.out.println("numDistinct " + numDistinct);


        // System.out.println("max val: " + maxVal);
        // System.out.println("min val: " + minVal);

        //edge cases

        double eq_tuples = 1/numDistinct;
        double greater_tuples = 0.5;
        double geq = 0.0;
        boolean gt_max = v > maxVal;
        boolean lt_min = v < minVal;
        // System.out.println("the value of the boolean is " + gt_max);



        // List eq_tuples = new ArrayList();

        if (maxVal != minVal){
            //if maxVal == minVal there's only 1 value
            greater_tuples = (maxVal - val)/(maxVal-minVal);
            // System.out.println("formula result: " + greater_tuples);
        }
        else{
            if (v != maxVal && v != minVal){
                eq_tuples = 0.0;
            }
        }

        if (op == Predicate.Op.EQUALS){
            // System.out.println(numDistinct);
            // if (v > maxVal || v < minVal){return 0.0;}
            if (gt_max || lt_min){
                return 0.0;
            }
            return eq_tuples;
        } else if (op == Predicate.Op.NOT_EQUALS){
            if (gt_max || lt_min){
                return 1.0;
            }
            return (1 - eq_tuples);
        }
        else if (op == Predicate.Op.GREATER_THAN || op == Predicate.Op.GREATER_THAN_OR_EQ){
            if (gt_max){
                // System.out.println("value: " + v + ". maxvalue: " + maxVal);
                return 0.0;
            }
            if (lt_min){
                return 1.0;
            }
            // System.out.println("formula result: " + greater_tuples);
            return greater_tuples;
        } else if (op == Predicate.Op.LESS_THAN || op == Predicate.Op.LESS_THAN_OR_EQ){
            if (lt_min){
                return 0.0;
            }
            if (gt_max){
                return 1.0;
            }
            return (1-greater_tuples);
        }
        // } else if (op == Predicate.Op.GREATER_THAN_OR_EQ){
        //     return eq_tuples+greater_tuples;
        // } else if (op == Predicate.Op.LESS_THAN_OR_EQ){
        //     return 1-(eq_tuples+greater_tuples);
        // }

        return -1.0;
    }

    /**
     * Helper function to make a good hash value of an integer
     */
    static int hashCode(int v) {
        v ^= (v >>> 20) ^ (v >>> 12);
        return v ^ (v >>> 7) ^ (v >>> 4);
    }
}
