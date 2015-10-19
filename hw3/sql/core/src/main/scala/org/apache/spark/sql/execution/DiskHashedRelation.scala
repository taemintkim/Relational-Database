//updated 10/18 7:01
package org.apache.spark.sql.execution

import java.io._
import java.nio.file.{Path, StandardOpenOption, Files}
import java.util.{ArrayList => JavaArrayList}

import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.expressions.{Projection, Row}
import org.apache.spark.sql.execution.CS186Utils._

import scala.collection.JavaConverters._


/**
 * This trait represents a regular relation that is hash partitioned and spilled to
 * disk.
 */
private[sql] sealed trait DiskHashedRelation {
  /**
   *
   * @return an iterator of the [[DiskPartition]]s that make up this relation.
   */
  def getIterator(): Iterator[DiskPartition]

  /**
   * Close all the partitions for this relation. This should involve deleting the files hashed into.
   */
  def closeAllPartitions()
}

/**
 * A general implementation of [[DiskHashedRelation]].
 *
 * @param partitions the disk partitions that we are going to spill to
 */
protected [sql] final class GeneralDiskHashedRelation(partitions: Array[DiskPartition])
  extends DiskHashedRelation with Serializable {

  override def getIterator() = {
    // IMPLEMENT ME
    partitions.iterator
  }

  override def closeAllPartitions() = {
    // IMPLEMENT ME
    partitions.foreach((x: DiskPartition) => {
      x.closePartition();
    })
  }
}

private[sql] class DiskPartition (
                                   filename: String,
                                   blockSize: Int) {
  private val path: Path = Files.createTempFile("", filename)
  private val data: JavaArrayList[Row] = new JavaArrayList[Row]
  private val outStream: OutputStream = Files.newOutputStream(path)
  private val inStream: InputStream = Files.newInputStream(path)
  private val chunkSizes: JavaArrayList[Int] = new JavaArrayList[Int]()
  private var writtenToDisk: Boolean = false
  private var inputClosed: Boolean = false

  /**
   * This method inserts a new row into this particular partition. If the size of the partition
   * exceeds the blockSize, the partition is spilled to disk.
   *
   * @param row the [[Row]] we are adding
   */
  def insert(row: Row) = {
    // IMPLEMENT ME //todo
    if (inputClosed) {
      throw new SparkException("input is closed!")
    }
    data.add(row)
    if (measurePartitionSize() > blockSize) {
      spillPartitionToDisk()
    }
  }

  /**
   * This method converts the data to a byte array and returns the size of the byte array
   * as an estimation of the size of the partition.
   *
   * @return the estimated size of the data
   */
  private[this] def measurePartitionSize(): Int = {
    CS186Utils.getBytesFromList(data).size
  }

  /**
   * Uses the [[Files]] API to write a byte array representing data to a file.
   */
  private[this] def spillPartitionToDisk() = {
    val bytes: Array[Byte] = getBytesFromList(data)

    // This array list stores the sizes of chunks written in order to read them back correctly.
    chunkSizes.add(bytes.size)

    Files.write(path, bytes, StandardOpenOption.APPEND)
    writtenToDisk = true
  }

  /**
   * If this partition has been closed, this method returns an Iterator of all the
   * data that was written to disk by this partition.
   *
   * @return the [[Iterator]] of the data
   */
  def getData(): Iterator[Row] = { //todo
    if (!inputClosed) {
      throw new SparkException("Should not be reading from file before closing input. Bad things will happen!")
    }

    new Iterator[Row] {
      var currentIterator: Iterator[Row] = data.iterator.asScala
      val chunkSizeIterator: Iterator[Int] = chunkSizes.iterator().asScala
      var byteArray: Array[Byte] = null

      override def next(): Row = {
        // IMPLEMENT ME
        if (!currentIterator.hasNext && chunkSizeIterator.hasNext) {
          fetchNextChunk()
        }
        return currentIterator.next()
      }

      override def hasNext(): Boolean = {
        // IMPLEMENT ME
        return chunkSizeIterator.hasNext || currentIterator.hasNext
      }

      /**
       * Fetches the next chunk of the file and updates the iterator. Should return true
       * unless the iterator is empty.
       *
       * @return true unless the iterator is empty.
       */
      private[this] def fetchNextChunk(): Boolean = {
//        IMPLEMENT ME //todo this is still sketch.
        val chunkBytes = getNextChunkBytes(inStream, chunkSizeIterator.next().asInstanceOf[Int], getBytesFromList(data))
        currentIterator = getListFromBytes(chunkBytes).iterator.asScala
        return currentIterator.hasNext
      }
    }
  }

  /**
   * Closes this partition, implying that no more data will be written to this partition. If getData()
   * is called without closing the partition, an error will be thrown.
   *
   * If any data has not been written to disk yet, it should be written. The output stream should
   * also be closed.
   */
  def closeInput() = {
    // IMPLEMENT ME //todo
    if (!writtenToDisk) {
      spillPartitionToDisk()
    }
    outStream.close()
    inputClosed = true
  }


  /**
   * Closes this partition. This closes the input stream and deletes the file backing the partition.
   */
  private[sql] def closePartition() = {
    inStream.close()
    Files.deleteIfExists(path)
  }
}

