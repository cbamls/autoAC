package com.cbam.autoAC.haligongOJ;

import com.cbam.autoAC.common.DbUtil;
import com.cbam.autoAC.common.HttpUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * CopyRright (c)2014-2016 Haerbin Hearglobal Co.,Ltd
 * Project: idoc-parent
 * Comments:
 * Author:cbam
 * Create Date:2016/11/26
 * Modified By:
 * Modified Date:
 * Modified Reason:
 */

/**
 * "#include+<iostream>using+namespace+std;int+main()+{++++int+a,+b;++++while+(cin+>>+a+>>+b)+{++++++++cout+<<+a+++b+<<+endl;++++}++++return+0;}+"
 */
public class AcHaligong {

    private static final String SHARED_LIST_URL = "http://acm.hrbust.edu.cn/index.php?jumpUrl=&m=Status&a=showStatus&user_name=&judge_status=2&language=2&shared=1&problem_id=";
    private static final String VIEW_SHARED_CODE = "http://acm.hrbust.edu.cn/index.php?m=ShareCode&a=viewshare&bought=1&run_id=";
    private static final String LOGIN_URL = "http://acm.hrbust.edu.cn/index.php?m=User&a=login";
    private static final String userName = "cbam";
    private static final String password = "chenggong";
    private static final String COOKIES = "_ga=GA1.3.1882735288.1479881561; last_problem_vol=1; PHPSESSID=k54h8ift5u9621qmqc8t0o1t17";

    public void doChainPost(String problem_id, String code) {
        Map<String, String> params = new HashMap<>(2);
        params.put("jumpUrl", "");
        params.put("problem_id", problem_id);
        params.put("language", "2");
        params.put("source_code", code);
        String re = HttpUtils.getInstance()
                .doPost("http://acm.hrbust.edu.cn/index.php?m=ProblemSet&a=postCode", params)
                                //.doGet("http://acm.hrbust.edu.cn/index.php?m=ProblemSet&a=postCode")
                .getContent();
    }
    public String toLogin() {
        Map<String, String> params = new HashMap<>(2);
        params.put("user_name", userName);
        params.put("password", password);

        String re = HttpUtils.getInstance()
                .addHeader("POST", "/index.php?m=ProblemSet&a=postCode HTTP/1.1")
                .addHeader("Host", "acm.hrbust.edu.cn")
                .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Cookie", COOKIES)
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests", "1")
                //.addParameter("id", "1")
                .doPost(LOGIN_URL, params)
                //.doGet("http://acm.hrbust.edu.cn/index.php?m=ProblemSet&a=postCode")
                .getContent();
        return re;
    }
    public String getSharedListUrl(String targetUrl) {
        String rtn = HttpUtils.getInstance()
                .doGet(targetUrl)
                .getContent();
        return rtn;
    }

    public TreeMap<Integer, List> getShareMap(int problem_id) {
        //选出效率最高的AC分享
        TreeMap<Integer, List> map = new TreeMap<>();
        String target_shared_url = SHARED_LIST_URL + problem_id;
        String html = this.getSharedListUrl(target_shared_url);
        int loc = 0;
        int flag = 0;
        int minTime = 100000;
        //System.out.println(html);
        while(html.indexOf("this code\"><img src=\"Public/images/viewshare.png\"></a></td><td>", loc) != -1 ) {
            int loc_run_id = html.indexOf("this code\"><img src=\"Public/images/viewshare.png\"></a></td><td>", loc);
            int loc_time = html.indexOf("iew this code\">G++</a></td><td>", loc_run_id) + "iew this code\">G++</a></td><td>".length();
            int loc_ms = html.indexOf("ms", loc_time);
            int loc_td = html.indexOf("</td", loc_run_id + "this code\"><img src=\"Public/images/viewshare.png\"></a></td><td>".length());
            String run_time = html.substring(loc_time, loc_ms);
            int len = loc_run_id + "this code\"><img src=\"Public/images/viewshare.png\"></a></td><td>".length();
            String run_id = html.substring(len, loc_td);
            int loc_mem = html.indexOf("</td><td>", loc_ms);
            String mem = html.substring(loc_mem + 9, html.indexOf( "k\n" +
                    "\t\t\t\t</td><td",loc_mem + 9));
            int loc_autor = html.indexOf("alt=\"", loc_mem);
            String author = html.substring(loc_autor + 5, html.indexOf( "\">",loc_autor + 5));
            int loc_date = html.indexOf("</td></tr>", loc_autor);
            String date = html.substring(loc_date - 19, loc_date);
            loc = loc_ms;
            List list = new ArrayList<>();
            list.add(mem);
            list.add(author);
            list.add(run_id);
            list.add(date);
            if(Integer.parseInt(run_time) < minTime) {
                minTime = Math.min(Integer.parseInt(run_time), minTime);
                map.put(Integer.parseInt(run_time), list);
            } else if(Integer.parseInt(run_time) == minTime) {
                List pre_list = map.get(Integer.parseInt(run_time));
                if(Integer.parseInt(pre_list.get(0).toString()) > Integer.parseInt(mem)) {
                    map.put(Integer.parseInt(run_time), list);
                }
            }
        }
        return map;
    }

