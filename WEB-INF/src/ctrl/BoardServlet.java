package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * 관리자 게시판(BBS) 관리 — 원본 _admin/bbs/support_{info,video,safety}_{list,form,proc}.asp 정합
 * URL: /board/?code=6|8|10&mode=list|form|proc  (BD_CODE: 6=유용한정보, 8=동영상, 10=안전센터)
 * 테이블: dpl_law_board (원본 law_board 컬럼 미러)
 */
public class BoardServlet extends HttpServlet {
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

    private int bdCode(HttpServletRequest req) {
        int c = toInt(req.getParameter("code"), 6);
        return (c==8||c==10) ? c : 6;
    }
    private String boardTitle(int code) {
        if (code==8)  return "동영상 정보";
        if (code==10) return "안전센터 정보";
        return "유용한 정보";
    }

    // 목록 — 원본 support_info_list.asp: No./제목/작성자/작성일/조회, 검색 전체/제목/내용, 정렬 TITLE/WRITER/RDATE/HIT
    private void doList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int code = bdCode(req);
        int page = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int offset = (page-1)*PS;

        String w = "WHERE BD_CODE="+code;
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("TITLE".equals(qKey)) w += " AND BD_TITLE LIKE N'%"+sw+"%'";
            else if ("CONT".equals(qKey))  w += " AND ISNULL(BD_CONTENTS,'') LIKE N'%"+sw+"%'";
            else w += " AND (BD_TITLE LIKE N'%"+sw+"%' OR ISNULL(BD_CONTENTS,'') LIKE N'%"+sw+"%')";
        }
        String order = "BD_IDX DESC";
        if (qSort.length()>2) {
            String dir = qSort.startsWith("A|") ? "ASC" : "DESC";
            String col = qSort.substring(2);
            if      ("TITLE".equals(col))  order = "BD_TITLE "+dir;
            else if ("WRITER".equals(col)) order = "BD_WRITER "+dir;
            else if ("RDATE".equals(col))  order = "BD_REG_DATE "+dir;
            else if ("HIT".equals(col))    order = "BD_HIT "+dir;
        }

        List<Map<String,Object>> list = sql("SELECT BD_IDX,BD_TITLE,ISNULL(BD_WRITER,'') AS BD_WRITER,"
            + "ISNULL(BD_HIT,0) AS BD_HIT,BD_IS_USE,CONVERT(NVARCHAR(10),BD_REG_DATE,120) AS BD_REG_DATE "
            + "FROM dpl_law_board "+w+" ORDER BY "+order+" OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_board "+w);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("code",code); req.setAttribute("boardTitle",boardTitle(code));
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord); req.setAttribute("qSort",qSort);
        req.getRequestDispatcher("/jsp/board/board_list.jsp").forward(req, resp);
    }

    // 등록/수정 폼 — 원본 support_info_form.asp: 제목/정보유형(cols1)/상품유형(cols2)/작성자/일자(cols3)/내용
    private void doForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int code = bdCode(req);
        int idx  = toInt(req.getParameter("bd_idx"), 0);
        Map<String,Object> info = null;
        if (idx>0) {
            List<Map<String,Object>> rows = sql("SELECT BD_IDX,BD_TITLE,ISNULL(BD_ETC_COLS_1,'') AS BD_ETC_COLS_1,"
                + "ISNULL(BD_ETC_COLS_2,'') AS BD_ETC_COLS_2,ISNULL(BD_ETC_COLS_3,'') AS BD_ETC_COLS_3,"
                + "ISNULL(BD_WRITER,'') AS BD_WRITER,ISNULL(BD_CONTENTS,'') AS BD_CONTENTS,BD_IS_USE "
                + "FROM dpl_law_board WHERE BD_IDX="+idx+" AND BD_CODE="+code);
            if (!rows.isEmpty()) info = rows.get(0);
        }
        req.setAttribute("info",info); req.setAttribute("bdIdx",idx);
        req.setAttribute("code",code); req.setAttribute("boardTitle",boardTitle(code));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"), ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",  nvl(req.getParameter("page"), "1"));
        req.getRequestDispatcher("/jsp/board/board_form.jsp").forward(req, resp);
    }

    // 처리 — ADD/MOD/DEL (PreparedStatement)
    private void doProc(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int code = bdCode(req);
        String action = nvl(req.getParameter("action"), "ADD");
        int idx       = toInt(req.getParameter("bd_idx"), 0);
        String title  = nvl(req.getParameter("bd_title"), "");
        String cols1  = nvl(req.getParameter("bd_etc_cols_1"), "");
        String cols2  = nvl(req.getParameter("bd_etc_cols_2"), "");
        String cols3  = nvl(req.getParameter("bd_etc_cols_3"), "");
        String writer = nvl(req.getParameter("bd_writer"), "");
        String cont   = nvl(req.getParameter("bd_contents"), "");
        String isUse  = nvl(req.getParameter("bd_is_use"), "Y");

        String rtnMsg = "";
        if (("ADD".equals(action)||"MOD".equals(action)) && title.isEmpty()) rtnMsg = "제목을 입력하세요.";

        if (rtnMsg.isEmpty()) {
            try (Connection conn = DBPool.getConnection()) {
                if ("ADD".equals(action)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dpl_law_board (BD_CODE,BD_TITLE,BD_ETC_COLS_1,BD_ETC_COLS_2,BD_ETC_COLS_3,BD_WRITER,BD_CONTENTS,BD_HIT,BD_IS_USE,BD_REG_DATE) "
                        + "VALUES (?,?,?,?,?,?,?,0,?,GETDATE())")) {
                        ps.setInt(1,code); ps.setString(2,title); ps.setString(3,cols1); ps.setString(4,cols2);
                        ps.setString(5,cols3); ps.setString(6,writer); ps.setString(7,cont); ps.setString(8,isUse);
                        ps.executeUpdate();
                    }
                } else if ("MOD".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dpl_law_board SET BD_TITLE=?,BD_ETC_COLS_1=?,BD_ETC_COLS_2=?,BD_ETC_COLS_3=?,BD_WRITER=?,BD_CONTENTS=?,BD_IS_USE=? "
                        + "WHERE BD_IDX=? AND BD_CODE=?")) {
                        ps.setString(1,title); ps.setString(2,cols1); ps.setString(3,cols2); ps.setString(4,cols3);
                        ps.setString(5,writer); ps.setString(6,cont); ps.setString(7,isUse);
                        ps.setInt(8,idx); ps.setInt(9,code);
                        ps.executeUpdate();
                    }
                } else if ("DEL".equals(action) && idx>0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dpl_law_board WHERE BD_IDX=? AND BD_CODE=?")) {
                        ps.setInt(1,idx); ps.setInt(2,code);
                        ps.executeUpdate();
                    }
                }
            }
        }

        String page  = nvl(req.getParameter("page"), "1");
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String linkParams = "code="+code+"&page="+page
            + (qKey.isEmpty()  ? "" : "&qKey="+qKey)
            + (qWord.isEmpty() ? "" : "&qWord="+java.net.URLEncoder.encode(qWord,"UTF-8"));

        if (rtnMsg.isEmpty()) {
            String url;
            if ("ADD".equals(action) || "DEL".equals(action))
                url = req.getContextPath()+"/board/?mode=list&"+linkParams;
            else
                url = req.getContextPath()+"/board/?mode=form&bd_idx="+idx+"&"+linkParams;
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
