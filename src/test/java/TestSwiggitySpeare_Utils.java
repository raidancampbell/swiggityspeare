import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by aidan on 8/16/15.
 */
public class TestSwiggitySpeare_Utils {
    
    //this test suite is gonna be pretty sparse
    //it was designed for Travis CI, or some other remote JUnit testing package
    //so I'm ignoring all the tests that have dependencies
    
    @Test
    public void testTrimAnyNick(){
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: bob, GLENNAN"), "bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick("bob, GLENNAN"), "bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick(""), "");
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: "), "");
        Assert.assertEquals(Swiggityspeare_utils.trimNick(null), null);
    }
    
    @Test
    public void testTrimSpecificNick(){
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: bob, GLENNAN", "alice"), "bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: bob, GLENNAN", "bob"), "alice: bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: bob, GLENNAN", ""), "bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick("alice: bob, GLENNAN", ""), "bob, GLENNAN");
        Assert.assertEquals(Swiggityspeare_utils.trimNick(null, null), null);
    }
    
    @Test
    public void testIsNumber(){
        Assert.assertTrue(Swiggityspeare_utils.isNumber("0"));
        Assert.assertTrue(Swiggityspeare_utils.isNumber("10"));
        Assert.assertTrue(Swiggityspeare_utils.isNumber("31498120935782149085701209237140978432097814098730917584091"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber("1.0"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber("-1"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber("+-1.0.2"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber("bill"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber("1 0"));
        Assert.assertFalse(Swiggityspeare_utils.isNumber(""));
        Assert.assertFalse(Swiggityspeare_utils.isNumber(null));
        Assert.assertFalse(Swiggityspeare_utils.isNumber(" "));
    }
}
