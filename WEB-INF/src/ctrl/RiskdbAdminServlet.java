package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 위해정보DB 관리 — 원본 _admin/safetydb/riskdb_{list,form,proc}.asp 정합
 * URL: /riskdb_admin/?mode=list|form|proc   테이블: dpl_riskdb (RD_*)
 * 결함구분 RD_TYPE(화학적/물리적/생물학적/전기적/환경적결함 — 데모 F4 실값), 등급 RD_LEVEL(1관심~4심각)
 */
public class RiskdbAdminServlet extends HttpServlet {
    private static final int PS = 10;

    private void route(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String mode = nvl(req.getParameter("mode"), "list");
            switch (mode) {
                case "form": doForm(req, resp); break;
                case "proc": doProc(req, resp); break;
                default:     doList(req, resp);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException { route(req, resp); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException { route(req, resp); }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        int offset = (page-1)*PS;

        String w = "WHERE 1=1";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("TITLE".equals(qKey))  w += " AND RD_TITLE LIKE N'%"+sw+"%'";
            else if ("FACTOR".equals(qKey)) w += " AND ISNULL(RD_FACTOR,'') LIKE N'%"+sw+"%'";
            else w += " AND (RD_TITLE LIKE N'%"+sw+"%' OR ISNULL(RD_FACTOR,'') LIKE N'%"+sw+"%')";
        }
        List<Map<String,Object>> list = sql("SELECT RD_IDX,RD_TITLE,ISNULL(RD_TYPE,'') AS RD_TYPE,"
            + "ISNULL(RD_FACTOR,'') AS RD_FACTOR,ISNULL(RD_LEVEL,1) AS RD_LEVEL,ISNULL(RD_SOURCE,'') AS RD_SOURCE,"
            + "RD_IS_USE,CONVERT(NVARCHAR(10),RD_REG_DATE,120) AS RD_REG_DATE "
            + "FROM dpl_riskdb "+w+" ORDER BY RD_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_riskdb "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.getRequestDispatcher("/jsp/board/riskdb_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("rd_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT RD_IDX,RD_TITLE,ISNULL(RD_TYPE,'') AS RD_TYPE,"
                + "ISNULL(RD_FACTOR,'') AS RD_FACTOR,ISNULL(RD_LEVEL,1) AS RD_LEVEL,ISNULL(RD_SOURCE,'') AS RD_SOURCE,"
                + "ISNULL(RD_LINK,'') AS RD_LINK,ISNULL(RD_CONTENT,'') AS RD_CONTENT,RD_IS_USE "
                + "FROM dpl_riskdb WHERE RD_IDX="+idx);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("rdIdx",idx);
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"), ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/riskdb_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("rd_idx"), 0);
        String title  = nvl(req.getParameter("rd_title"), "");
        String type   = nvl(req.getParameter("rd_type"), "");
        String factor = nvl(req.getParameter("rd_factor"), "");
        int level     = toInt(req.getParameter("rd_level"), 1);
        String source = nvl(req.getParameter("rd_source"), "");
        String link   = nvl(req.getParameter("rd_link"), "");
        String cont   = nvl(req.getParameter("rd_content"), "");
        String isUse  = nvl(req.getParameter("rd_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && title.isEmpty()) rtnMsg = "제목을 입력하세요.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_riskdb (RD_TITLE,RD_TYPE,RD_FACTOR,RD_LEVEL,RD_SOURCE,RD_LINK,RD_CONTENT,RD_CATE,RD_IS_USE,RD_REG_DATE) "
                        + "VALUES (?,?,?,?,?,?,?,0,?,GETDATE())")) {
                        ps.setString(1,title); ps.setString(2,type); ps.setString(3,factor); ps.setInt(4,level);
                        ps.setString(5,source); ps.setString(6,link); ps.setString(7,cont); ps.setString(8,isUse);
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_riskdb SET RD_TITLE=?,RD_TYPE=?,RD_FACTOR=?,RD_LEVEL=?,RD_SOURCE=?,RD_LINK=?,RD_CONTENT=?,RD_IS_USE=? WHERE RD_IDX=?")) {
                        ps.setString(1,title); ps.setString(2,type); ps.setString(3,factor); ps.setInt(4,level);
                        ps.setString(5,source); ps.setString(6,link); ps.setString(7,cont); ps.setString(8,isUse); ps.setInt(9,idx);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dpl_riskdb WHERE RD_IDX=?")) {
                        ps.setInt(1,idx); ps.executeUpdate();
                    }
                }
            }
        }

        String page  = nvl(req.getParameter("page"), "1");
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String linkParams = "page="+page
            + (qKey.isEmpty()  ? "" : "&qKey="+qKey)
            + (qWord.isEmpty() ? "" : "&qWord="+java.net.URLEncoder.encode(qWord,"UTF-8"));

        if (rtnMsg.isEmpty()) {
            String url;
            if ("ADD".equals(action) || "DEL".equals(action))
                url = req.getContextPath()+"/riskdb_admin/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/riskdb_admin/?mode=form&rd_idx="+idx+"&"+linkParams;
            resp.sendRedirect(url);
        } else {
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write("<script>alert('"+rtnMsg.replace("'","\\'")+"'); history.back();</script>");
        }
    }

    private List<Map<String,Object>> sql(String query) throws Exception {
        List<Map<String,Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData(); int n = meta.getColumnCount();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i=1;i<=n;i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                rows.add(row);
            }
        }
        return rows;
    }
    private int countSql(String query) {
        try (Connection conn=DBPool.getConnection(); Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery(query)) {
            if(rs.next()) return rs.getInt(1);
        } catch(Exception ignored){}
        return 0;
    }
    private String nvl(String v, String def) { return (v==null||v.isEmpty())?def:v; }
    private int toInt(String v, int def) { try { return Integer.parseInt(v); } catch(Exception e){ return def; } }
}
