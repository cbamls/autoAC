package com.cbam.autoAC.HDOJ;

import com.cbam.autoAC.common.DbUtil;
import com.cbam.autoAC.common.HttpUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CopyRright (c)2014-2016 Haerbin Hearglobal Co.,Ltd
 * Project: idoc-parent
 * Comments:
 * Author:cbam
 * Create Date:2016/11/28
 * Modified By:
 * Modified Date:
 * Modified Reason:
 */


public class ACHDOJ {
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ACHDOJ.class);
    private static String session = "";
    private static String userName = "";
    private static String  password = "";
    public List getBlogUrlBaidu(String url) {
        List list = new ArrayList<>();
        String rtn = HttpUtils.getInstance()
                .addHeader("Host", "www.baidu.com")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0")
                // .addHeader("Cookie", "BAIDUID=B9B48C6259486551C447660F9C1A7847:FG=1; BIDUPSID=74116AD565D059F5E1C73C534C55FF86; PSTM=1479806655; ispeed_lsm=0; BD_UPN=13314552; ispeed=1; H_PS_PSSID=1443_21655_21127_18133_21455_21409_21554_20929; H_PS_645EC=0faaO%2FNNCER4e9Oeee8zxGbI4eFJMmcUxUHjd%2BJPWWa5FWR5ap5gERlv%2BXY; BD_CK_SAM=1; PSINO=2")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .doGet(url)
                .getContent();
        Pattern p = Pattern.compile("href=\"(http://blog.csdn[^\\s\"]+)\"");
        Matcher m = p.matcher(rtn);
        while(m.find()) {
            System.out.println(m.group(1));
        }
        return list;
    }
    public boolean toLogin() {
        Map<String, String> params = new HashMap<>(2);
        params.put("username", userName);
        params.put("userpass", password);
        params.put("login", "Sign+In");
        HttpUtils.ResponseResult re = HttpUtils.getInstance()
                .doPost("http://acm.hdu.edu.cn/userloginex.php?action=login", params);
        String content = re.getContent();
        session =  re.getCookies().substring(re.getCookies().indexOf("PHPSESSID"));
        return content.length() > 2 ? false : true;
    }
    public String checkSubmit(String name, int problem_id, String flag_run_id, boolean flag, String userCode, String blog_url, String blog_html) {
        HttpUtils.ResponseResult rs = HttpUtils.getInstance()
                .addHeader("Host", "acm.hdu.edu.cn")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0")
                .addHeader("Cookie", session)
                //.addHeader("Cookie", "CNZZDATA1254072405=652775645-1479901221-%7C1480340391; PHPSESSID=639helutvnogr2suru6amds807; exesubmitlang=0")
                .addHeader("Connection", "keep-alive")
                .doPost("http://acm.hdu.edu.cn/status.php");
        //.doGet("http://acm.hrbust.edu.cn/index.php?m=ProblemSet&a=postCode")

        String html = rs.getContent();
        try {
            HttpUtils.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int td;

       while((td = html.indexOf("<a href=\"/showproblem.php?pid=" + problem_id)) == -1) {
           LOGGER.error("无限");
          return "ing," + flag_run_id;
       }
        int td_start = td;
        while(html.indexOf("/viewcode.php?rid=", td_start) == -1) {
            return "ing," + flag_run_id;
        }
        int loc_run_id = html.indexOf("/viewcode.php?rid=", td_start) + "/viewcode.php?rid=".length();
        String cur_run_id = html.substring(loc_run_id, loc_run_id + 8);
        System.out.println("cur_run_id => " + cur_run_id);
       // if(list_run_id.contains(cur_run_id))
        System.out.println("falg_run_id => " + flag_run_id + " cur_run_id => " + cur_run_id);
        while(flag_run_id.equals(cur_run_id) && flag) {
            LOGGER.error("无限循环了");
            return "ing," + cur_run_id;
        }
        int tx = html.indexOf("</font></td><td><a href=\"/showproblem.php?pid=" + problem_id, td - 100);
        int ty = html.indexOf("</font></a></td><td><a href=\"/showproblem.php?pid=" + problem_id, td - 100);
        int loc_right;
        if(tx != -1 && ty != -1) {
            loc_right = Math.min(tx, ty);
        } else if(tx != -1 || ty != -1){
            loc_right = Math.max(tx, ty);
        } else {
            LOGGER.error("能出现 => " + tx  + "  " + ty);
            return "ing";
        }

        int loc_color = html.indexOf("color", loc_right - 50);
        int loc_left = html.indexOf(">", loc_color);
        String judge_result = html.substring(loc_left + 1, loc_right);


        int loc_run_time = html.indexOf("</a></td><td>", td) + "</a></td><td>".length();
        int loc_MS = html.indexOf("MS", loc_run_time);
        int run_time = Integer.parseInt(html.substring(loc_run_time, loc_MS));
        LOGGER.debug("run_time => " + run_time);
        int loc_memory = html.indexOf("K", loc_run_time);
        int memory = 0;
        try {
           memory = Integer.parseInt(html.substring(loc_MS + 11, loc_memory));
       } catch (Exception x) {
           x.printStackTrace();
       }
        LOGGER.debug("memory=> " + memory);

        String re2 = judge_result;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = sdf.format(new Date());
        try {
            if(!judge_result.equals("Accepted")) {
                this.persistent(problem_id, userCode, Integer.parseInt(cur_run_id), run_time, String.valueOf(memory), userName, nowDate, judge_result, blog_url, "" );

            } else {
                this.persistent(problem_id, userCode, Integer.parseInt(cur_run_id), run_time, String.valueOf(memory), userName, nowDate, judge_result, blog_url, blog_html );

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        LOGGER.debug("judge_result => " + judge_result);
        System.out.println("c =>"+re2);
        if(re2.substring(re2.length() - 3).equals("ing")) {
            return "ing," + cur_run_id;
        } else if(re2.equals("Accepted")) {
            return "Accepted," + cur_run_id;
        } else {
            return "complete," + cur_run_id;
        }
    }
    public String submitCode(String userCode, int problem_id, String  flag_run_id, boolean flag2, String blog_url, String blog_html) {
        Map<String, String> params = new HashMap<>(2);
        params.put("problemid", String.valueOf(problem_id));
        params.put("language", "0");
        params.put("usercode", userCode);
        HttpUtils.ResponseResult rs = HttpUtils.getInstance()
                .addHeader("Host", "acm.hdu.edu.cn")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,****/*//*//**//*//**//**//**//*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Cookie", session)
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0")
                .addHeader("Connection", "keep-alive")
                .doPost("http://acm.hdu.edu.cn/submit.php?action=submit", params);
                //.doGet("http://acm.hrbust.edu.cn/index.php?m=ProblemSet&a=postCode")

        String rtn = rs.getContent();
        String flag = "ing";
        String [] arr = null;
        int tot = 0;
        while(flag.equals("ing")) {
            if(tot++ == 20) {
                return "b" + arr[1];
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String t = checkSubmit("cbam", problem_id, flag_run_id, flag2, userCode, blog_url, blog_html);
            arr = t.split(",");
            flag = arr[0];
        }
        System.out.println("falg => " + flag);
        if(flag.equals("Accepted")) {
            return "a" + arr[1];
        }
        return "b" + arr[1];
    }

    public List getBlogUrlBiying(String url) {
        List list = new ArrayList<>();
        HttpUtils.ResponseResult rs = HttpUtils.getInstance()
                .addHeader("Host", "cn.bing.com")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0")
                //.addHeader("Cookie", "MUID=1C5A4304492967353B4C4ADC4D2964C5; SRCHD=AF=NOFORM; SRCHUID=V=2&GUID=A80CCA430001400A8459A69423BFF3EC; SRCHUSR=DOB=20161129; _SS=SID=0AA5546D02076A360E5E5DB103A66B96&HV=1480387684; _EDGE_S=mkt=zh-cn&SID=0AA5546D02076A360E5E5DB103A66B96; MUIDB=1C5A4304492967353B4C4ADC4D2964C5; SRCHHPGUSR=CW=1349&CH=146&DPR=1&UTC=480; WLS=TS=63615984486; _FP=hta=on")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .doGet(url);
        String rtn = rs.getContent();
      /*  try {
            HttpUtils.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       // System.out.println(rtn);
        Pattern p = Pattern.compile("href=\"(http://blog.csdn[^\\s\"]+)\"");
        Matcher m = p.matcher(rtn);
        while(m.find()) {
           list.add(m.group(1));
        }
        return list;

    }
    public String getCodeHtml(String url) {

        //http://blog.csdn.net/creativewang/article/details/7588533
        //"http://blog.csdn.net/.*/article/details/.*"
        List list = new ArrayList<>();
        try {
            HttpUtils.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpUtils.ResponseResult  rs = HttpUtils.getInstance()
                .addHeader("Host", "blog.csdn.net")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0")
                .addHeader("Accept-Encoding","gzip, deflate")
                .addHeader("Cookie", "uuid_tt_dd=1746089302942500699_20161122; Hm_lvt_6bcd52f51e9b3dce32bec4a3997715ac=1480386893,1480387577,1480388253,1480388647; bdshare_firstime=1479807144554; __message_sys_msg_id=0; __message_gu_msg_id=0; __message_cnel_msg_id=0; __message_in_school=0; _ga=GA1.2.2133540753.1479812381; UN=lsgqjh; UE=\"756029571@qq.com\"; BT=1480245865041; __utma=17226283.2133540753.1479812381.1479879714.1479879714.1; __utmz=17226283.1479879714.1.1.utmcsr=tuicool.com|utmccn=(referral)|utmcmd=referral|utmcct=/articles/rEfEBn; __message_district_code=230000; uuid=1cad9e63-f6a7-442b-9489-a12a5e55224a; Hm_lpvt_6bcd52f51e9b3dce32bec4a3997715ac=1480388647; avh=51232558%2c51232260%2c40891791%2c4757021%2c7884836; dc_tos=ohdv6w; dc_session_id=1480388648017")
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests","1")
                .addHeader("If-None-Match","W/\"376d82c4c2a4d366cf51a803eb4510ba\"")
                .addHeader("Cache-Control","max-age=0")
                .doGet(url);
        String rtn = rs.getContent();
        try {
            HttpUtils.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // System.out.println(rtn);
        return rtn;
    }
    public String getCode(String html, int problem_id) {
        String str = "";
        String java = StringEscapeUtils.escapeJava(html);
        if(html == null) {
            return "";
        }
        String newHtml = java.replaceAll("\n", "");
        Pattern p = Pattern.compile("<title>(.*)</title>");
        Matcher m = p.matcher(newHtml);
        while(m.find()) {
            LOGGER.debug(m.group(1));
            if(m.group(1).indexOf(String.valueOf(problem_id)) == -1) {
                return "";
            }
        }
        int loc_start1 = html.indexOf("class=\"cpp\">") + "class=\"cpp\">".length();
        int loc_start2 = html.indexOf("#include");
        if(loc_start2 != -1 && loc_start1 != -1) {
            if(html.substring(loc_start2 - 1, loc_start2).equals("/")) {
                loc_start2 = html.indexOf("#include", loc_start2 + 1);
            }
        }
        int loc_start = Math.max(loc_start1, loc_start2);
        int tx = html.indexOf("</textarea>", loc_start);
        int ty = html.indexOf("</pre>", loc_start);
        int loc_end = 0;
        if(tx != -1 && ty != -1)
            loc_end= Math.min(tx, ty);
         else if(tx == -1 && ty != -1) {
            loc_end = ty;
        } else if(tx != -1 && ty == -1) {
            loc_end = tx;
        } else {
            return str;
        }
        if(loc_start == -1 || loc_end == -1) {
            return str;
        }
        if(loc_end -loc_start <= 50) {
            return str;
        }
        str = html.substring(loc_start, loc_end);
        if(str.indexOf("</span>") != -1 || str.indexOf("<strong>") != -1) {
            return "";
        }
        return str;
    }
    public String resolve_code(String code) {
        String rtn = code.replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&amp;", "&")
                .replaceAll("&#39;", "\'")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&#43;", "+");
        return rtn;
    }
    public String encoded(String code) {
        try {
            byte[] tbyte = code.getBytes("utf-8");
            String newStr = new String(tbyte,"gb2312");
            return newStr;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String reslove_list_url(List list_url, int problem_id) {
        String result = "";
        String flag_run_id = "";
        boolean yy = true;
        for(Object each : list_url) {
            String url = (String) each;
            System.out.println("url => " + url);
            String csdn_html = getCodeHtml(url);
            String code = getCode(csdn_html, problem_id);

            if(code.equals("")) {
                continue;
            }
            String resolved_code = resolve_code(code);
            if(yy) {
                yy = false;
                String res = this.submitCode(resolved_code, problem_id, flag_run_id, false, url, csdn_html);
                if(String.valueOf(res.charAt(0)).equals("a")) {
                    break;
                } else {
                    flag_run_id = res.substring(1);
                }
            } else {
                String res = this.submitCode(resolved_code, problem_id, flag_run_id, true, url, csdn_html);
                if(String.valueOf(res.charAt(0)).equals("a")) {
                    break;
                } else {
                    flag_run_id = res.substring(1);
                }
            }

            LOGGER.debug("next-----------------------");
        }
        return result;
    }
    public Boolean init(HashMap map) {
        DbUtil dbUtil = new DbUtil();
        String sql = "select problem_id from autoac_copy";
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
        System.out.println("初始化成功！");
        return true;
    }
    public String toHexString(String s)
    {
        String str="";
        for (int i=0;i<s.length();i++)
        {
            int ch = (int)s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
    public void persistent(int problem_id, String code, int run_id, int run_time, String mem, String author, String date, String state, String blog_url, String blog_html) throws UnsupportedEncodingException {
        String sql = "insert into autoac_copy(problem_id, run_id, run_time, memory, author, create_time, ac_code, ac_time, acmer, password, state, blog_url, blog_html)  values(" + problem_id + "," + run_id + "," + run_time
                + ",\"" + mem + "k\",\"" + userName + "\",now(),'" + toHexString(code) + "', str_to_date('" + date +"','%Y-%m-%d %H:%i:%s'),'" + userName + "', '" + password + "', '"+ state +"','" + blog_url + "','" + toHexString(blog_html) + "')";
        new DbUtil().insert(sql);
      /*  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String nowDate = sdf.format(new Date());
        String sql = "insert into autoac_copy(problem_id, run_id, run_time, memory, author, " + nowDate + ", ac_code, ac_time, acmer, password, state, blog_url, blog_html) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps=null;
        Connection con = null;
        con = new DbUtil().getInitJDBCUtil().getConnection();
        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, problem_id);
            ps.setInt(2, run_id);
            ps.setInt(3, run_time);
            ps.setString(4, mem);
            ps.setString(5, userName);
            ps.setString(7, code);
            ps.setString(8, date);
            ps.setString(9, userName);
            ps.setString(10, password);
            ps.setString(11, state);
            ps.setString(12, blog_url);
            ps.setString(13, blog_html);
            ps.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        LOGGER.debug("已存库 ！");
    }
    public static void main(String[] args) {
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        userName = args[2];
        password = args[3];
        ACHDOJ achdoj = new ACHDOJ();

       // List list = achdoj.getBlogUrlBaidu("https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=%20hdu%201002%20csdn&oq=hdu%201002%20csdb&rsv_pq=c0e8521c00050b46&rsv_t=4ce9Z5VYa1xACxYuotgWhc%2BRFlwuN0sixZapzB%2BCg3I5tMT5Pl0ABpdboFM&rqlang=cn&rsv_enter=0&inputT=1767&rsv_sug3=43&rsv_sug4=2312");
       HashMap map = new HashMap();
        achdoj.init(map);
        if(!achdoj.toLogin()) {
            LOGGER.error("登录失败");
            return ;
        } else {
            LOGGER.debug("登录成功");
        }
        for(int start = k; start <= m; start++) {

            try {
                if(map.containsKey(start)) {
                    continue;
                }
                String targetUrl = "http://cn.bing.com/search?q=hdu+"+ start +"+csdn&qs=n&form=QBRE&pq=hdu+"+ start + "+csdn&sc=1-9&sp=-1&sk=&cvid=B6CA7CF0A69C4B67906F6A4627C23BBF";
                List list_url = achdoj.getBlogUrlBiying(targetUrl);
                if(list_url.size() >= 1) {
                    achdoj.reslove_list_url(list_url, start);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
   }
}
