package com.wh.system.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.system.encrypt.Encryption;

/**
 * Created by Administrator on 2015/1/
 */
public class HttpHelp extends Thread {

    public enum HttpResultState{
        hrsNone, hrsMessage, hrsNULL, hrsUnKnownError, hrsTimeout, hrsRequestError, hrsParseError, hrsNULLError}

    int HTTP_READTIMEOUT = 1000 * 60 * 3;
    int HTTP_CONNECTTIMEOUT = 1000 * 60 * 1;

    AtomicBoolean HTTP_MUSTRESULT = new AtomicBoolean(true);

    public interface IHttpResult {
        public void OnHttpResult(RequestInfo request, ExecuteResult result);
    }

    public static class RequestInfo{
        public IHttpResult hr = null;
        public boolean redo = false;
        public String pagename = null;
        public String params = null;

        public enum NotifyType{
            ntMain, ntThread
        }

        public enum RequestType{
            rtPost, rtGet,
        }

        public class Total{
            public Date dt = new Date();
            public long times = -1;
        }

        public Total total = new Total();
        public RequestType rt = RequestType.rtGet;
        public NotifyType nt = NotifyType.ntMain;

        public volatile int redocount = 0;

        public final String id = newSerialNo();

        public final AtomicBoolean valid = new AtomicBoolean(true);

        RequestInfo(String pagename, String paramString, RequestType rt, NotifyType nt, IHttpResult hr){
            HashMap<String, String> datas = new HashMap<String, String>();
            String[] tmps = paramString.split("=");
            if (tmps.length < 2){
                throw new InvalidParameterException(paramString);
            }
            datas.put(tmps[0], tmps[1]);
            init(pagename, datas, rt, nt, hr);
        }

        RequestInfo(String pagename, HashMap<String, String> params, RequestType rt, NotifyType nt, IHttpResult hr){
            init(pagename, params, rt, nt, hr);
        }

        void init(String pagename, HashMap<String, String> params, RequestType rt, NotifyType nt, IHttpResult hr){
            if (params != null && params.size() > 0) {
                this.params = EncodeParam(id, params);
                if (this.params == null)
                    throw new InvalidParameterException("invalid http request params!");
            }
            this.pagename = pagename;
            this.rt = rt;
            this.nt = nt;
            this.hr = hr;
        }

    }

    class RequestManager{
        HashMap<String, RequestInfo> executeRequests = new HashMap<String, RequestInfo>();
        List<RequestInfo> lst = new ArrayList<RequestInfo>();

        AtomicBoolean pause = new AtomicBoolean(false);

        void addTop(RequestInfo request) {
            synchronized (lst) {
                if (pause.get())
                    return;

                if (!request.valid.get()){
                    return;
                }

                lst.add(0, request);
                lst.notifyAll();
            }
        }

        void add(RequestInfo request) {
            synchronized (lst) {
                if (pause.get())
                    return;

                if (!request.valid.get()){
                    return;
                }

                lst.add(request);
                lst.notifyAll();
            }
        }

        void clear() {
            synchronized (lst){
                for (RequestInfo request: executeRequests.values()) {
                    request.valid.getAndSet(false);
                }
                executeRequests.clear();
                lst.clear();
            }
        }

        RequestInfo get() {
            synchronized (lst) {
                if (lst.size() == 0)
                    try {
                        lst.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }

                if (lst.size() > 0) {
                    if (pause.get())
                        return null;

                    RequestInfo request = lst.remove(0);
                    executeRequests.put(request.id, request);
                    return request;
                } else
                    return null;
            }
        }

        void removeExecuteRequest(String id){
            synchronized (lst){
                if (executeRequests.containsKey(id)){
                    RequestInfo request = executeRequests.remove(id);
                    request.valid.getAndSet(false);
                }
            }
        }

    }

    class PoolManager{
        ExecutorService pool;

        Object lockObject = new Object();

        int maxPools = 8;

        public PoolManager(){
            setTaskMaxCount(maxPools);
        }

