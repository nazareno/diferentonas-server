package integration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import play.test.WithBrowser;

/**
 * 
 */
public class WithBrowserTest extends WithBrowser{

    @Test
    public void test() {
    	browser.goTo("/");
    	assertThat(browser.pageSource(), containsString("Olar"));
    }

    @Test
    public void testCidadeInexistente() {
    	browser.goTo("/cidade/0");
    	assertThat(browser.pageSource(), containsString("Not found 0"));
    }

}
