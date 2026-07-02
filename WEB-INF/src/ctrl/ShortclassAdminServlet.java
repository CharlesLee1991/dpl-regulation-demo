package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 숏클래스 관리 — 원본 _admin/bbs/support_shortclass_{list,form,proc}.asp 정합
 * URL: /shortclass/?mode=list|form|proc   테이블: dpl_shortclass (SC_*)
 * 원본 폼: 제목/교육분야/내용/썸네일/동영상/노출 → SC_TITLE/SC_TYPE/SC_DESC/SC_THUMB_URL/SC_VIDEO_URL/SC_IS_USE (+규제법률 SC_LL_IDX)
 */
public class ShortclassAdminServlet extends HttpServlet {
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
            if      ("TITLE".equals(qKey)) w += " AND s.SC_TITLE LIKE N'%"+sw+"%'";
            else if ("CONT".equals(qKey))  w += " AND ISNULL(s.SC_DESC,'') LIKE N'%"+sw+"%'";
            else w += " AND (s.SC_TITLE LIKE N'%"+sw+"%' OR ISNULL(s.SC_DESC,'') LIKE N'%"+sw+"%')";
        }
        List<Map<String,Object>> list = sql("SELECT s.SC_IDX,s.SC_TITLE,ISNULL(s.SC_TYPE,'') AS SC_TYPE,"
            + "ISNULL(l.LL_TITLE,'') AS LL_TITLE,s.SC_IS_USE,CONVERT(NVARCHAR(10),s.SC_REG_DATE,120) AS SC_REG_DATE "
            + "FROM dpl_shortclass s LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=s.SC_LL_IDX "
            + w+" ORDER BY s.SC_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_shortclass s "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.getRequestDispatcher("/jsp/board/sc_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("sc_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT SC_IDX,SC_TITLE,ISNULL(SC_DESC,'') AS SC_DESC,"
                + "ISNULL(SC_TYPE,'') AS SC_TYPE,ISNULL(SC_LL_IDX,0) AS SC_LL_IDX,"
                + "ISNULL(SC_THUMB_URL,'') AS SC_THUMB_URL,ISNULL(SC_VIDEO_URL,'') AS SC_VIDEO_URL,SC_IS_USE "
                + "FROM dpl_shortclass WHERE SC_IDX="+idx);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("scIdx",idx);
        req.setAttribute("legalList", sql("SELECT LL_IDX,LL_TITLE FROM dpl_regulation_legal WHERE LL_IS_USE='Y' ORDER BY LL_SORT,LL_IDX"));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"), ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/sc_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("sc_idx"), 0);
        String title  = nvl(req.getParameter("sc_title"), "");
        String type   = nvl(req.getParameter("sc_type"), "법규");
        String desc   = nvl(req.getParameter("sc_desc"), "");
        int llIdx     = toInt(req.getParameter("sc_ll_idx"), 0);
        String thumb  = nvl(req.getParameter("sc_thumb_url"), "");
        String video  = nvl(req.getParameter("sc_video_url"), "");
        String isUse  = nvl(req.getParameter("sc_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && title.isEmpty()) rtnMsg = "제목을 입력하세요.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_shortclass (SC_TITLE,SC_DESC,SC_TYPE,SC_CATE,SC_LL_IDX,SC_THUMB_URL,SC_VIDEO_URL,SC_IS_USE,SC_REG_DATE) "
                        + "VALUES (?,?,?,0,?,?,?,?,GETDATE())")) {
                        ps.setString(1,title); ps.setString(2,desc); ps.setString(3,type);
                        ps.setInt(4,llIdx); ps.setString(5,thumb); ps.setString(6,video); ps.setString(7,isUse);
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_shortclass SET SC_TITLE=?,SC_DESC=?,SC_TYPE=?,SC_LL_IDX=?,SC_THUMB_URL=?,SC_VIDEO_URL=?,SC_IS_USE=? WHERE SC_IDX=?")) {
                        ps.setString(1,title); ps.setString(2,desc); ps.setString(3,type);
                        ps.setInt(4,llIdx); ps.setString(5,thumb); ps.setString(6,video); ps.setString(7,isUse);
                        ps.setInt(8,idx);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dpl_shortclass WHERE SC_IDX=?")) {
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
                url = req.getContextPath()+"/shortclass/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/shortclass/?mode=form&sc_idx="+idx+"&"+linkParams;
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
