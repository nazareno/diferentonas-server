import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import play.test.WithBrowser;

public class WithBrowserTest extends WithBrowser{

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
    	browser.goTo("/");
    	assertThat(browser.pageSource(), containsString("Olar"));
    }

}
