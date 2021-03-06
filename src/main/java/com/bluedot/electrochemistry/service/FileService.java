package com.bluedot.electrochemistry.service;

import com.bluedot.electrochemistry.dao.base.BaseDao;
import com.bluedot.electrochemistry.dao.base.BaseMapper;
import com.bluedot.electrochemistry.factory.MapperFactory;
import com.bluedot.electrochemistry.pojo.domain.File;
import com.bluedot.electrochemistry.service.base.BaseService;
import com.bluedot.electrochemistry.service.callback.ServiceCallback;
import com.bluedot.framework.simplespring.core.annotation.Service;
import com.bluedot.framework.simplespring.inject.annotation.Autowired;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description
 * @createDate 2021/8/25-14:37
 */
@Service
public class FileService extends BaseService {

    @Autowired
    MapperFactory mapperFactory;

    @Autowired
    BaseDao baseDao;

    /**
     * 导出文件
     *
     * @param map 数据
     */
    private void export(Map<String, Object> map) {
        int fileId = Integer.parseInt((String) map.get("fileId")) ;
        BaseMapper mapper = mapperFactory.createMapper();
        File file = mapper.getFileById(fileId);
        String url = file.getUrl();
        url = "D://WorkPlace_Code//IDEA_Code//Electrochemical-analysis-system//" + url;
        java.io.File f = new java.io.File(url);
        List<AV> dataList = new ArrayList<>();

        try {
            BufferedReader fis = new BufferedReader(new FileReader(f));
            String temp = null;
            boolean flag = false;
            while((temp = fis.readLine()) != null) {
                if("Potential/V, Current/A".equals(temp)){
                    temp = fis.readLine();
                    flag = true;
                    continue;
                }
                if (flag){
                    if (temp.contains(",")){
                        String[] split = temp.split(",");
                        AV av = new AV(split[0], Double.parseDouble(split[1]));
                        dataList.add(av);
                    }
                }
            }
            map.put("dataList",dataList);
            map.put("message", "执行成功");
            map.put("code", 200);
        } catch (IOException e) {
            map.put("message", e.getMessage());
            map.put("code", 500);
        }
    }

    /**
     * 查询文件
     *
     * @param map 数据集合
     */
    private void listFiles(Map<String, Object> map) {
        try {
            String search = (String) map.get("search");
            if ("1".equals(search)){
                searchFiles(map);
                return;
            }
            String str = (String) map.get("username");
            int username = Integer.parseInt(str);
            Integer pageStart = Integer.parseInt((String) map.get("page"));
            Integer pageSize = Integer.parseInt((String) map.get("limit"));
            short type = Short.parseShort((String) map.get("type"));
            short status = Short.parseShort((String) map.get("status"));

            BaseMapper mapper = mapperFactory.createMapper();
            List<File> files = null;
            Long size = null;
            if (type == 1) {
                files = mapper.listFiles(type, status, username, (pageStart - 1) * pageSize, pageSize);
                size = mapper.countFiles(type, status, username);
            }else {
                files = mapper.listFilesByAdmin0((short) 1,(pageStart - 1) * pageSize, pageSize);
                size = mapper.countFilesByAdmin0((short) 1);
            }
            map.put("data", files);
            map.put("code", 200);
            map.put("message", "文件列表加载完成");
            map.put("length", size);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", 500);
            map.put("message", "文件列表加载失败");
        }

    }

    /**
     * 查询文件
     *
     * @param map 数据集合
     */
    private void searchFiles(Map<String, Object> map) {
        try {
            BaseMapper mapper = mapperFactory.createMapper();

            String str = (String) map.get("username");
            int username = Integer.parseInt(str);
            Integer pageStart = Integer.parseInt((String) map.get("page"));
            Integer pageSize = Integer.parseInt((String) map.get("limit"));
            short type = Short.parseShort((String) map.get("type"));
            String title = (String) map.get("title");
            Integer status = Integer.parseInt((String) map.get("status"));
            List<File> files = null;
            Long size = null;
            if (type == 1) {
                files = mapper.searchFileByUser("%" + title + "%", status,username, (short) 1, (pageStart - 1) * pageSize, pageSize);
                size = mapper.countFilesByUser("%" + title + "%", status,username, (short) 1);
            } else {
                files = mapper.searchFileByAdmin("%" + title + "%","%" + title + "%", (short) 1, (pageStart - 1) * pageSize, pageSize);
                size = mapper.countFilesByAdmin("%" + title + "%","%" + title + "%", (short) 1);
            }

            map.put("data", files);
            map.put("length", size);
            map.put("code", 200);
            map.put("message", "文件列表加载完成");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", 500);
            map.put("message", "文件列表加载失败");
        }

    }