private[sql] object DiskHashedRelation {

  /**
   * Given an input iterator, partitions each row into one of a number of [[DiskPartition]]s
   * and constructors a [[DiskHashedRelation]].
   *
   * This executes the first phase of external hashing -- using a course-grained hash function
   * to partition the tuples to disk.
   *
   * The block size is approximately set to 64k because that is a good estimate of the average
   * buffer page.
   *
   * @param input the input [[Iterator]] of [[Row]]s
   * @param keyGenerator a [[Projection]] that generates the keys for the input
   * @param size the number of [[DiskPartition]]s
   * @param blockSize the threshold at which each partition will spill
   * @return the constructed [[DiskHashedRelation]]
   */
  def apply (
              input: Iterator[Row],
              keyGenerator: Projection,
              size: Int = 64,
              blockSize: Int = 64000) = {
    // IMPLEMENT ME

    println("begin")
    val hashed_partitions: JavaArrayList[DiskPartition] = new JavaArrayList[DiskPartition]
    // val hashed_partitions: Array[DiskPartition] = new Array[DiskPartition]
    var i = 0
    while (i < size){
      var name = "file"+i.toString()
      val disk_partition = new DiskPartition(name, blockSize)
      hashed_partitions.add(disk_partition) //create an array list with $size number DiskPartitions
      i += 1
    }
    println(hashed_partitions.size()) //for debugging


    while (input.hasNext && input != null) {
      var row = input.next
      var row_keys = keyGenerator(row)
      var hash_val = row_keys.hashCode % size
      var partition_obj = hashed_partitions.get(hash_val) //get the DiskPartition pertaining to that hash value
      partition_obj.insert(row) //insert the row into that partition
    }

//    println(f"the size of the first partition is ${hashed_partitions.get(0).data}")
    // String[] foo = l.toArray(new String[foo.size()]);
//    val array_partitions : Array[DiskPartition] = hashed_partitions.toArray(new Array[DiskPartition](size))// turn the ArrayList of DiskPartiitons into an array
    val array_partitions : Array[DiskPartition] = hashed_partitions.toArray(new Array[DiskPartition](size))// turn the ArrayList of DiskPartiitons into an array
    val hashedRelation : GeneralDiskHashedRelation = new GeneralDiskHashedRelation(array_partitions)
    println(array_partitions(0).getData)
    hashedRelation.closeAllPartitions()
    println("finished")

//    val hashedRelation: DiskHashedRelation = DiskHashedRelation(data.iterator, keyGenerator, 3, 64000)
//    var count: Int = 0
//    for (partition <- hashedRelation.getIterator()) {
//      for (row <- partition.getData()) {
//        println(s"hashcode is ${row.hashCode() % 3}")
//        println(s"the count is at ${count}")
////        assert(row.hashCode() % 3 == count)
//      }
//      count += 1
//    }

    hashedRelation

  //   protected [sql] final class GeneralDiskHashedRelation(partitions: Array[DiskPartition])
  // extends DiskHashedRelation with Serializable {

  }
}