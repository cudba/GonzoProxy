package ch.compass.gonzoproxy.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ByteArraysTest {

	@Test
	public void testTrimLengthValid() {
		byte[] testArray = new byte[]{'A','B','C','D'};
		byte[] expectedArray = new byte[]{'A','B'};
		
		int fromIndex = 0;
		int length = 2;
		assertArrayEquals(expectedArray, ByteArraysUtils.trim(testArray, fromIndex, length));
		
	}
	
	@Test
	public void testTrimLengthTooBig()	{
		byte[] testArray = new byte[]{'A','B','C','D'};
		
		int fromIndex = 0;
		int length = testArray.length + 1;
		assertArrayEquals(testArray, ByteArraysUtils.trim(testArray, fromIndex, length));
		
	}
	
	@Test
	public void testTrimLengthNegative() {
		byte[] testArray = new byte[]{'A','B','C','D'};
		
		int fromIndex = 0;
		int length = -1;
		assertArrayEquals(testArray, ByteArraysUtils.trim(testArray, fromIndex, length));
	}
	
	@Test
	public void testGetDelimiterIndices() {
		ArrayList<Integer> expectedIndices = new ArrayList<Integer>();
		expectedIndices.add(0);
		expectedIndices.add(18);
		expectedIndices.add(33);
		
		String delimiter = "aa aa aa";
		String testStream = "aa aa aa 0e af 73 aa aa aa ef 03 aa aa aa";
		byte[] testArray = testStream.getBytes();
		ArrayList<Integer> indices = ByteArraysUtils.getDelimiterIndices(testArray, delimiter.getBytes());
		for (int i = 0; i < indices.size(); i++) {
			assertEquals(expectedIndices.get(i), indices.get(i));	
		}
	}
	
	@Test
	public void testEnlargeSize(){
		int initSize = 10;
		int enlargedSize = 20;
		byte[] testArray = new byte[initSize];
		testArray = ByteArraysUtils.enlarge(testArray, enlargedSize);
		assertEquals(enlargedSize, testArray.length);
	}
	
	@Test
	public void testMerge(){
		String sourceInput = "How much wood would a woodchuck chuck if a woodchuck could chuck wood?";
		String appendInput = "Ohhh shut up";
		String mergedInput = sourceInput + appendInput;
		
		byte[] source = sourceInput.getBytes();
		byte[] append = appendInput.getBytes();
		
		byte[] expectedValue = mergedInput.getBytes();
		
		byte[] merged = ByteArraysUtils.merge(source, append);
		
		assertArrayEquals(expectedValue, merged);
		
	}
}
