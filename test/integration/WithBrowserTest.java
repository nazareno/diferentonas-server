package integration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import play.test.WithBrowser;

/**
 * 
 */
@Ignore
public class WithBrowserTest extends WithBrowser{

    @Test
    public void test() {
    	browser.goTo("/api");
    	assertThat(browser.pageSource(), containsString("Olar"));
    }

    @Test
    public void testCidadeInexistente() {
    	browser.goTo("/api/cidade/0");
    	assertThat(browser.pageSource(), containsString("Not found 0"));
    }

}
