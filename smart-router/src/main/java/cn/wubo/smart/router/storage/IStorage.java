package cn.wubo.smart.router.storage;

import java.util.List;
import java.util.Map;

public interface IStorage {
    void add(RouterInfo routerInfo);

    List<RouterInfo> getAll();

    List<Map<String, Object>> getAllStatic();

    List<RouterInfo> getByEndpoint(String endpoint);

    void reset();
}
