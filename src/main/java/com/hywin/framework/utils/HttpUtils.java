package com.hywin.framework.utils;

import com.hywin.framework.constant.Constants;
import com.hywin.framework.pojo.ResponseEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private CloseableHttpClient httpClient;

    private RequestConfig requestConfig;

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }
    /**
     * 执行get请求,200返回响应内容
     *
     * @param url
     * @return
     * @throws IOException
     */
    public ResponseEntity<String> doGet(String url) {
        //创建httpClient对象
        CloseableHttpResponse response = null;
        HttpGet httpGet = new HttpGet(url);
        //设置请求参数
        httpGet.setConfig(requestConfig);

        return doHttpRequest(httpGet);
    }

    /**
     * 执行带有参数的get请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public ResponseEntity<String> doGet(String url, Map<String, String> paramMap) {
        String uriString = "";
        try {
            URIBuilder builder = new URIBuilder(url);
            for (String s : paramMap.keySet()) {
                builder.addParameter(s, paramMap.get(s));
            }
            uriString = builder.build().toString();
        } catch (URISyntaxException e) {
            return new ResponseEntity(Constants.SYS_ERROR_CODE06, Constants.SYS_ERROR_MSG06);
        }
        return doGet(uriString);
    }

    /**
     * 执行post请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public ResponseEntity<String> doPost(String url, Map<String, String> paramMap) {
        HttpPost httpPost = new HttpPost(url);
        //设置请求参数
        httpPost.setConfig(requestConfig);
        if (paramMap != null) {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            for (String s : paramMap.keySet()) {
                parameters.add(new BasicNameValuePair(s, paramMap.get(s)));
            }
            //构建一个form表单式的实体
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, Charset.forName("UTF-8"));
            //将请求实体放入到httpPost中
            httpPost.setEntity(formEntity);
        }
        return doHttpRequest(httpPost);
    }


    /**
     * 执行http请求
     *
     * @param request
     * @return
     * @throws IOException
     */
    public ResponseEntity<String> doHttpRequest(HttpUriRequest request) {
        //创建httpClient对象
        CloseableHttpResponse response = null;
        try {
            //执行请求
            response = httpClient.execute(request);
            //判断返回状态码是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                logger.error("http请求异常");
                return new ResponseEntity(Constants.SYS_ERROR_CODE06, Constants.SYS_ERROR_MSG06);
            }
            return new ResponseEntity<String>(EntityUtils.toString(response.getEntity(), "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            logger.error("报文编码异常[{}]", e.getMessage());
            return new ResponseEntity(Constants.SYS_ERROR_CODE01, Constants.SYS_ERROR_MSG01);
        } catch (IOException e) {
            logger.error("http读取异常[{}]", e.getMessage());
            return new ResponseEntity(Constants.SYS_ERROR_CODE03, Constants.SYS_ERROR_MSG03);
        } catch (Exception e) {
            logger.error("http请求异常[{}]", e.getMessage());
            return new ResponseEntity(Constants.SYS_ERROR_CODE06, Constants.SYS_ERROR_MSG06);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    return new ResponseEntity(Constants.SYS_ERROR_CODE03, Constants.SYS_ERROR_MSG03);
                }
            }
        }
    }

    /**
     * 执行post请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public ResponseEntity<String> doPost(String url) throws IOException {
        return doPost(url, null);
    }


    /**
     * 提交json数据
     *
     * @param url
     * @param json
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public ResponseEntity<String> doPostJson(String url, String json) {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(this.requestConfig);

        if (json != null) {
            // 构造一个请求实体
            StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(stringEntity);
        }
        return doHttpRequest(httpPost);
    }
}

