package cn.wubo.smart.router;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static java.lang.Thread.sleep;

@Slf4j
@RequestMapping("test")
@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return String.format("%s say:'hello world!'", name);
    }


    @PostMapping("/v1/post")
    public String v1post(@RequestBody TestPost testPost) {
        return String.format("%s %s!", testPost.getValue1(), testPost.getValue2());
    }

    @Data
    public static class TestPost {
        private String value1;
        private String value2;
    }

    @GetMapping("/v1/get")
    public String v1get(@RequestParam("flagv1") String flagv1) {
        return "get v1 " + flagv1;
    }

    @GetMapping("/v2/get")
    public String v2get(@RequestParam("flagv2") String flagv2) {
        return "get v2 " + flagv2;
    }
}
