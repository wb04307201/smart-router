package cn.wubo.smart.router;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HttpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void testHello() {
        String url = "http://localhost:" + port + "/test/hello?name=wb04307201";
        String result = restTemplate.getForObject(url, String.class);
        assert result.equals("wb04307201 say:'hello world!'");

        Map result1 = restTemplate.getForObject(url, Map.class);
        assert result1.get("status").equals(429);
    }

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
