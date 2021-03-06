package com.bluedot.framework.simplespring.mvc;


import com.bluedot.framework.simplespring.mvc.processor.RequestProcessor;
import com.bluedot.framework.simplespring.mvc.render.ResultRender;
import com.bluedot.framework.simplespring.mvc.render.impl.DefaultResultRender;
import com.bluedot.framework.simplespring.mvc.render.impl.JsonResultRender;
import com.bluedot.framework.simplespring.util.LogUtil;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 1
 */
public class RequestProcessorChain {
    /**
     * 请求处理迭代器
     */
    private Iterator<RequestProcessor> requestProcessorIterator;
    /**
     * 请求request
     */
    private HttpServletRequest req;
    /**
     * 请求response
     */
    private HttpServletResponse resp;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 请求状态码
     */
    private int responseCode;
    /**
     * 请求结果渲染器
     */
    private ResultRender resultRender;
    /**
     * 日志
     */
    private final Logger log = LogUtil.getLogger();

    /**
     * 构造函数
     *
     * @param iterator 请求处理迭代器
     * @param req      req
     * @param resp     resp
     */
    public RequestProcessorChain(Iterator<RequestProcessor> iterator, HttpServletRequest req, HttpServletResponse resp) {
        this.requestProcessorIterator = iterator;
        this.req = req;
        this.resp = resp;
        this.requestMethod = req.getMethod();
        this.requestPath = req.getPathInfo();
        this.responseCode = HttpServletResponse.SC_OK;
    }

    /**
     * 以责任链的模式执行请求链
     */
    public void doRequestProcessorChain() {
        //通过迭代器遍历注册的请求助力器实现类列表
        try {
            while (requestProcessorIterator.hasNext()) {
                RequestProcessor requestProcessor = requestProcessorIterator.next();
                log.debug("this requestProcessor is {}", requestProcessor);
                //直到某个请求处理器执行后返回为 false 为止
                if (!requestProcessor.process(this)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.debug("执行出错" + e.getMessage());
            //执行报错 直接交给前端处理
            Map<String,Object> map = new HashMap<>();
            map.put("message",e.getMessage());
            map.put("code",404);
            setResultRender(new JsonResultRender(map));
        }


    }

    /**
     * 执行处理器
     */
    public void doRender() {
        //如果请求处理器实现类未选择合适的渲染器，则使用默认渲染器
        if (null == this.resultRender) {
            this.resultRender = new DefaultResultRender();
        }
        try {
            //调用渲染器的render方法对结果进行渲染
            log.debug("using {} to render view", this.resultRender.getClass().getSimpleName());
            this.resultRender.render(this);
        } catch (Exception e) {
            log.error("doRender error:", e);
        }
    }


    public Iterator<RequestProcessor> getRequestProcessorIterator() {
        return requestProcessorIterator;
    }

    public void setRequestProcessorIterator(Iterator<RequestProcessor> requestProcessorIterator) {
        this.requestProcessorIterator = requestProcessorIterator;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public ResultRender getResultRender() {
        return resultRender;
    }

    public void setResultRender(ResultRender resultRender) {
        this.resultRender = resultRender;
    }

    public Logger getLog() {
        return log;
    }

    @Override
    public String toString() {
        return "RequestProcessorChain{" +
                "requestProcessorIterator=" + requestProcessorIterator +
                ", req=" + req +
                ", resp=" + resp +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", responseCode=" + responseCode +
                ", resultRender=" + resultRender +
                '}';
    }


}
