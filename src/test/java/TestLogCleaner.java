import junit.framework.Assert;
import org.junit.Test;
/**
 * Created by aidan on 8/15/15.
 */
public class TestLogCleaner {
    
    @Test
    public void testRemoveTimestamp(){
        String sampleOne = "20150517T00:17:44 billybo has left IRC (Quit: Leaving)";
        String expectedSampleOne = "billybo has left IRC (Quit: Leaving)";
        String sampleTwo = "20130317T20:56:52 billybo: alfred, ping";
        String expectedSampleTwo = "billybo: alfred, ping";
        
        String actualSampleOne = LogCleaner.removeTimestamp(sampleOne);
        String actualSampleTwo = LogCleaner.removeTimestamp(sampleTwo);

        Assert.assertEquals(actualSampleOne, expectedSampleOne);
        Assert.assertEquals(actualSampleTwo, expectedSampleTwo);
    }
    
    @Test
    public void testFluffen(){
        String validInput = "the quick\nbrown fox\njumps over\nthe lazy dog";
        String[] expectedOutput = {"the quick","brown fox","jumps over","the lazy dog"};
        
        String[] actualOutput = LogCleaner.fluffen(validInput);
        for(int i = 0; i < actualOutput.length; i++) {
            Assert.assertEquals(expectedOutput[i], actualOutput[i]);
        }
        
        String baseInput = "hi";
        expectedOutput = new String[]{"hi"};
        actualOutput = LogCleaner.fluffen(baseInput);
        Assert.assertEquals(expectedOutput[0], actualOutput[0]);
        Assert.assertEquals(1, actualOutput.length);
        
        String simpleAbuse = "";
        expectedOutput = new String[]{""};
        actualOutput = LogCleaner.fluffen(simpleAbuse);
        Assert.assertEquals(expectedOutput[0], actualOutput[0]);
        Assert.assertEquals(1, actualOutput.length);
        
        String meanAbuse = null;
        expectedOutput = new String[]{null};
        actualOutput = LogCleaner.fluffen(meanAbuse);
        Assert.assertEquals(expectedOutput[0], actualOutput[0]);
        Assert.assertEquals(1, actualOutput.length);
    }
    
    @Test
    public void testRemoveStatusMessages() {
        String input = "alice|away is now known as alice";
        String input2 = "alice: bob, GLENNAN";
        String input3 = "carl has joined (carl@spartan-9ADD1351.cwru.edu)";
        String expected = "";
        String actual = LogCleaner.removeStatusMessages(input);
        Assert.assertEquals(expected, actual);

        actual = LogCleaner.removeStatusMessages(input2);
        expected = "alice: bob, GLENNAN";
        Assert.assertEquals(expected, actual);

        actual = LogCleaner.removeStatusMessages(input3);
        expected = "";
        Assert.assertEquals(expected, actual);
    }

}