        public void setTaskMaxCount(int max){
            synchronized (lockObject) {
                maxPools = max;
                if (pool != null)
                    pool.shutdown();
                pool = Executors.newFixedThreadPool(maxPools);
            }
        }

        public void reset(){
            synchronized (lockObject) {
                requestManager.clear();
                setTaskMaxCount(maxPools);
            }
        }

        public void execute(Runnable runnable){
            synchronized (lockObject){
                pool.execute(runnable);
            }
        }
    }

    PoolManager poolManager = new PoolManager();

    public ExecuteResult Execute(RequestInfo request) {
        ExecuteResult er = new ExecuteResult();
        try {
            URL url = new URL(request.pagename);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setConnectTimeout(HTTP_CONNECTTIMEOUT);
                urlConnection.setReadTimeout(HTTP_READTIMEOUT);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestMethod(request.rt == RequestInfo.RequestType.rtGet ? "GET" : "POST");
                urlConnection.setInstanceFollowRedirects(true);
                
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("withCredentials", true);
//                urlConnection.setRequestProperty("xhrFields", jsonObject.toString());
                urlConnection.setRequestProperty("requestCrypted", "false");
                urlConnection.setRequestProperty("responseCrypted", "false");
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Charset", "UTF-8");
                urlConnection.setRequestProperty("dataType", "json");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                urlConnection.connect();

                BufferedWriter out =
                        new BufferedWriter(new OutputStreamWriter(urlConnection
                                .getOutputStream(), "UTF8"));

                out.write(request.params);
                out.flush();
                out.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));

