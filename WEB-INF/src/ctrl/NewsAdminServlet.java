package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 제품안전뉴스 관리 — 원본 _admin/safetydb/legal_safety_{list,form,proc}.asp 정합
 * URL: /news_admin/?mode=list|form|proc   테이블: dpl_law_board (LS_* 원본미러)
 * 정보구분 BD_ETC_COLS_3(1일반안전/2상품위해), 중요도 BD_ETC_COLS_4(1관심/2주의/3경계/4심각), 출처 BD_ETC_COLS_2, 링크 BD_ETC_COLS_1
 */
public class NewsAdminServlet extends HttpServlet {
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

        String w = "WHERE BD_CODE=2";   // v5.3: 뉴스=BBS_LEGAL_SAFETY(2)
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("TITLE".equals(qKey)) w += " AND BD_TITLE LIKE N'%"+sw+"%'";
            else if ("CONT".equals(qKey))  w += " AND ISNULL(BD_CONTENTS,'') LIKE N'%"+sw+"%'";
            else w += " AND (BD_TITLE LIKE N'%"+sw+"%' OR ISNULL(BD_CONTENTS,'') LIKE N'%"+sw+"%')";
        }
        List<Map<String,Object>> list = sql("SELECT BD_IDX,BD_TITLE,ISNULL(BD_ETC_COLS_2,'') AS BD_ETC_COLS_2,"
            + "ISNULL(BD_ETC_COLS_3,'1') AS BD_ETC_COLS_3,ISNULL(BD_ETC_COLS_4,'1') AS BD_ETC_COLS_4,"
            + "CONVERT(NVARCHAR(10),BD_REG_DATE,120) AS BD_REG_DATE "
            + "FROM dpl_law_board "+w+" ORDER BY BD_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_board "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.getRequestDispatcher("/jsp/board/news_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("ls_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT BD_IDX,BD_TITLE,ISNULL(BD_CONTENTS,'') AS BD_CONTENTS,"
                + "ISNULL(BD_ETC_COLS_1,'') AS BD_ETC_COLS_1,ISNULL(BD_ETC_COLS_2,'') AS BD_ETC_COLS_2,"
                + "ISNULL(BD_ETC_COLS_3,1) AS BD_ETC_COLS_3,ISNULL(BD_ETC_COLS_4,1) AS BD_ETC_COLS_4,'Y' "
                + "FROM dpl_law_board WHERE BD_IDX="+idx);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("lsIdx",idx);
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"), ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/news_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("ls_idx"), 0);
        String title  = nvl(req.getParameter("ls_title"), "");
        String cont   = nvl(req.getParameter("ls_content"), "");
        String link   = nvl(req.getParameter("ls_cols_01"), "");
        String source = nvl(req.getParameter("ls_cols_02"), "");
        int c3        = toInt(req.getParameter("ls_cols_03"), 1);
        int c4        = toInt(req.getParameter("ls_cols_04"), 1);
        String isUse  = nvl(req.getParameter("ls_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && title.isEmpty()) rtnMsg = "제목을 입력하세요.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_law_board (BD_CODE,BD_TITLE,BD_CONTENTS,BD_ETC_COLS_1,BD_ETC_COLS_2,BD_ETC_COLS_3,BD_ETC_COLS_4,BD_HIT,BD_REG_DATE) "
                        + "VALUES (2,?,?,?,?,?,?,0,GETDATE())")) {
                        ps.setString(1,title); ps.setString(2,cont); ps.setString(3,link); ps.setString(4,source);
                        ps.setString(5,String.valueOf(c3)); ps.setString(6,String.valueOf(c4));
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_law_board SET BD_TITLE=?,BD_CONTENTS=?,BD_ETC_COLS_1=?,BD_ETC_COLS_2=?,BD_ETC_COLS_3=?,BD_ETC_COLS_4=?,BD_UPD_DATE=GETDATE() WHERE BD_IDX=?")) {
                        ps.setString(1,title); ps.setString(2,cont); ps.setString(3,link); ps.setString(4,source);
                        ps.setString(5,String.valueOf(c3)); ps.setString(6,String.valueOf(c4)); ps.setInt(7,idx);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dpl_law_board WHERE BD_IDX=?")) {
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
                url = req.getContextPath()+"/news_admin/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/news_admin/?mode=form&ls_idx="+idx+"&"+linkParams;
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
