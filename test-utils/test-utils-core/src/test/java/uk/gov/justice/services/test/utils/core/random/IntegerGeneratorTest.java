package uk.gov.justice.services.test.utils.core.random;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntegerGeneratorTest {

    @Test
    public void shouldReturnIntegersInOnesNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTenThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredThousandsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensOfMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999999, 0, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsOfMillionsNegativeRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999999, 0, 1000);
    }
    
  
    @Test
    public void shouldReturnIntegersInOnesPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTenThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99999,  1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredThousandsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 9999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensOfMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 99999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsOfMillionsPositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(0, 999999999, 1000);
    }

    @Test
    public void shouldReturnIntegersInOnesNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9, 9, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99, 99, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999, 999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999, 9999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTenThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999, 99999,  1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredThousandsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999, 999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-9999999, 9999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensOfMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-99999999, 99999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsOfMillionsNegativePositiveRange() {
        shouldTestRangesUsingMinMaxGenerator(-999999999, 999999999, 1000);
    }

    @Test
    public void shouldReturnIntegersInOnesPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTenThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99999,  1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredThousandsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(9999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInTensOfMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(99999999, 1000);
    }
    
    @Test
    public void shouldReturnIntegersInHundredsOfMillionsPositiveRangeUsingMaxGenerator() {
        shouldTestRangesUsingMaxGenerator(999999999, 1000);
    }

    
    @Test
    public void shouldReturnIntegersBetweenMinimumInclusiveAndMaximumExcluded() {
        IntegerGenerator ig = new IntegerGenerator();
        for (int i = 0; i < 10000; i++) {
            int k = ig.next();
            assertTrue(k < Integer.MAX_VALUE);
            assertTrue(k >= Integer.MIN_VALUE);
        }
    }
       
    private void shouldTestRangesUsingMinMaxGenerator(final int min, final int max, final int loopCount) {
        IntegerGenerator ig = new IntegerGenerator(min, max);
        for (int i = 0; i < loopCount; i++) {
            int k = ig.next();
            assertTrue(format("Value cannot be greater than Max value %s , got Value: %s", max, k), k < max);
            assertTrue(format("Value cannot be smaller than Min value %s , got Value: %s", min, k), k >= min);
        }
    }
    
    private void shouldTestRangesUsingMaxGenerator(final int max, final int loopCount) {
        IntegerGenerator ig = new IntegerGenerator(0, max);
        for (int i = 0; i < loopCount; i++) {
            int k = ig.next();
            assertTrue(format("Value cannot be greater than Max value %s , got Value: %s", max, k), k < max);
            assertTrue(format("Value cannot be smaller than Min value %s , got Value: %s", 0, k), k >= 0);
        }
    }
}
