import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by aidan on 8/16/15.
 */
public class TestShakespeareCleaner {
    
    @Test
    public void testAddNick(){
        String input = "hi";
        String expected = "Shakespeare: hi";
        String actual = ShakespeareCleaner.addNick(input);

        Assert.assertEquals(expected, actual);
        
        String simpleAbuse = "";
        expected = "";
        actual = ShakespeareCleaner.addNick(simpleAbuse);
        
        Assert.assertEquals(expected, actual);
        
        String meanAbuse = null;
        expected = null;
        actual = ShakespeareCleaner.addNick(meanAbuse);

        Assert.assertEquals(expected, actual);
    }
    
}
