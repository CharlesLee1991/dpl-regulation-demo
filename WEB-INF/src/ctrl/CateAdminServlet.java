package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 카테고리 관리 — 원본 _admin/regulations/cate_{list,form,proc}.asp + cate_detail_* 정합
 * URL: /cate_admin/?depth=1|2&mode=list|form|proc   테이블: dpl_law_category (원본 law_category 미러)
 * depth=1 중분류, depth=2 소분류(상위=중분류 select)
 */
public class CateAdminServlet extends HttpServlet {
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

    private int depth(HttpServletRequest req) { int d = toInt(req.getParameter("depth"),1); return d==2?2:1; }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int d = depth(req);
        int page = toInt(req.getParameter("page"), 1);
        String qWord = nvl(req.getParameter("qWord"), "");
        int qParent  = toInt(req.getParameter("qParent"), 0);
        int offset = (page-1)*PS;

        String w = "WHERE c.LC_DEPTH="+d;
        if (!qWord.isEmpty()) w += " AND c.LC_CATEGORY LIKE N'%"+qWord.replace("'","''")+"%'";
        if (d==2 && qParent>0) w += " AND c.LC_PARENT_IDX="+qParent;

        List<Map<String,Object>> list = sql("SELECT c.LC_IDX,c.LC_CATEGORY,c.LC_PARENT_IDX,c.LC_IS_USE,"
            + "CONVERT(NVARCHAR(10),c.LC_REG_DATE,120) AS LC_REG_DATE,ISNULL(p.LC_CATEGORY,'') AS PARENT_NAME "
            + "FROM dpl_law_category c LEFT JOIN dpl_law_category p ON p.LC_IDX=c.LC_PARENT_IDX "
            + w+" ORDER BY c.LC_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_category c "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("depth",d); req.setAttribute("qWord",qWord); req.setAttribute("qParent",qParent);
        req.setAttribute("cateTitle", d==2?"소분류 관리":"중분류 관리");
        if (d==2) req.setAttribute("parentList", sql("SELECT LC_IDX,LC_CATEGORY FROM dpl_law_category WHERE LC_DEPTH=1 AND LC_IS_USE='Y' ORDER BY LC_IDX"));
        req.getRequestDispatcher("/jsp/board/cate_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int d = depth(req);
        int idx = toInt(req.getParameter("lc_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT LC_IDX,LC_CATEGORY,LC_PARENT_IDX,LC_IS_USE FROM dpl_law_category WHERE LC_IDX="+idx);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("lcIdx",idx);
        req.setAttribute("depth",d);
        req.setAttribute("cateTitle", d==2?"소분류 관리":"중분류 관리");
        if (d==2) req.setAttribute("parentList", sql("SELECT LC_IDX,LC_CATEGORY FROM dpl_law_category WHERE LC_DEPTH=1 AND LC_IS_USE='Y' ORDER BY LC_IDX"));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/cate_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int d = depth(req);
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("lc_idx"), 0);
        String name   = nvl(req.getParameter("lc_category"), "");
        int parent    = (d==2) ? toInt(req.getParameter("lc_parent_idx"), 0) : 0;
        String isUse  = nvl(req.getParameter("lc_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && name.isEmpty()) rtnMsg = "카테고리명을 입력하세요.";
        if (rtnMsg.isEmpty() && d==2 && parent<=0 && !"DEL".equals(action)) rtnMsg = "상위 카테고리를 선택하세요.";
        if (rtnMsg.isEmpty() && "DEL".equals(action) && d==1 && idx>0
            && countSql("SELECT COUNT(*) FROM dpl_law_category WHERE LC_PARENT_IDX="+idx)>0)
            rtnMsg = "하위 소분류가 존재하여 삭제할 수 없습니다.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_law_category (LC_CATEGORY,LC_PARENT_IDX,LC_ROOT_IDX,LC_DEPTH,LC_IS_USE,LC_REG_DATE) VALUES (?,?,?,?,?,GETDATE())")) {
                        ps.setString(1,name); ps.setInt(2,parent); ps.setInt(3,parent); ps.setInt(4,d); ps.setString(5,isUse);
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_law_category SET LC_CATEGORY=?,LC_PARENT_IDX=?,LC_ROOT_IDX=?,LC_IS_USE=? WHERE LC_IDX=?")) {
                        ps.setString(1,name); ps.setInt(2,parent); ps.setInt(3,parent); ps.setString(4,isUse); ps.setInt(5,idx);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dpl_law_category WHERE LC_IDX=?")) {
                        ps.setInt(1,idx); ps.executeUpdate();
                    }
                }
            }
        }

        String page  = nvl(req.getParameter("page"), "1");
        String qWord = nvl(req.getParameter("qWord"), "");
        String linkParams = "depth="+d+"&page="+page
            + (qWord.isEmpty() ? "" : "&qWord="+java.net.URLEncoder.encode(qWord,"UTF-8"));

        if (rtnMsg.isEmpty()) {
            String url;
            if ("ADD".equals(action) || "DEL".equals(action))
                url = req.getContextPath()+"/cate_admin/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/cate_admin/?mode=form&lc_idx="+idx+"&"+linkParams;
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
