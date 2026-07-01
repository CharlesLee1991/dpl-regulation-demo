package ctrl;

import db.DBPool;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * 프론트엔드 사용자 화면  /front/*
 * - /front/                메인
 * - /front/legal/          법규정보 목록
 * - /front/legal/view      법규정보 상세
 * - /front/safety/         위해정보 목록
 * - /front/safety/view     위해정보 상세
 * - /front/standard/       롯데스탠다드 목록
 * - /front/standard/view   롯데스탠다드 상세
 * - /front/support/        숏클래스 목록
 * - /front/support/view    숏클래스 상세
 * - /front/search/         통합검색 결과
 */
public class FrontServlet extends HttpServlet {

    private static final int PS = 10; // page size

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String uri = req.getRequestURI();
        try {
            if (uri.equals("/front/") || uri.equals("/front") || uri.startsWith("/front/index")) {
                doMain(req, resp);
            } else if (uri.startsWith("/front/legal/view")) {
                doLegalView(req, resp);
            } else if (uri.startsWith("/front/legal/") && "revise".equals(req.getParameter("tab"))) {
                doLegalRevise(req, resp);
            } else if (uri.startsWith("/front/legal/")) {
                doLegalList(req, resp);
            } else if (uri.startsWith("/front/safety/view")) {
                doRiskdbView(req, resp);
            } else if (uri.startsWith("/front/safety/") && "news".equals(req.getParameter("tab"))) {
                doSafetyNews(req, resp);
            } else if (uri.startsWith("/front/safety/")) {
                doRiskdbList(req, resp);
            } else if (uri.startsWith("/front/standard/view")) {
                doStandardView(req, resp);
            } else if (uri.startsWith("/front/standard/")) {
                doStandardList(req, resp);
            } else if (uri.startsWith("/front/support/view")) {
                doShortclassView(req, resp);
            } else if (uri.startsWith("/front/support/")) {
                doShortclassList(req, resp);
            } else if (uri.startsWith("/front/search/")) {
                doSearch(req, resp);
            } else {
                doMain(req, resp);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    // ── 메인 ──────────────────────────────────────────────────────
    private void doMain(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.setAttribute("cntLegal",    count("dpl_regulation"));
        req.setAttribute("cntSafety",   count("dpl_riskdb"));
        req.setAttribute("cntStandard", count("dpl_standard"));
        req.setAttribute("recentLegal", sql("SELECT TOP 5 r.LR_IDX,r.LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IS_USE='Y' ORDER BY r.LR_IDX DESC"));
        req.setAttribute("legalNews",   sql("SELECT TOP 5 r.LR_IDX,r.LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IS_USE='Y' ORDER BY r.REG_DATE DESC"));
        req.getRequestDispatcher("/jsp/front/front_main.jsp").forward(req, resp);
    }

    // ── 법규정보 목록 ──────────────────────────────────────────────
    private void doLegalList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"), 1);
        int qLL  = toInt(req.getParameter("qLL"), 0);
        int qLR  = toInt(req.getParameter("qLR"), 0);
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int offset = (page-1)*PS;
        // 원본 MPRD F2 = 품명(세부품목) 단위 목록 + 적용구분(사용연령)·재질(형태)
        String w = "WHERE d.LD_IS_USE='Y'";
        if (qLL>0)         w += " AND d.LL_IDX="+qLL;
        if (qLR>0)         w += " AND d.LR_IDX="+qLR;
        if (!qWord.isEmpty()) w += " AND d.LD_ITEM_NAME LIKE N'%"+qWord.replace("'","''")+"%'";
        String order = "NAME".equals(qSort) ? "d.LD_ITEM_NAME ASC, d.LD_IDX DESC" : "d.LD_IDX DESC";

        String base = "FROM dpl_items_detail d "
            + "LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX "
            + "LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX "+w;
        List<Map<String,Object>> list = sql("SELECT d.LD_IDX,d.LR_IDX,d.LD_ITEM_NAME,"
            + "ISNULL(d.LD_USE_AGE,'') AS LD_USE_AGE,ISNULL(d.LD_MATERIAL,'') AS LD_MATERIAL,"
            + "ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE "
            + base+" ORDER BY "+order+" OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) "+base);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qLL",qLL); req.setAttribute("qLR",qLR); req.setAttribute("qWord",qWord); req.setAttribute("qSort",qSort);
        req.setAttribute("legalList",sql("SELECT LL_IDX,LL_TITLE FROM dpl_regulation_legal WHERE LL_IS_USE='Y' ORDER BY LL_SORT"));
        req.setAttribute("regulationList",qLL>0?sql("SELECT LR_IDX,LR_TITLE FROM dpl_regulation WHERE LR_IS_USE='Y' AND LL_IDX="+qLL+" ORDER BY LR_TITLE"):new ArrayList<>());
        req.getRequestDispatcher("/jsp/front/front_legal_list.jsp").forward(req, resp);
    }

    // ── 법규 제·개정 정보 (F3) ─────────────────────────────────────
    private void doLegalRevise(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qType = nvl(req.getParameter("qC7"), "");   // 고시유형
        int offset = (page-1)*PS;

        String w = "WHERE n.LN_IS_USE='Y' AND ISNULL(n.LN_TYPE,'')<>''";
        if (!qType.isEmpty()) w += " AND n.LN_TYPE=N'"+qType.replace("'","''")+"'";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            // qKey: TITLE/CONT/COL1(고시번호)/COL2(고시명)/COL5(담당부처)/COL8(품목)
            if ("COL5".equals(qKey))      w += " AND n.LN_DEPT LIKE N'%"+sw+"%'";
            else if ("COL8".equals(qKey)) w += " AND n.LN_ITEM LIKE N'%"+sw+"%'";
            else                          w += " AND n.LN_TITLE LIKE N'%"+sw+"%'";
        }
        String base = "FROM dpl_notify n "+w;
        List<Map<String,Object>> list = sql("SELECT n.LN_IDX,n.LN_TITLE,"
            + "ISNULL(n.LN_TYPE,'') AS LN_TYPE,ISNULL(n.LN_ITEM,'') AS LN_ITEM,"
            + "ISNULL(n.LN_NOTI_DATE,'') AS LN_NOTI_DATE,ISNULL(n.LN_EXEC_DATE,'') AS LN_EXEC_DATE,"
            + "ISNULL(n.LN_DEPT,'') AS LN_DEPT "
            + base+" ORDER BY n.LN_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) "+base);

        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord); req.setAttribute("qC7",qType);
        req.getRequestDispatcher("/jsp/front/front_legal_revise_list.jsp").forward(req, resp);
    }

    // ── 법규정보 상세 ──────────────────────────────────────────────
    private void doLegalView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("lr_idx"),0);
        List<Map<String,Object>> rows = idx>0 ? sql("SELECT r.LR_IDX,r.LR_TITLE,ISNULL(r.LR_CONDITION,'') AS LR_CONDITION,ISNULL(r.LR_CERTIFY_GUIDE,'') AS LR_CERTIFY_GUIDE,ISNULL(r.LR_PENALTY,'') AS LR_PENALTY,ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(l.LL_DEPT,'') AS LL_DEPT,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IDX="+idx) : new ArrayList<>();
        req.setAttribute("info", rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.setAttribute("relatedNotify", idx>0?sql("SELECT LN_IDX,LN_TITLE FROM dpl_notify WHERE LN_IS_USE='Y' AND LR_IDX="+idx+" ORDER BY LN_TITLE"):new ArrayList<>());
        req.getRequestDispatcher("/jsp/front/front_legal_view.jsp").forward(req, resp);
    }

    // ── 위해정보 목록 ──────────────────────────────────────────────
    private void doRiskdbList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qKey  = nvl(req.getParameter("qKey"),"TITLE");
        String qWord = nvl(req.getParameter("qWord"),"");
        String qSort = nvl(req.getParameter("qSort"),"");
        String qCate = nvl(req.getParameter("qCate"),"0");
        String qLT   = nvl(req.getParameter("qLT"),"0");
        String qLL   = nvl(req.getParameter("qLL"),"0");
        String w = "WHERE RD_IS_USE='Y'";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            String col;
            if      ("FACTOR".equals(qKey))   col="RD_FACTOR";
            else if ("CONTENTS".equals(qKey)) col="RD_CONTENT";
            else if ("KEYWORD".equals(qKey))  col="RD_TITLE";   // 키워드=제목 매칭
            else                              col="RD_TITLE";
            w += " AND "+col+" LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qCate) && !qCate.isEmpty()) w += " AND RD_CATE="+toInt(qCate,0);
        if (!"0".equals(qLT)   && !qLT.isEmpty())   w += " AND RD_TYPE=N'"+qLT.replace("'","''")+"'";
        if (!"0".equals(qLL)   && !qLL.isEmpty())   w += " AND RD_LEVEL="+toInt(qLL,0);
        List<Map<String,Object>> list = sql("SELECT RD_IDX,RD_TITLE,RD_TYPE,RD_FACTOR,RD_LEVEL,RD_SOURCE,CONVERT(NVARCHAR(10),RD_REG_DATE,120) AS RD_REG_DATE FROM dpl_riskdb "+w+" ORDER BY RD_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_riskdb "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord); req.setAttribute("qSort",qSort);
        req.setAttribute("qCate",qCate); req.setAttribute("qLT",qLT); req.setAttribute("qLL",qLL);
        req.getRequestDispatcher("/jsp/front/front_riskdb_list.jsp").forward(req, resp);
    }

    // ── 제품안전 뉴스 ──────────────────────────────────────────────
    private void doSafetyNews(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qKey  = nvl(req.getParameter("qKey"),"");
        String qWord = nvl(req.getParameter("qWord"),"");
        String qC3   = nvl(req.getParameter("qC3"),"0");   // 정보구분
        String qC4   = nvl(req.getParameter("qC4"),"0");   // 중요도등급
        String w = "WHERE LS_IS_USE='Y'";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("SOURCE".equals(qKey)) w += " AND LS_COLS_02 LIKE N'%"+sw+"%'";
            else                            w += " AND LS_TITLE LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qC3) && !qC3.isEmpty()) w += " AND LS_COLS_03="+toInt(qC3,0);
        if (!"0".equals(qC4) && !qC4.isEmpty()) w += " AND LS_COLS_04="+toInt(qC4,0);
        List<Map<String,Object>> list = sql("SELECT LS_IDX,LS_TITLE,LS_COLS_02,LS_COLS_03,LS_COLS_04,REPLACE(CONVERT(NVARCHAR(10),LS_REG_DATE,23),'-','.') AS LS_REG_DATE FROM dpl_law_safety "+w+" ORDER BY LS_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_safety "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.setAttribute("qC3",qC3); req.setAttribute("qC4",qC4);
        req.getRequestDispatcher("/jsp/front/front_safety_news_list.jsp").forward(req, resp);
    }

    // ── 위해정보 상세 ──────────────────────────────────────────────
    private void doRiskdbView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("rd_idx"),0);
        List<Map<String,Object>> rows = idx>0?sql("SELECT RD_IDX,RD_TITLE,RD_TYPE,RD_FACTOR,RD_LEVEL,RD_SOURCE,RD_LINK,RD_CONTENT,CONVERT(NVARCHAR(10),RD_REG_DATE,120) AS RD_REG_DATE FROM dpl_riskdb WHERE RD_IDX="+idx):new ArrayList<>();
        req.setAttribute("info",rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.getRequestDispatcher("/jsp/front/front_riskdb_view.jsp").forward(req, resp);
    }

    // ── 롯데스탠다드 목록 ──────────────────────────────────────────
    private void doStandardList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qKey  = nvl(req.getParameter("qKey"),"ITEMS");
        String qWord = nvl(req.getParameter("qWord"),"");
        String qCate = nvl(req.getParameter("qCate"),"0");
        String qLD   = nvl(req.getParameter("qLD"),"0");
        String w = "WHERE ST_IS_USE='Y'";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("CODE".equals(qKey))  w += " AND ST_CODE LIKE N'%"+sw+"%'";
            else if ("TITLE".equals(qKey)) w += " AND ST_TITLE LIKE N'%"+sw+"%'";
            else                           w += " AND ST_ITEMS LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qCate) && !qCate.isEmpty()) w += " AND ST_CATE="+toInt(qCate,0);
        if (!"0".equals(qLD)   && !qLD.isEmpty())   w += " AND ST_DIV=N'"+qLD.replace("'","''")+"'";
        List<Map<String,Object>> list = sql("SELECT ST_IDX,ST_DIV,ST_CODE,ST_TITLE,ST_ITEMS,ST_VER_DATE FROM dpl_standard "+w+" ORDER BY ST_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_standard "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.setAttribute("qCate",qCate); req.setAttribute("qLD",qLD);
        req.getRequestDispatcher("/jsp/front/front_standard_list.jsp").forward(req, resp);
    }

    // ── 롯데스탠다드 상세 ──────────────────────────────────────────
    private void doStandardView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("st_idx"),0);
        List<Map<String,Object>> rows = idx>0?sql("SELECT ST_IDX,ST_DIV,ST_CODE,ST_TITLE,ST_ITEMS,ST_VER_DATE,ST_CONTENT,CONVERT(NVARCHAR(10),ST_REG_DATE,120) AS ST_REG_DATE FROM dpl_standard WHERE ST_IDX="+idx):new ArrayList<>();
        req.setAttribute("info",rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.getRequestDispatcher("/jsp/front/front_standard_view.jsp").forward(req, resp);
    }

    // ── 숏클래스 목록 ──────────────────────────────────────────────
    private void doShortclassList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qWord  = nvl(req.getParameter("qWord"),"");
        String qField = nvl(req.getParameter("qField"),"");
        String qLL    = nvl(req.getParameter("qLL"),"0");
        String qCate  = nvl(req.getParameter("qCate"),"0");
        String w = "WHERE SC_IS_USE='Y'";
        if (!qWord.isEmpty())  w += " AND SC_TITLE LIKE N'%"+qWord.replace("'","''")+"%'";
        if (!qField.isEmpty()) w += " AND SC_TYPE=N'"+qField.replace("'","''")+"'";
        if (!"0".equals(qLL)   && !qLL.isEmpty())   w += " AND SC_LL_IDX="+toInt(qLL,0);
        if (!"0".equals(qCate) && !qCate.isEmpty()) w += " AND SC_CATE="+toInt(qCate,0);
        List<Map<String,Object>> list = sql("SELECT SC_IDX,SC_TITLE,SC_DESC,SC_TYPE FROM dpl_shortclass "+w+" ORDER BY SC_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_shortclass "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qWord",qWord); req.setAttribute("qField",qField);
        req.setAttribute("qLL",qLL); req.setAttribute("qCate",qCate);
        req.setAttribute("featured",sql("SELECT TOP 4 SC_IDX,SC_TITLE,SC_DESC,SC_TYPE FROM dpl_shortclass WHERE SC_IS_USE='Y' ORDER BY SC_IDX DESC"));
        req.getRequestDispatcher("/jsp/front/front_shortclass_list.jsp").forward(req, resp);
    }

    // ── 숏클래스 상세 ──────────────────────────────────────────────
    private void doShortclassView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("sc_idx"),0);
        List<Map<String,Object>> rows = idx>0?sql("SELECT SC_IDX,SC_TITLE,SC_DESC,SC_TYPE,CONVERT(NVARCHAR(10),SC_REG_DATE,120) AS SC_REG_DATE FROM dpl_shortclass WHERE SC_IDX="+idx):new ArrayList<>();
        req.setAttribute("info",rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.getRequestDispatcher("/jsp/front/front_shortclass_view.jsp").forward(req, resp);
    }

    // ── 통합검색 ──────────────────────────────────────────────────
    private void doSearch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String qWord = nvl(req.getParameter("qWord"), nvl(req.getParameter("qSearchWord"),""));
        String w = qWord.isEmpty() ? "1=1" : "";
        String safeWord = qWord.replace("'","''");

        List<Map<String,Object>> legalResult = new ArrayList<>();
        List<Map<String,Object>> riskdbResult = new ArrayList<>();
        List<Map<String,Object>> standardResult = new ArrayList<>();
        int cntLegal=0, cntRiskdb=0, cntStandard=0;

        if (!qWord.isEmpty()) {
            legalResult  = sql("SELECT TOP 5 r.LR_IDX,r.LR_TITLE,ISNULL(l.LL_TITLE,'') AS LL_TITLE,CONVERT(NVARCHAR(10),r.REG_DATE,120) AS LR_REG_DATE FROM dpl_regulation r LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX WHERE r.LR_IS_USE='Y' AND (r.LR_TITLE LIKE N'%"+safeWord+"%' OR l.LL_TITLE LIKE N'%"+safeWord+"%') ORDER BY r.LR_IDX DESC");
            riskdbResult = sql("SELECT TOP 5 RD_IDX,RD_TITLE,RD_TYPE,CONVERT(NVARCHAR(10),RD_REG_DATE,120) AS RD_REG_DATE FROM dpl_riskdb WHERE RD_IS_USE='Y' AND RD_TITLE LIKE N'%"+safeWord+"%' ORDER BY RD_IDX DESC");
            standardResult=sql("SELECT TOP 5 ST_IDX,ST_DIV,ST_CODE,ST_TITLE FROM dpl_standard WHERE ST_IS_USE='Y' AND (ST_TITLE LIKE N'%"+safeWord+"%' OR ST_ITEMS LIKE N'%"+safeWord+"%') ORDER BY ST_IDX DESC");
            cntLegal   = countSql("SELECT COUNT(*) FROM dpl_regulation WHERE LR_IS_USE='Y' AND LR_TITLE LIKE N'%"+safeWord+"%'");
            cntRiskdb  = countSql("SELECT COUNT(*) FROM dpl_riskdb WHERE RD_IS_USE='Y' AND RD_TITLE LIKE N'%"+safeWord+"%'");
            cntStandard= countSql("SELECT COUNT(*) FROM dpl_standard WHERE ST_IS_USE='Y' AND ST_TITLE LIKE N'%"+safeWord+"%'");
        }

        req.setAttribute("qWord",qWord);
        req.setAttribute("legalResult",legalResult); req.setAttribute("cntLegal",cntLegal);
        req.setAttribute("riskdbResult",riskdbResult); req.setAttribute("cntRiskdb",cntRiskdb);
        req.setAttribute("standardResult",standardResult); req.setAttribute("cntStandard",cntStandard);
        req.getRequestDispatcher("/jsp/front/front_search_result.jsp").forward(req, resp);
    }

    // ── DB 헬퍼 ──────────────────────────────────────────────────
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
    private int count(String table) {
        try (Connection conn=DBPool.getConnection(); Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM "+table)) {
            if(rs.next()) return rs.getInt(1);
        } catch(Exception ignored){}
        return 0;
    }
    private int countSql(String query) {
        try (Connection conn=DBPool.getConnection(); Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery(query)) {
            if(rs.next()) return rs.getInt(1);
        } catch(Exception ignored){}
        return 0;
    }
    private String nvl(String v, String def) { return (v==null||v.isEmpty())?def:v; }
    private int toInt(String v, int def) {
        if(v==null) return def;
        try { return Integer.parseInt(v.trim()); } catch(NumberFormatException e) { return def; }
    }
}
