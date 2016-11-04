package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static org.junit.Assert.*;

import org.junit.Test;

public class LongGeneratorTest {

    @Test
    public void shouldReturnLongsInOnesNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTenThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensOfMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsOfMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999999, 0, 1000);
    }
    
  
    @Test
    public void shouldReturnLongsInOnesPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTenThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99999,  1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensOfMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsOfMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999999999, 1000);
    }

    @Test
    public void shouldReturnLongsInOnesNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9, 9, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99, 99, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999, 999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999, 9999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTenThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999, 99999,  1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999, 999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999999, 9999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensOfMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999999, 99999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsOfMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999999, 999999999, 1000);
    }

    @Test
    public void shouldReturnLongsInOnesPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9999L, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTenThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99999,  1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInTensOfMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsInHundredsOfMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999999999, 1000);
    }
    
    @Test
    public void shouldReturnLongsBetweenMinimumInclusiveAndMaximumExcluded() {
        final LongGenerator ig = new LongGenerator();
        for (int i = 0; i < 10000; i++) {
            final long k = ig.next();
            assertTrue(k < Long.MAX_VALUE);
            assertTrue(k >= Long.MIN_VALUE);
        }
    }
       
    private void shouldTestRangesUsingMinMaxGenerator(final long min, final long max, final int loopCount) {
        final LongGenerator ig = new LongGenerator(min, max);
        for (int i = 0; i < loopCount; i++) {
            final long k = ig.next();
            assertTrue(format("Value cannot be greater than Max value %s , got Value: %s", max, k), k < max);
            assertTrue(format("Value cannot be smaller than Min value %s , got Value: %s", min, k), k >= min);
        }
    }
    
    private void shouldTestRangesUsingMaxGenerator(final long max, final int loopCount) {
        final LongGenerator ig = new LongGenerator(max);
        for (int i = 0; i < loopCount; i++) {
            final long k = ig.next();
            assertTrue(format("Value cannot be greater than Max value %s , got Value: %s", max, k), k < max);
            assertTrue(format("Value cannot be smaller than Min value %s , got Value: %s", 0, k), k >= 0);
        }
    }
}