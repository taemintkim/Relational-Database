package simpledb;

import org.junit.Assert;
import org.junit.Test;
import simpledb.Predicate.Op;

public class IntStatisticsTest {

	/**
	 * Test with a minimum and a maximum that are both negative numbers.
	 */
	@Test public void negativeRangeTest() {
		IntStatistics h = new IntStatistics(10);
		
		// All of the values here are negative.
		// Also, there are more of them than there are bins.
		for (int c = -60; c <= -10; c++) {
			h.addValue(c);
			h.estimateSelectivity(Op.EQUALS, c);
		}
		double testVal = h.estimateSelectivity(Op.EQUALS, -33);
		// System.out.print(testVal);
		System.out.println("Test 1");
		Assert.assertEquals(0.02, testVal, 0.01);
	}
	
	/**
	 * Make sure that equality binning does something reasonable.
	 */
	@Test public void opEqualsTest() {
		IntStatistics h = new IntStatistics(10);
		
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		System.out.println("Test 2");
		// need hella more tests for this one
		double testVal1 = h.estimateSelectivity(Op.EQUALS, 3);
		double testVal2 = h.estimateSelectivity(Op.EQUALS, 8);
		// System.out.println("selectivity of 3: " + testVal1);
		// System.out.println("selectivity of 8: " + testVal2);
		// This really should return "1.0"; but,
		// be conservative in case of alternate implementations
		Assert.assertTrue(testVal1 > 0.9);
		Assert.assertTrue(testVal2 < 0.001);
	}
	
	// /**
	//  * Make sure that GREATER_THAN binning does something reasonable.
	//  */
	@Test public void opGreaterThanTest() {
		IntStatistics h = new IntStatistics(10);
		System.out.println("Test 3");
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		h.addValue(1);
		h.addValue(10);

		double testVal1 = h.estimateSelectivity(Op.GREATER_THAN, -1);
		double testVal2 = h.estimateSelectivity(Op.GREATER_THAN, 2);
		double testVal3 = h.estimateSelectivity(Op.GREATER_THAN, 4);
		double testVal4 = h.estimateSelectivity(Op.GREATER_THAN, 12);
		// System.out.println(testVal1 + ". " + testVal2 + ". " + testVal3+ ". " + testVal4 + ". ");
		
		Assert.assertTrue(testVal1 > 0.999);
		Assert.assertEquals(.9, testVal2, .05);
		Assert.assertEquals(.7, testVal3, .05);
		Assert.assertTrue(testVal4 < 0.001);
	}
	
	// /**
	//  * Make sure that LESS_THAN binning does something reasonable.
	//  */
	@Test public void opLessThanTest() {
		IntStatistics h = new IntStatistics(10);
			System.out.println("Test 4");
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		h.addValue(1);
		h.addValue(10);
		
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN, -1) < 0.001);
		Assert.assertEquals(.1, h.estimateSelectivity(Op.LESS_THAN, 2), .1);
		Assert.assertEquals(.3, h.estimateSelectivity(Op.LESS_THAN, 4), .1);
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN, 12) > 0.999);
	}
	
	// /**
	//  * Make sure that GREATER_THAN_OR_EQ binning does something reasonable.
	//  */
	@Test public void opGreaterThanOrEqualsTest() {
		IntStatistics h = new IntStatistics(3);
		System.out.println("Test 5");
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		h.addValue(1);
		h.addValue(10);

		double t1 = h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, -1);
		double t2 = h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 2);
		double t3 = h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 3);
		double t4 = h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 12);
		// System.out.println(t1 + ". " + t2 + ". " + t3+ ". " + t4 + ". ");
		
		Assert.assertTrue(h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, -1) > 0.999);
		Assert.assertTrue(h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 2) > 0.6);
		Assert.assertTrue(h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 3) > 0.45);
		Assert.assertTrue(h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 3) < h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 2));
		Assert.assertTrue(h.estimateSelectivity(Op.GREATER_THAN_OR_EQ, 12) < 0.001);
	}
	
	// /**
	//  * Make sure that LESS_THAN_OR_EQ binning does something reasonable.
	//  */
	@Test public void opLessThanOrEqualsTest() {
		IntStatistics h = new IntStatistics(10);
		
		System.out.println("Test 6");
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		h.addValue(1);
		h.addValue(10);
		
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN_OR_EQ, -1) < 0.001);
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN_OR_EQ, 2) < h.estimateSelectivity(Op.LESS_THAN_OR_EQ, 3));
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN_OR_EQ, 3) < h.estimateSelectivity(Op.LESS_THAN_OR_EQ, 4));
		Assert.assertTrue(h.estimateSelectivity(Op.LESS_THAN_OR_EQ, 12) > 0.999);
	}
	
	// /**
	//  * Make sure that equality binning does something reasonable.
	//  */
	@Test public void opNotEqualsTest() {
		IntStatistics h = new IntStatistics(100);
		
		// Set some values
		h.addValue(3);
		h.addValue(3);
		h.addValue(3);
		
		// Be conservative in case of alternate implementations
		Assert.assertTrue(h.estimateSelectivity(Op.NOT_EQUALS, 3) < 0.001);
		Assert.assertTrue(h.estimateSelectivity(Op.NOT_EQUALS, 8) > 0.01);
	}
}