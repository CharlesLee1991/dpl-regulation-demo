package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * 프론트엔드 사용자 화면  /front/*
 */
public class FrontServlet extends HttpServlet {

    private static final int LIST_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String uri = req.getRequestURI();
        try {
            if (uri.equals("/front/") || uri.equals("/front") || uri.startsWith("/front/index")) {
                doMain(req, resp);
            } else if (uri.contains("/front/legal/view")) {
                doLegalView(req, resp);
            } else if (uri.contains("/front/legal/")) {
                doLegalList(req, resp);
            } else {
                doMain(req, resp);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    private void doMain(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setAttribute("cntLegal",    count("dpl_regulation"));
        req.setAttribute("cntSafety",   count("dpl_safety"));
        req.setAttribute("cntStandard", count("dpl_items"));
        req.setAttribute("recentLegal", queryList("SELECT TOP 5 LR_IDX,LR_TITLE,ISNULL(LL_TITLE,'') AS LL_TITLE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IS_USE='Y' ORDER BY r.REG_DATE DESC"));
        req.setAttribute("legalNews",   queryList("SELECT TOP 5 LR_IDX,LR_TITLE,ISNULL(LL_TITLE,'') AS LL_TITLE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IS_USE='Y' ORDER BY r.LR_IDX DESC"));
        req.getRequestDispatcher("/jsp/front/front_main.jsp").forward(req, resp);
    }

    private void doLegalList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page     = toInt(req.getParameter("page"), 1);
        int qLL      = toInt(req.getParameter("qLL"), 0);
        int qLR      = toInt(req.getParameter("qLR"), 0);
        String qWord = nvl(req.getParameter("qWord"), "");
        int offset   = (page - 1) * LIST_SIZE;

        String where = "WHERE r.LR_IS_USE='Y'";
        if (qLL > 0)   where += " AND r.LL_IDX=" + qLL;
        if (qLR > 0)   where += " AND r.LR_IDX=" + qLR;
        if (!qWord.isEmpty()) where += " AND r.LR_TITLE LIKE N'%" + qWord.replace("'","''") + "%'";

        List<Map<String,Object>> list = queryList(
            "SELECT r.LR_IDX,r.LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(l.LL_DEPT,'') AS LL_DEPT,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX " + where + " ORDER BY r.LR_IDX DESC OFFSET " + offset + " ROWS FETCH NEXT " + LIST_SIZE + " ROWS ONLY");
        int total = countWhere("dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX", where.replace("WHERE",""));

        req.setAttribute("list",    list);
        req.setAttribute("total",   total);
        req.setAttribute("page",    page);
        req.setAttribute("pageCnt", total > 0 ? (int)Math.ceil((double)total/LIST_SIZE) : 1);
        req.setAttribute("qLL",     qLL);
        req.setAttribute("qLR",     qLR);
        req.setAttribute("qWord",   qWord);
        req.setAttribute("legalList",      queryList("SELECT LL_IDX,LL_TITLE FROM dpl_regulation_legal WHERE LL_IS_USE='Y' ORDER BY LL_SORT"));
        req.setAttribute("regulationList", qLL > 0 ? queryList("SELECT LR_IDX,LR_TITLE FROM dpl_regulation WHERE LR_IS_USE='Y' AND LL_IDX=" + qLL + " ORDER BY LR_TITLE") : new ArrayList<>());
        req.getRequestDispatcher("/jsp/front/front_legal_list.jsp").forward(req, resp);
    }

    private void doLegalView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int lrIdx = toInt(req.getParameter("lr_idx"), 0);
        Map<String,Object> info = new LinkedHashMap<>();
        List<Map<String,Object>> relatedNotify = new ArrayList<>();
        if (lrIdx > 0) {
            List<Map<String,Object>> rows = queryList(
                "SELECT r.LR_IDX,r.LR_TITLE,ISNULL(r.LR_CONDITION,'') AS LR_CONDITION,ISNULL(r.LR_CERTIFY_GUIDE,'') AS LR_CERTIFY_GUIDE,ISNULL(r.LR_PENALTY,'') AS LR_PENALTY,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(l.LL_DEPT,'') AS LL_DEPT,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IDX=" + lrIdx);
            if (!rows.isEmpty()) info = rows.get(0);
            relatedNotify = queryList("SELECT LN_IDX,LN_TITLE FROM dpl_notify WHERE LN_IS_USE='Y' AND LR_IDX=" + lrIdx + " ORDER BY LN_TITLE");
        }
        req.setAttribute("info",          info);
        req.setAttribute("relatedNotify", relatedNotify);
        req.getRequestDispatcher("/jsp/front/front_legal_view.jsp").forward(req, resp);
    }

    // ── DB 헬퍼 ─────────────────────────────────────────────────────
    private List<Map<String,Object>> queryList(String sql) throws Exception {
        List<Map<String,Object>> rows = new ArrayList<>();
        try (Connection conn = DBPool.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCnt = meta.getColumnCount();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCnt; i++)
                    row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                rows.add(row);
            }
        }
        return rows;
    }

    private int count(String table) {
        try (Connection conn = DBPool.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    private int countWhere(String table, String where) {
        String sql = "SELECT COUNT(*) FROM " + table + (where.isEmpty() ? "" : " WHERE " + where);
        try (Connection conn = DBPool.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }

    private String nvl(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
    private int toInt(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return def; }
    }
}