                er.httpstate = urlConnection.getResponseCode(); //鑾峰彇鍝嶅簲鐮�
                switch (er.httpstate) {
                    case 200: {
                        er.state = HttpResultState.hrsNULL;
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null){
                            sb.append(line);
                        }
                        er.data = sb.toString();

                        if (er.data == null || er.data.isEmpty()) {
                            if (HTTP_MUSTRESULT.get()){
                                er.state = HttpResultState.hrsNULLError;
                            }
                            return er;
                        }

                        er.state = HttpResultState.hrsMessage;
                        return er;
                    }
                    case 408: {
                        er.state = HttpResultState.hrsTimeout;
                        er.errMsg = urlConnection.getResponseMessage();
                        throw new TimeoutException();
                    }
                    default: {
                        er.state = HttpResultState.hrsUnKnownError;
                        er.errMsg = urlConnection.getResponseMessage();
                        throw new InvalidObjectException("result data no json!");
                    }
                }
            }finally {
                urlConnection.disconnect();
            }
        } catch (Throwable e) {
            er.exception = e;
            if (er.state == HttpResultState.hrsNone)
                er.state = HttpResultState.hrsRequestError;

        }
        return er;
    }

    class HttpRunnable implements Runnable {
        RequestInfo request = null;
        ExecuteResult result = null;
        public HttpRunnable(RequestInfo request){
            this.request = request;
        }

		@Override
        public void run() {
            request.total.dt = new Date();
            result = Execute(request);

            switch (result.state) {
            case hrsMessage: {
                try {
                    result.json = ParseJson(result.data);
                    request.total.times = new Date().getTime() - request.total.dt.getTime();
                } catch (Throwable throwable) {
                    result.state = HttpResultState.hrsParseError;
                    result.exception = throwable;
                }
                break;
            }
			case hrsNULL:
				break;
			case hrsNULLError:
				break;
			case hrsNone:
				break;
			case hrsParseError:
				break;
			case hrsRequestError:
				break;
			case hrsTimeout:
				break;
			case hrsUnKnownError:
				break;
			default:
				break;
            }

            if  (result.state != HttpResultState.hrsMessage) {
                if (request.redo) {
                    request.redocount++;
                    requestManager.addTop(request);
                    return;
                }
            }

            requestManager.removeExecuteRequest(request.id);
            switch (request.nt) {
                case ntMain:
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            request.hr.OnHttpResult(request, result);
                        }
                    });
                    break;
                case ntThread:
                    request.hr.OnHttpResult(request, result);
                    break;
            }
        }
    }

    @Override
    public void finalize() {
        this.interrupt();
        try {
            poolManager.reset();
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                RequestInfo request = requestManager.get();
                if (request == null)
                    continue;

                poolManager.execute(new HttpRunnable(request));
            }catch (Throwable e){
                if (e instanceof InterruptedException){
                    break;
                }
            }
        }
    }

    public HttpHelp() {
        super();
    }

    public static JSONObject ParseJson(String jsonstr) {
        try {
            JSONObject json = new JSONObject(jsonstr);
            return json;

        } catch (JSONException ex) {
            return null;
        }

    }

    public static AtomicLong SERIALNO = new AtomicLong(0);

    public static String newID(){
        String id = UUID.randomUUID().toString();
        id = MD5(id);
        return id.substring(0, 8);
    }

    public static String newSerialNo(){
        if (SERIALNO.get() == Long.MAX_VALUE){
            SERIALNO.set(0);
        }

        long index = SERIALNO.incrementAndGet();

        return newID() + String.valueOf(index);
    }

    public static final String SerialNoNodeName = "SerialNoNodeName";
    public static String EncodeParam(String id, HashMap<String, String> datas) {
        try {
            if (id == null){
                id = newSerialNo();
            }
            datas.put(SerialNoNodeName, id);
            List<String> keys = new ArrayList<String>(datas.keySet());
            String value = null;
            for (String key : keys) {
                String tmp = URLEncoder.encode(datas.get(key), "UTF8");
                tmp = key + "=" + tmp;
                if (value == null)
                    value = tmp;
                else
                    value += "&" + tmp;
            }
            return value;
        }catch (Exception e){
            return null;
        }
    }

    public static class ExecuteResult{
        public int httpstate = -1;
        public HttpResultState state = HttpResultState.hrsNone;
        public String data = null;
        public String errMsg = null;
        public JSONObject json = null;
        public Throwable exception = null;
    }

    RequestManager requestManager = new RequestManager();

    public boolean AsyncGet(String url, HashMap<String, String> datas, IHttpResult hr) {
        requestManager.add(new RequestInfo(url, datas, RequestInfo.RequestType.rtGet, RequestInfo.NotifyType.ntThread, hr));
        return true;
    }

    public boolean AsyncGet(String url, IHttpResult hr) {
        return AsyncGet(url, null, hr);
    }

    public boolean AsyncPost(String url, LinkedHashMap<String, String> datas, IHttpResult hr) {
        requestManager.add(new RequestInfo(url, datas, RequestInfo.RequestType.rtPost, RequestInfo.NotifyType.ntThread, hr));
        return true;
    }

    public boolean AsyncPost(String url, IHttpResult hr) {
        return AsyncPost(url, null, hr);
    }

    public boolean SyncGet(String url, HashMap<String, String> datas, IHttpResult hr) {
        requestManager.add(new RequestInfo(url, datas, RequestInfo.RequestType.rtGet, RequestInfo.NotifyType.ntMain, hr));
        return true;
    }

    public boolean SyncGet(String url, IHttpResult hr) {
        return SyncGet(url, null, hr);
    }

    public boolean SyncPost(String url, LinkedHashMap<String, String> datas, IHttpResult hr) {
        requestManager.add(new RequestInfo(url, datas, RequestInfo.RequestType.rtPost, RequestInfo.NotifyType.ntMain, hr));
        return true;
    }

    public boolean SyncPost(String url, IHttpResult hr) {
        return SyncPost(url, null, hr);
    }

    public static String MD5(String value) {
        String result = Encryption.MD5Util.MD5(value).toLowerCase();
        return result;
    }

    public static void setParams(int taskMax) {
        hh.poolManager.setTaskMaxCount(taskMax);
        if (!isstart) {
            isstart = true;
            hh.start();
        }
    }


    public static void HttpReset() {
        hh.poolManager.reset();
    }

    public static boolean HttpGet(String pagename, LinkedHashMap<String, String> datas, IHttpResult hr) {
        return hh.SyncGet(pagename, datas, hr);
    }

    public static boolean HttpGet(String url, IHttpResult hr) {
        return hh.SyncGet(url, hr);
    }

    public static LinkedHashMap<String, String> JsonToMap(JSONObject datas){
    	LinkedHashMap<String, String> params = new LinkedHashMap<>();
    	JSONArray keys = datas.names();
    	for (int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
    		params.put(key, JsonHelp.getString(datas, key));
		}
    	return params;
    }
    
    public static boolean HttpPost(String pagename, LinkedHashMap<String, String> datas, IHttpResult hr) {
        return hh.SyncPost(pagename, datas, hr);
    }

    public static boolean HttpPost(String url, IHttpResult hr) {
        return hh.SyncPost(url, hr);
    }

    public static JSONObject HttpGet(final String pagename, final LinkedHashMap<String, String> datas) {
        SyncTools.ISyncValue syncValue = new SyncTools.SyncCallbackValue(new SyncTools.ISyncValue.IGetValueAndWait() {
            @Override
            public void getValue(final SyncTools.ISyncValue value) {
                boolean b = hh.AsyncGet(pagename, datas, new IHttpResult() {
                    @Override
                    public void OnHttpResult(RequestInfo request, ExecuteResult result) {
                        if (result.state == HttpResultState.hrsMessage)
                            value.setValue(result.json);
                        else
                            value.setValue(null);
                    }
                });
                if (!b)
                    value.setValue(null);
            }
        });
        return (JSONObject)syncValue.getValue();
    }

    public static JSONObject HttpGet(final String url) {
        SyncTools.ISyncValue syncValue = new SyncTools.SyncCallbackValue(new SyncTools.ISyncValue.IGetValueAndWait() {
            @Override
            public void getValue(final SyncTools.ISyncValue value) {
                boolean b = hh.AsyncGet(url, new IHttpResult() {
                    @Override
                    public void OnHttpResult(RequestInfo request, ExecuteResult result) {
                        if (result.state == HttpResultState.hrsMessage)
                            value.setValue(result.json);
                        else
                            value.setValue(null);
                    }
                });
                if (!b)
                    value.setValue(null);
            }
        });
        return (JSONObject)syncValue.getValue();
    }

    public static JSONObject HttpPost(final String pagename, final LinkedHashMap<String, String> datas) {
        SyncTools.ISyncValue syncValue = new SyncTools.SyncCallbackValue(new SyncTools.ISyncValue.IGetValueAndWait() {
            @Override
            public void getValue(final SyncTools.ISyncValue value) {
                boolean b = hh.AsyncPost(pagename, datas, new IHttpResult() {
                    @Override
                    public void OnHttpResult(RequestInfo request, ExecuteResult result) {
                        if (result.state == HttpResultState.hrsMessage)
                            value.setValue(result.json);
                        else
                            value.setValue(null);
                    }
                });
                if (!b)
                    value.setValue(null);
            }
        });
        return (JSONObject)syncValue.getValue();
    }

    public static JSONObject HttpPost(final String url) {
        SyncTools.ISyncValue syncValue = new SyncTools.SyncCallbackValue(new SyncTools.ISyncValue.IGetValueAndWait() {
            @Override
            public void getValue(final SyncTools.ISyncValue value) {
                boolean b = hh.AsyncPost(url, new IHttpResult() {
                    @Override
                    public void OnHttpResult(RequestInfo request, ExecuteResult result) {
                        if (result.state == HttpResultState.hrsMessage)
                            value.setValue(result.json);
                        else
                            value.setValue(null);
                    }
                });
                if (!b)
                    value.setValue(null);
            }
        });
        return (JSONObject)syncValue.getValue();
    }

    static HttpHelp hh = null;
    static boolean isstart = false;

    static {
        hh = new HttpHelp();
        setParams(5);
    }

}
