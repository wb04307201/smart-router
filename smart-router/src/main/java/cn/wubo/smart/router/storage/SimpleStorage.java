package cn.wubo.smart.router.storage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleStorage implements IStorage {

    private List<RouterInfo> routerInfos = new ArrayList<>();
    private Long systemStartTime = System.currentTimeMillis();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH");

    @Override
    public void add(RouterInfo routerInfo) {
        if (routerInfos.size() > 1000) routerInfos.remove(0);
        routerInfos.add(routerInfo);
    }

    @Override
    public List<RouterInfo> getAll() {
        return routerInfos;
    }

    @Override
    public List<Map<String, Object>> getAllStatic() {
        return routerInfos.parallelStream()
                // 先收集所有不同的端点配置
                .map(info -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("method", info.getMethod());
                    stat.put("endpoint", info.getEndpoint());
                    stat.put("isRateLimit", info.getIsRateLimit());
                    stat.put("isProxy", info.getIsProxy());
                    return stat;
                })
                .distinct()
                // 对每个端点进行统计计算
                .peek(stat -> {
                    String method = (String) stat.get("method");
                    String endpoint = (String) stat.get("endpoint");

                    // 筛选出当前端点的所有请求
                    List<RouterInfo> endpointRequests = routerInfos.stream()
                            .filter(item -> item.getMethod().equals(method) && item.getEndpoint().equals(endpoint))
                            .toList();

                    // 统计总请求数
                    long requestCount = endpointRequests.size();
                    stat.put("requestCount", requestCount);

                    // 计算QPS (基于系统运行时间)
                    long systemRunTimeSeconds = Math.max(1, (System.currentTimeMillis() - systemStartTime) / 1000);
                    stat.put("qps", (double) requestCount / systemRunTimeSeconds);

                    // 统计被消费(允许访问)的请求数
                    long consumCount = endpointRequests.stream()
                            .filter(RouterInfo::getIsConsum)
                            .count();
                    stat.put("consumCount", consumCount);
                })
                .toList();
    }

    @Override
    public List<RouterInfo> getByMethodAnaEndpoint(String method, String endpoint) {
        return routerInfos.stream().filter(info -> info.getMethod().equals(method) && info.getEndpoint().equals(endpoint)).toList();
    }

    @Override
    public void reset() {
        routerInfos.clear();
        systemStartTime = System.currentTimeMillis();
    }
}