    /**
     * 根据文件编号去查询文件
     *
     * @param map 数据
     */
    private void findFile(Map<String, Object> map) {
        try {
            Integer fileId = (Integer) map.get("fileId");
            Integer pageStart = (Integer) map.get("pageStart");
            Integer pageSize = (Integer) map.get("pageSize");
            BaseMapper mapper = mapperFactory.createMapper();
//            File file = mapper.getFileById(fileId);
//            map.put("data",file);
        } catch (Exception e) {
            map.put("message", e.getMessage());
            map.put("code", 404);
        }
    }

    /**
     * 上传文件
     *
     * @param map 数据，其中包含必要参数：
     *              username：文件上传的用户账号
     *              dataCycle：文件数据实验的循环圈数
     */
    private void uploadFile(Map<String, Object> map) {

        java.io.File file = (java.io.File) map.get("file");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuffer str = new StringBuffer();
            String temp = "";
            int lineNum = 0, dataLine = 9999999;        //记录行号,数据行号
            String[] dataArray = null;  //存放临时数据的两项

            //x0,x1,灵敏度, y0,y1
            double dataStart = -999,dataEnd, dataPrecision = -999, dataBottom = 999, dataPeak = -999;
            while ((temp = reader.readLine()) != null) {
                //文件类型判断
                if(++lineNum == 2 && !"Differential Pulse Voltammetry".equals(temp)){
                    fileFormatError(map, file);
                    return;
                }
                str.append(temp).append("\n");
                //拿到循环圈数 !!! DPV没有循环圈数
                //当行号位于16行的时候，会出现灵敏度（精度）设置
                if(lineNum == 16){
                    dataArray = temp.split(" \\(A/V\\) = ");
                    if ("Sensitivity".equals(dataArray[0])) dataPrecision = Double.parseDouble(dataArray[1]);
                    else {
                        fileFormatError(map, file);
                        return;
                    }
                }
                //初始化数据行号
                if("Potential/V, Current/A".equals(temp)){
                    dataLine = lineNum+2;
                }
                //数据整理与统计
                if(lineNum >= dataLine){
                    dataArray = temp.split(", ");
                    if(lineNum == dataLine){//记录x轴的最小值
                        dataStart = Double.parseDouble(dataArray[0]);
                    }
                    //y0整理
                    dataBottom = Math.min(dataBottom, Double.parseDouble(dataArray[1]));
                    //y1整理
                    dataPeak = Math.max(dataBottom, Double.parseDouble(dataArray[1]));
                }
            }
            //x轴的最大值记录
            dataEnd = Double.parseDouble(dataArray[0]);
            //数据库写入操作
            File userFile = new File();
            userFile.setDataStart(dataStart);
            userFile.setDataEnd(dataEnd);
            userFile.setDataBottom(dataBottom);
            userFile.setDataPeak(dataPeak);
            userFile.setDataPrecision(dataPrecision);
            //将去除后缀名的文件名注入
            userFile.setName((file.getName()).split("[.]")[0]);
            userFile.setUrl((String) map.get("filePath"));
            userFile.setOwner(Integer.parseInt((String) map.get("username")));
            userFile.setSize(file.length()+"Byte");
            userFile.setType(1);
            userFile.setStatus(1);
            userFile.setProduceTime(new Timestamp(new Date().getTime()));
            doSimpleModifyTemplate(map, new ServiceCallback<Object>() {
                @Override
                public int doDataModifyExecutor(BaseDao baseDao) {
                    return baseDao.insert(userFile);
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            map.put("message", e.getMessage());
            map.put("code", 500);
        } finally {
            map.put("logMessage","上传文件");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    map.put("message", e.getMessage());
                    map.put("code", 500);
                }
            }
        }
    }

    //当文件格式错误时进行的操作
    private void fileFormatError(Map<String, Object> map, java.io.File file) {
        map.put("message", "文件格式错误");
        map.put("code", 500);
        file.delete();
    }

    /**
     * 删除文件，文件彻底删除
     *
     * @param map 数据
     */
    private void deleteFile(Map<String, Object> map) {
        doSimpleModifyTemplate(map, new ServiceCallback<Object>() {
            @Override
            public int doDataModifyExecutor(BaseDao baseDao) {
                Integer fileId = Integer.parseInt((String) map.get("fileId"));
                File file = new File();
                file.setId(fileId);
                map.put("logMessage", "删除文件");
                return baseDao.delete(file);
            }
        });

    }

