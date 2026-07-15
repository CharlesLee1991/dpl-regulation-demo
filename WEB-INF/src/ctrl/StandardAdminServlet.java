package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 롯데 스탠다드 관리 — 원본 _admin/safetydb/standard_{list,form,proc}.asp 정합
 * URL: /standard_admin/?mode=list|form|proc   테이블: dpl_standard (ST_*)
 * 구분 LS_DIV(공통/품질기준/인스펙션), 기준서번호 LS_CODE, 최신개정일 LS_REV_DATE, 적용품목 LS_ITEMS
 */
public class StandardAdminServlet extends HttpServlet {
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
            if      ("CODE".equals(qKey))  w += " AND ISNULL(LS_CODE,'') LIKE N'%"+sw+"%'";
            else if ("TITLE".equals(qKey)) w += " AND LS_TITLE LIKE N'%"+sw+"%'";
            else if ("ITEMS".equals(qKey)) w += " AND ISNULL(LS_ITEMS,'') LIKE N'%"+sw+"%'";
            else w += " AND (LS_TITLE LIKE N'%"+sw+"%' OR ISNULL(LS_CODE,'') LIKE N'%"+sw+"%' OR ISNULL(LS_ITEMS,'') LIKE N'%"+sw+"%')";
        }
        List<Map<String,Object>> list = sql("SELECT LS_IDX,ISNULL(LS_DIV,'') AS LS_DIV,ISNULL(LS_CODE,'') AS LS_CODE,"
            + "LS_TITLE,ISNULL(LS_ITEMS,'') AS LS_ITEMS,ISNULL(LS_REV_DATE,'') AS LS_REV_DATE,"
            + "CONVERT(NVARCHAR(10),LS_REG_DATE,120) AS LS_REG_DATE "
            + "FROM dpl_standard "+w+" ORDER BY LS_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_standard "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.getRequestDispatcher("/jsp/board/standard_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("st_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT LS_IDX,ISNULL(LS_DIV,'') AS LS_DIV,ISNULL(LS_CODE,'') AS LS_CODE,"
                + "LS_TITLE,ISNULL(LS_ITEMS,'') AS LS_ITEMS,ISNULL(LS_REV_DATE,'') AS LS_REV_DATE,"
                + "FROM dpl_standard WHERE LS_IDX="+idx);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("stIdx",idx);
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"), ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/standard_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("st_idx"), 0);
        String div    = nvl(req.getParameter("st_div"), "공통");
        String code   = nvl(req.getParameter("st_code"), "");
        String title  = nvl(req.getParameter("st_title"), "");
        String items  = nvl(req.getParameter("st_items"), "");
        String ver    = nvl(req.getParameter("st_ver_date"), "");
        String cont   = nvl(req.getParameter("st_content"), "");
        String isUse  = nvl(req.getParameter("st_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && title.isEmpty()) rtnMsg = "기준서명을 입력하세요.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_standard (LS_DIV,LS_CODE,LS_TITLE,LS_ITEMS,LS_REV_DATE,LS_REG_DATE) "
                        + "VALUES (?,?,?,?,?,GETDATE())")) {
                        ps.setString(1,div); ps.setString(2,code); ps.setString(3,title); ps.setString(4,items);
                        ps.setString(5,ver);
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_standard SET LS_DIV=?,LS_CODE=?,LS_TITLE=?,LS_ITEMS=?,LS_REV_DATE=?,LS_UPD_DATE=GETDATE() WHERE LS_IDX=?")) {
                        ps.setString(1,div); ps.setString(2,code); ps.setString(3,title); ps.setString(4,items);
                        ps.setString(5,ver); ps.setInt(6,idx);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dpl_standard WHERE LS_IDX=?")) {
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
                url = req.getContextPath()+"/standard_admin/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/standard_admin/?mode=form&st_idx="+idx+"&"+linkParams;
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