    public String getCode(int run_id) {
        String targetUrl = VIEW_SHARED_CODE + run_id;
        String rtn = null;
        String html = HttpUtils.getInstance()
                .doGet(targetUrl)
                .getContent();
       // System.out.println(html);
        int loc_start = html.indexOf("<pre class=\"prettyprint\">");
        int loc_end = html.indexOf("</pre></td></tr><tr><td class=\"showcode_");
        rtn = resolveCode(html.substring(loc_start + "<pre class=\"prettyprint\">".length(), loc_end));
        return rtn;
    }

    public String resolveCode(String code) {

       String rtn = code.replaceAll("&lt;", "<")
               .replaceAll("&gt;", ">")
               .replaceAll("&quot;", "\"")
               .replaceAll("&amp;", "&");
        return rtn;
    }

    public void persistent(int problem_id, String code, int run_id, int run_time, String mem, String author, String date) {
       String sql = "insert into autoAc(problem_id, run_id, run_time, memory, autor, create_time, ac_code, ac_time)  values(" + problem_id + "," + run_id + "," + run_time
               + ",\"" + mem + "k\",\"" + author + "\",now(),\"" + StringEscapeUtils.escapeJava(code) + "\", str_to_date('" + date +"','%Y-%m-%d %H:%i:%s'))";
        System.out.println(sql);

        new DbUtil().insert(sql);
    }
    public void init(HashMap map) {
        DbUtil dbUtil = new DbUtil();
        String sql = "select problem_id from autoAc";
        ResultSet rs = dbUtil.select(sql);
        try {
            while (rs.next()) {
                int problem_id = rs.getInt(1);
                //System.out.println(problem_id);
                map.put(problem_id, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1、打开分享列表，选择题号和分享
     * 2、获取分享列表页面的run_id 组装get Url 打开分享的代码
     * 3、在分享代码的页面 获取problem_id 及 shared code 组装POST提交代码
     * @param args
     */
    public static void main(String[] args) {
        int start = Integer.parseInt(args[0]);
        int end = Integer.parseInt(args[1]);
        System.out.println("start=> " + start + "end => " + end);
        AcHaligong autoAc = new AcHaligong();
        autoAc.toLogin();
        HashMap map_problem_id = new HashMap();
        autoAc.init(map_problem_id);
        for(int problem_id = start; problem_id <= end; problem_id++) {
            if(map_problem_id.containsKey(problem_id)) {
                continue;
            }
            TreeMap<Integer, List > treeMap = autoAc.getShareMap(problem_id);
           for(Map.Entry entry : treeMap.entrySet()) {
               List list = treeMap.get(entry.getKey());
               int run_id = Integer.parseInt(list.get(2).toString());
               int run_time = (int) entry.getKey();
               String mem = list.get(0).toString();
               String author = (String) list.get(1);
               String dealedCode = autoAc.getCode(run_id);
               String date = list.get(3).toString();
              // System.out.println(dealedCode);
               autoAc.persistent(problem_id, dealedCode, run_id, run_time, mem, author, date);
               autoAc.doChainPost(String.valueOf(problem_id), dealedCode);
               break;
           }
        }

    }
}
