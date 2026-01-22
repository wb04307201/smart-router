package cn.wubo.smart.router.http;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MutableHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, String[]> modifiedParams = new HashMap<>(); // 存储修改后的参数
    private byte[] modifiedBody; // 存储修改后的请求体字节
    private BufferedReader bodyReader; // 缓存请求体内容

    public MutableHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 1. 复制原始查询参数
        modifiedParams.putAll(request.getParameterMap());

        // 2. 读取并缓存原始请求体（仅POST/PUT等有Body的请求）
        if (isRequestBodySupported(request)) {
            InputStream inputStream = request.getInputStream();
            modifiedBody = StreamUtils.copyToByteArray(inputStream);
            bodyReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(modifiedBody), StandardCharsets.UTF_8));
        }
    }

    // 重写getParameterMap，优先返回修改后的参数
    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(modifiedParams);
    }

    @Override
    public String[] getParameterValues(String name) {
        return modifiedParams.get(name);
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(modifiedParams.keySet());
    }

    // 重写getInputStream，返回修改后的请求体
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (modifiedBody == null) {
            return super.getInputStream();
        }
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(modifiedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setReadListener(ReadListener readListener) {}
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (bodyReader == null) {
            return super.getReader();
        }
        return bodyReader;
    }

    // 修改查询参数/表单参数
    public void setParameter(String name, String... values) {
        modifiedParams.put(name, values);
    }

    // 修改JSON请求体
    public void setJsonBody(String json) {
        this.modifiedBody = json.getBytes(StandardCharsets.UTF_8);
        this.bodyReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(modifiedBody), StandardCharsets.UTF_8));
    }

    // 判断是否需要处理请求体
    private boolean isRequestBodySupported(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType() != null ?
                request.getContentType().toLowerCase() : "";
        return ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/x-www-form-urlencoded"));
    }
}
