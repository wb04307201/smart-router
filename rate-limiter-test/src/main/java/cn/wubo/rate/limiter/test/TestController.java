package cn.wubo.rate.limiter.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return String.format("%S say:'hello world!'", name);
    }
}
