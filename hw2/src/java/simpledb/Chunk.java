package simpledb;

/**
 * Chunk contains tuples that have been read in and stored to
 * minimize the number of page accesses to read/write tuples
 * for ChunkNestedLoopJoin.
 */
public class Chunk {
    private int chunkSize;
    private Tuple[] tupleArray;

    /**
     * Create a new Chunk with the specified chunkSize (int).
     *
     * @param tupleArray
     *            the tuples that are read in and stored in 
     *            this Chunk.           
     */
    public Chunk(int chunkSize) {
        // IMPLEMENT ME
        this.chunkSize = chunkSize;
        tupleArray = new Tuple[chunkSize];
    }

    /**
     * Load the chunk with tuples. Max number of tuples = chunkSize.
     *
     * @param The iterator that stores a table's tuples.
     */
    public void loadChunk(DbIterator iterator) throws DbException, TransactionAbortedException {
        // IMPLEMENT ME
        int i = 0;
        tupleArray = new Tuple[chunkSize];
        while (iterator.hasNext() && i < chunkSize) {
            tupleArray[i] = iterator.next();
            i++;
        }
    }

    /**
     * @return The tupleArray of this Chunk.
     */
    public Tuple[] getChunkTuples() {
        // IMPLEMENT ME
        return tupleArray;
    }

}
