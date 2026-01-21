package cn.wubo.smart.router;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void testPost() {
        String url = "http://localhost:" + port + "/test/post";
        TestController.TestPost testPost = new TestController.TestPost();
        testPost.setValue2("world");
        String result = restTemplate.postForObject(url, testPost, String.class);
        System.out.println(result);
        assert result.equals("Hello world!");
    }

    @Test
    void testGet() {
        String url = "http://localhost:" + port + "/test/get?flag=999";
        String result = restTemplate.getForObject(url, String.class);
        System.out.println(result);
        assert result.equals("get v1 999") || result.equals("get v2 999");
    }
}
