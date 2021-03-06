package com.bluedot.electrochemistry.service;

import com.bluedot.electrochemistry.dao.base.BaseDao;
import com.bluedot.electrochemistry.dao.base.BaseMapper;
import com.bluedot.electrochemistry.factory.MapperFactory;
import com.bluedot.electrochemistry.pojo.domain.File;
import com.bluedot.electrochemistry.pojo.domain.User;
import com.bluedot.electrochemistry.pojo.domain.UserRole;
import com.bluedot.electrochemistry.service.base.BaseService;
import com.bluedot.electrochemistry.service.callback.ServiceCallback;
import com.bluedot.framework.simplespring.core.annotation.Service;
import com.bluedot.framework.simplespring.inject.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jun
 * @version 1.0
 * @date 2021/9/11 19:18
 */
@Service
public class AdminService extends BaseService {

    @Autowired
    MapperFactory mapperFactory;
    /**
     * 查询管理员信息
     *
     */

    @Autowired
    BaseDao baseDao;

    /**
     * 查询管理员列表
     * @param map
     */
    private void queryAdmins(Map<String, Object> map) {
        try{
            BaseMapper mapper = mapperFactory.createMapper();
            List<User> adminlist = new ArrayList<>();

            Integer pageNum = Integer.parseInt((String) map.get("pageNum"));
            Integer pageSize = Integer.parseInt((String) map.get("pageSize"));
            //获取当前编号
            int pageStart = (pageNum-1)*pageSize;


            //System.out.println(map.get("query"));
            //query为""则查询所有用户，否则支持模糊查询
            long numbers = 0 ;
            String query = (String) map.get("query");
            if(query.equals("")){
                numbers = mapper.getAdminCount();
                adminlist = mapper.getAdmins(pageStart,pageSize);
            }else {
                numbers = mapper.getAdminCountByQuery("%"+query+"%","%"+query+"%");
                adminlist = mapper.getAdminsByQuery("%"+query+"%","%"+query+"%",pageStart,pageSize);
            }

            map.put("data",adminlist);
            map.put("numbers",numbers);

            map.put("logMessage","查询管理员");
            map.put("code",200);
            map.put("message","管理员列表加载成功");

        }catch (Exception e){
            map.put("code",500);
            map.put("message","管理员列表加载失败");
        }


    }

//    private void queryAdmins(Map<String , Object> map){
//        doSimpleQueryListTemplate(map, new ServiceCallback<User>() {
//            @Override
//            public List<User> doListExecutor(BaseMapper baseMapper, int pageStart, int pageSize) {
//                return baseMapper.getAdmins(pageStart,pageSize);
//            }
//
//            @Override
//            public List<User> doListExecutorByQueryCondition(BaseMapper baseMapper, int pageStart, int pageSize, String queryCondition, String queryValue) {
//                return baseMapper.getAdminByQueryCondition(queryCondition , queryValue , pageStart , pageSize);
//            }
//
//            @Override
//            public Long doCountExecutor(BaseMapper baseMapper) {
//                return baseMapper.getAdminCount();
//            }
//
//            @Override
//            public Long doCountExecutorByQueryCondition(BaseMapper baseMapper, String queryCondition, String queryValue) {
//                return baseMapper.getAdminCountByQueryCondition(queryCondition , queryValue);
//            }
//        });
//    }

    /**
     * 修改管理员的状态
     *
     * @param map
     */
    private void modifyAdminState(Map<String , Object> map){
        try{
            int update = baseDao.update(packagingUser(map));
            //System.out.println(update);
            map.put("data",update);

            map.put("logMessage","修改管理员状态");
            map.put("code",200);
            map.put("code","修改状态成功");
        }catch (Exception e){
            map.put("code",500);
            map.put("message","修改状态失败");
        }
    }


    /**
     * 添加管理员，同时为该管理员添加一个管理员角色
     *
     * @param map
     */
    private void addAdmin(Map<String , Object> map){

        try{

            System.out.println("11111111");

            User user = packagingUser(map);
            int addUser = baseDao.insert(user);

            //添加用户成功时，为该用户赋予管理员角色
            if(addUser == 1){
                int addUserRole = baseDao.insert(new UserRole(user.getUsername(),200));
                map.put("data",addUserRole+addUser);
            }

            map.put("logMessage","添加管理员");
            map.put("code",200);
            map.put("message","添加成功");
        }catch (Exception e){
            map.put("code",500);
            map.put("message","添加失败，当前用户（非管理员）已存在，请修改用户名");
        }

    }

    /**
     *
     * 删除该用户的管理员角色
     * @param map
     */
    private void deleteAdmin(Map<String , Object> map){
        try{
            //查询该用户的管理员角色的ID
            User user = packagingUser(map);
            BaseMapper mapper = mapperFactory.createMapper();
            UserRole userRole = mapper.getUserRoleId(user.getUsername(),200);
            System.out.println(userRole.getUserRoleId());

            //UserRole userRole = new UserRole(user.getUsername(),200);

            int deleteUserRole = baseDao.delete(userRole);

            map.put("data",deleteUserRole);

            map.put("logMessage","删除用户管理员角色");
            map.put("code",200);
            map.put("message","删除该用户管理员角色成功");
        }catch (Exception e){
            map.put("code",500);
            map.put("message","删除该用户管理员角色失败");
        }
    }


    /**
     * 查询要修改的用户
     * @param map
     */
    private void queryEditAdmin(Map<String, Object> map){
        try{
            BaseMapper mapper = mapperFactory.createMapper();
            User user = mapper.getQueryEditAdmin(Integer.parseInt((String) map.get("username")));

            map.put("data",user);
            map.put("code",200);
            map.put("message","加载当前用户成功");
        }catch (Exception e){
            map.put("code",500);
            map.put("message","加载当前用户失败");
        }
    }

    /**
     * 将请求数据中的信息封装成用户对象
     *
     * @param map
     * @return
     */
    private User packagingUser(Map<String , Object> map){
        Integer username = Integer.parseInt((String) map.get("username"));
        String password = (String) map.get("password");
        String nickname  = (String) map.get("nickname");
        Integer gender = map.get("gender") == null ? 0: Integer.parseInt((String) map.get("gender"));
        Integer age = map.get("age") == null ? 0:Integer.parseInt((String)  map.get("age"));
        String email = (String) map.get("email");

        String statusTmp = (String)map.get("status");
        //添加用户时statusTmp == null，给status赋初值
        Integer status = -1;
        if(statusTmp == null) status=1;
        else status = statusTmp.equals("true")?1:0;
        String portrait = (String) map.get("portrait");
        return new User(username,password,nickname,gender,age,email,null,status,portrait,null);

    }


    /**
     * 修改用户
     *
     * @param map
     */

    private void editAdmin(Map<String , Object> map){
        try{
            User user = packagingUser(map);
            int editAdmin = baseDao.update(user);
            map.put("data",editAdmin);
            map.put("logMessage","修改了一个管理员");
            map.put("code",200);
            map.put("message","修改成功");
        }catch (Exception e){
            map.put("code",500);
            map.put("message","修改失败");
            e.printStackTrace();
        }
    }

    /**
     * 封装用户角色中间表的信息
     *
     * @param map
     * @return
     */
    private UserRole packagingUserRole(Map<String , Object> map){
        Integer username = (Integer) map.get("username");
        Integer roleId = (Integer) map.get("roleId");
        return new UserRole(username,roleId);
    }
}