    /**
     * 移除文件 文件进入回收站
     *
     * @param map 数据
     */
    private void remove(Map<String, Object> map) {
        doSimpleModifyTemplate(map, new ServiceCallback<Object>() {
            @Override
            public int doDataModifyExecutor(BaseDao baseDao) {
                Integer fileId = Integer.parseInt((String) map.get("fileId"));
                File file = new File();
                file.setId(fileId);
                file.setStatus(2);
                map.put("logMessage", "移除文件");
                return baseDao.update(file);
            }
        });

    }

    /**
     * 还原文件，文件从回收站还原
     *
     * @param map 数据
     */
    private void restore(Map<String, Object> map) {
        doSimpleModifyTemplate(map, new ServiceCallback<Object>() {
            @Override
            public int doDataModifyExecutor(BaseDao baseDao) {
                int fileId = Integer.parseInt((String) map.get("fileId"));
                File file = new File();
                file.setId(fileId);
                file.setStatus(1);
                map.put("logMessage","还原文件");
                return baseDao.update(file);
            }
        });
    }

    /**
     * 更新文件
     *
     * @param map 数据
     */
    private void updateFile(Map<String, Object> map) {
        doSimpleModifyTemplate(map, new ServiceCallback<Object>() {
            @Override
            public int doDataModifyExecutor(BaseDao baseDao) {
                int fileId = (int) map.get("fileId");
                String size = "1";
                Timestamp modified_time = (Timestamp) map.get("modified_time");
                String hash = null;
                double data_start = (double) map.get("data_start");
                double data_end = (double) map.get("data_end");
                double data_bottom = (double) map.get("data_bottom");
                double data_peak = (double) map.get("data_peak");
                double data_precision = (double) map.get("data_precision");
                double data_cycle = (double) map.get("data_cycle");
                double data_rate = (double) map.get("data_rate");
                double data_results = (double) map.get("data_results");
                File file = new File(fileId, size, hash, modified_time, data_start, data_end, data_bottom, data_peak, data_precision, data_cycle, data_rate, data_results);
                return baseDao.update(file);
            }
        });


    }

    /**
     * 比较文件hash值
     *
     * @param fileHash 文件hash值
     */
    private boolean contrast(String fileHash, int username) {
//        long l = mapperFactory.createMapper().contrastFile(fileHash, username);
        return 2 >= 1;
    }

    /**
     * 下载文件
     *
     * @param map 数据
     */
    private void loadingData(Map<String, Object> map) {
        int fileId = Integer.parseInt((String) map.get("fileId")) ;
        BaseMapper mapper = mapperFactory.createMapper();
        File file = mapper.getFileById(fileId);
        String url = file.getUrl();
        url = "D://WorkPlace_Code//IDEA_Code//Electrochemical-analysis-system//" + url;
        java.io.File f = new java.io.File(url);
        List<String> vList = new ArrayList<>();
        List<Double> aList = new ArrayList<>();

        try {
            BufferedReader fis = new BufferedReader(new FileReader(f));
            String temp = null;
            boolean flag = false;
            while((temp = fis.readLine()) != null) {
                if("Potential/V, Current/A".equals(temp)){
                    temp = fis.readLine();
                    flag = true;
                    continue;
                }
                if (flag){
                    if (temp.contains(",")){
                        String[] split = temp.split(",");
                        vList.add(split[0]);
                        aList.add(Double.parseDouble(split[1]));
                    }
                }
            }
            map.put("detailA",aList);
            map.put("detailV",vList);
            map.put("file",file);
        } catch (IOException e) {
            map.put("message", e.getMessage());
            map.put("code", 500);
        }

    }

    /**
     * 获取文件数据曲线的切点，并将切点信息以数组的形式封装成Map到返回队列中去
     *
     * @param map 内含加载了数据的实体类File
     * @author zero
     */
    private void getTangent(Map<String, Object> map) {

    }

    /**
     * 获取实验报告，并将生成的实验报告文件以流的形式返回到map队列中去
     *
     * @param map 内含file:File实验数据文件；url:String，echarts图像地址
     * @author zero
     */
    private void getReport(Map<String, Object> map) {

    }

    /**
     * 保存文件，并将添加成功与否的状态值放入到map队列中去
     *
     * @param map 内含参数：
     *            username：int，账号；
     *            type：int,文件类型：0CV,1PDV,2SWV,3LSV；
     *            fileName：String,文件名；
     *            xDataArr[]：double[],x轴数据组；
     *            yDataArr[]：double[]，y轴数据组
     * @author zero
     */
    private void saveFile(Map<String, Object> map) {

    }
    class AV{
        private String A;
        private Double V;

        public AV(String a, Double v) {
            A = a;
            V = v;
        }

        public String getA() {
            return A;
        }

        public void setA(String a) {
            A = a;
        }

        public Double getV() {
            return V;
        }

        public void setV(Double v) {
            V = v;
        }
    }

}
