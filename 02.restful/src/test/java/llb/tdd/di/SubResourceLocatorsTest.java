package llb.tdd.di;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author LiLuBing
 * @PackageName: llb.tdd.di
 * @Description:
 * @ClassName: SubResourceLocatorsTest
 * @date 2022-11-13 下午3:47
 * @ProjectName tdd
 * @Version V1.0
 */
public class SubResourceLocatorsTest {

    @Test
    public void should_match_path_with_uri() {
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());

        ResourceRouter.SubResourceLocator locator = locators.findSubResource("/hello").get();

        assertEquals("Messages.hello", locator.toString());
    }

    @Test
    public void should_return_empty_if_not_match_uri() {
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        assertTrue(locators.findSubResource("/missing").isEmpty());
    }

    @Path("messages")
    static class Messages {
        @Path("/hello")
        public Message hello() {
            return new Message();
        }
    }

    static class Message {
    }
}
