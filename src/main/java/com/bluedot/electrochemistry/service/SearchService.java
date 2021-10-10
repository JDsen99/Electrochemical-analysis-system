package com.bluedot.electrochemistry.service;

import com.bluedot.electrochemistry.dao.BaseMapper;
import com.bluedot.electrochemistry.factory.MapperFactory;
import com.bluedot.electrochemistry.pojo.domain.User;
import com.bluedot.electrochemistry.service.base.BaseService;
import com.bluedot.framework.simplespring.core.BeanContainer;
import com.bluedot.framework.simplespring.core.annotation.Service;
import com.bluedot.framework.simplespring.inject.annotation.Autowired;
import com.bluedot.framework.simplespring.mvc.monitor.Data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author JDsen99
 * @description
 * @createDate 2021/8/25-18:32
 */
@Service
public class SearchService extends BaseService  {

    @Autowired
    MapperFactory mapperFactory;
    /**
     * 所有查询入口
     * @param map
     */
    private void search(Map map) {

    }

    /**
     * 所有列表入口
     * @param map
     */
    private void list(Map map) throws IOException {
        MapperFactory mapperFactory = (MapperFactory) BeanContainer.getInstance().getBean(MapperFactory.class);
        BaseMapper baseMapper = mapperFactory.createMapper();
        List<User> users = baseMapper.listUser();
        ((Data)map).put("list",users);
    }
    public MapperFactory getMapperFactory() {
        return mapperFactory;
    }
}
