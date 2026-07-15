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
            } else if (uri.startsWith("/front/support/") && ("info".equals(req.getParameter("tab")) || "video".equals(req.getParameter("tab")) || "safety".equals(req.getParameter("tab")))) {
                doSupportBoard(req, resp);
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
        // ── 조회 가능 정보 현황 — 원본 m.main.asp getMainCnt() SQL 이식 (v5.3) ──
        // 원본: I_CNT = LAW_ITEMS_DETAIL 5중 INNER JOIN(ITEMS·GRADEMARK·NOTIFY·REGULATION·REGULATION_LEGAL, 전부 IS_USE='Y')
        //       R_CNT = count(LAW_RISKDB) / S_CNT = count(LAW_STANDARD)
        req.setAttribute("cntLegal", countSql(
            "SELECT COUNT(*) FROM dpl_items_detail AS D "
          + "INNER JOIN (SELECT * FROM dpl_items WHERE LI_IS_USE='Y') AS LI ON D.LI_IDX=LI.LI_IDX "
          + "INNER JOIN (SELECT * FROM dpl_grademark WHERE LG_IS_USE='Y') AS LG ON LI.LG_IDX=LG.LG_IDX "
          + "INNER JOIN (SELECT * FROM dpl_notify WHERE LN_IS_USE='Y') AS LN ON LI.LN_IDX=LN.LN_IDX "
          + "INNER JOIN (SELECT * FROM dpl_regulation WHERE LR_IS_USE='Y') AS LR ON LN.LR_IDX=LR.LR_IDX "
          + "INNER JOIN (SELECT * FROM dpl_regulation_legal WHERE LL_IS_USE='Y') AS LL ON LR.LL_IDX=LL.LL_IDX "
          + "WHERE D.LD_IS_USE='Y'"));
        req.setAttribute("cntSafety",   countSql("SELECT COUNT(*) FROM dpl_riskdb"));
        req.setAttribute("cntStandard", countSql("SELECT COUNT(*) FROM dpl_standard"));
        // 제품안전뉴스 = 원본 ctrlBoard(BBS_LEGAL_SAFETY=2) → dpl_law_board BD_CODE=2 (v5.3)
        req.setAttribute("safetyNews",  sql("SELECT TOP 5 BD_IDX,BD_TITLE,ISNULL(BD_ETC_COLS_2,'') AS BD_ETC_COLS_2,ISNULL(BD_ETC_COLS_4,'1') AS BD_ETC_COLS_4 FROM dpl_law_board WHERE BD_CODE=2 ORDER BY BD_IDX DESC"));
        req.setAttribute("mainShort",   sql("SELECT TOP 8 LS_IDX,LS_TITLE,ISNULL(LS_CONTENTS,'') AS LS_CONTENTS FROM dpl_shortclass WHERE LS_SHOWYN='Y' ORDER BY LS_IDX DESC"));
        req.setAttribute("refVideo",    sql("SELECT TOP 4 BD_IDX,BD_TITLE,ISNULL(BD_CONTENTS,'') AS BD_CONTENTS FROM dpl_law_board WHERE BD_CODE=8 ORDER BY BD_IDX DESC"));
        req.setAttribute("refInfo",     sql("SELECT TOP 4 BD_IDX,BD_TITLE,ISNULL(BD_CONTENTS,'') AS BD_CONTENTS FROM dpl_law_board WHERE BD_CODE=6 ORDER BY BD_IDX DESC"));
        // 법규 제·개정 = 원본 ctrlBoard(BBS_LEGAL_REVISE=1) → dpl_law_board BD_CODE=1 (v5.3)
        req.setAttribute("legalNews",   sql("SELECT TOP 5 BD_IDX,BD_TITLE,ISNULL(BD_ETC_COLS_1,'') AS BD_ETC_COLS_1,CONVERT(NVARCHAR(10),BD_REG_DATE,120) AS BD_REG_DATE FROM dpl_law_board WHERE BD_CODE=1 ORDER BY BD_IDX DESC"));
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
        // v5.3: 원본 LAW_RISKDB 컬럼(LR_*) 기준. JSP 호환 위해 기존 별칭(RD_*)으로 매핑.
        String w = "WHERE 1=1";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            String col;
            if      ("FACTOR".equals(qKey))   col="LR_FACTOR";
            else if ("CONTENTS".equals(qKey)) col="LR_CONTENTS";
            else if ("KEYWORD".equals(qKey))  col="LR_TITLE";   // 키워드=제목 매칭
            else                              col="LR_TITLE";
            w += " AND "+col+" LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qLT)   && !qLT.isEmpty())   w += " AND LR_TYPE=N'"+qLT.replace("'","''")+"'";
        if (!"0".equals(qLL)   && !qLL.isEmpty())   w += " AND LR_LEVEL="+toInt(qLL,0);
        List<Map<String,Object>> list = sql("SELECT LR_IDX AS RD_IDX,LR_TITLE AS RD_TITLE,LR_TYPE AS RD_TYPE,LR_FACTOR AS RD_FACTOR,LR_LEVEL AS RD_LEVEL,LR_SOURCE AS RD_SOURCE,CONVERT(NVARCHAR(10),LR_REG_DATE,120) AS RD_REG_DATE FROM dpl_riskdb "+w+" ORDER BY LR_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
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
        // v5.3: 원본 제품안전뉴스 = ctrlBoard(BBS_LEGAL_SAFETY=2) → dpl_law_board BD_CODE=2.
        // 기존 dpl_law_safety(자체 신설)는 폐기. JSP 호환 위해 기존 별칭(LS_*)으로 매핑.
        String w = "WHERE BD_CODE=2";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("SOURCE".equals(qKey)) w += " AND BD_ETC_COLS_2 LIKE N'%"+sw+"%'";
            else                            w += " AND BD_TITLE LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qC3) && !qC3.isEmpty()) w += " AND BD_ETC_COLS_3=N'"+qC3.replace("'","''")+"'";
        if (!"0".equals(qC4) && !qC4.isEmpty()) w += " AND BD_ETC_COLS_4=N'"+qC4.replace("'","''")+"'";
        List<Map<String,Object>> list = sql("SELECT BD_IDX AS LS_IDX,BD_TITLE AS LS_TITLE,ISNULL(BD_ETC_COLS_2,'') AS LS_COLS_02,ISNULL(BD_ETC_COLS_3,'') AS LS_COLS_03,ISNULL(BD_ETC_COLS_4,'1') AS LS_COLS_04,REPLACE(CONVERT(NVARCHAR(10),BD_REG_DATE,23),'-','.') AS LS_REG_DATE FROM dpl_law_board "+w+" ORDER BY BD_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_board "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.setAttribute("qC3",qC3); req.setAttribute("qC4",qC4);
        req.getRequestDispatcher("/jsp/front/front_safety_news_list.jsp").forward(req, resp);
    }

    // ── 위해정보 상세 ──────────────────────────────────────────────
    private void doRiskdbView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("rd_idx"),0);
        List<Map<String,Object>> rows = idx>0?sql("SELECT LR_IDX AS RD_IDX,LR_TITLE AS RD_TITLE,LR_TYPE AS RD_TYPE,LR_FACTOR AS RD_FACTOR,LR_LEVEL AS RD_LEVEL,LR_SOURCE AS RD_SOURCE,LR_URL AS RD_LINK,LR_CONTENTS AS RD_CONTENT,CONVERT(NVARCHAR(10),LR_REG_DATE,120) AS RD_REG_DATE FROM dpl_riskdb WHERE LR_IDX="+idx):new ArrayList<>();
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
        // v5.3: 원본 LAW_STANDARD 컬럼(LS_*) 기준. JSP 호환 위해 기존 별칭(ST_*)으로 매핑.
        String w = "WHERE 1=1";
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("CODE".equals(qKey))  w += " AND LS_CODE LIKE N'%"+sw+"%'";
            else if ("TITLE".equals(qKey)) w += " AND LS_TITLE LIKE N'%"+sw+"%'";
            else                           w += " AND LS_ITEMS LIKE N'%"+sw+"%'";
        }
        if (!"0".equals(qLD)   && !qLD.isEmpty())   w += " AND LS_DIV=N'"+qLD.replace("'","''")+"'";
        List<Map<String,Object>> list = sql("SELECT LS_IDX AS ST_IDX,LS_DIV AS ST_DIV,LS_CODE AS ST_CODE,LS_TITLE AS ST_TITLE,LS_ITEMS AS ST_ITEMS,LS_REV_DATE AS ST_VER_DATE FROM dpl_standard "+w+" ORDER BY LS_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
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
        List<Map<String,Object>> rows = idx>0?sql("SELECT LS_IDX AS ST_IDX,LS_DIV AS ST_DIV,LS_CODE AS ST_CODE,LS_TITLE AS ST_TITLE,LS_ITEMS AS ST_ITEMS,LS_REV_DATE AS ST_VER_DATE,'' AS ST_CONTENT,CONVERT(NVARCHAR(10),LS_REG_DATE,120) AS ST_REG_DATE FROM dpl_standard WHERE LS_IDX="+idx):new ArrayList<>();
        req.setAttribute("info",rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.getRequestDispatcher("/jsp/front/front_standard_view.jsp").forward(req, resp);
    }

    // ── 셀프러닝 게시판 (유용한정보/동영상/안전센터) ──────────────
    private void doSupportBoard(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String tab = nvl(req.getParameter("tab"),"info");
        int bdCode; String tabTitle;
        if      ("video".equals(tab))  { bdCode=8;  tabTitle="동영상 정보"; }
        else if ("safety".equals(tab)) { bdCode=10; tabTitle="안전센터 정보"; }
        else                           { bdCode=6;  tabTitle="유용한 정보"; }
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qKey  = nvl(req.getParameter("qKey"),"");
        String qWord = nvl(req.getParameter("qWord"),"");
        String w = "WHERE BD_CODE="+bdCode;   // v5.3: 원본 LAW_BOARD엔 BD_IS_USE 없음
        if (!qWord.isEmpty()) {
            String sw = qWord.replace("'","''");
            if      ("CONT".equals(qKey))  w += " AND BD_CONTENTS LIKE N'%"+sw+"%'";
            else if ("COL1".equals(qKey))  w += " AND BD_ETC_COLS_1 LIKE N'%"+sw+"%'";
            else if ("COL2".equals(qKey))  w += " AND BD_ETC_COLS_2 LIKE N'%"+sw+"%'";
            else                           w += " AND BD_TITLE LIKE N'%"+sw+"%'";
        }
        List<Map<String,Object>> list = sql("SELECT BD_IDX,BD_TITLE,BD_ETC_COLS_1,BD_ETC_COLS_2,BD_WRITER,REPLACE(CONVERT(NVARCHAR(10),BD_REG_DATE,23),'-','.') AS BD_REG_DATE FROM dpl_law_board "+w+" ORDER BY BD_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_law_board "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qKey",qKey); req.setAttribute("qWord",qWord);
        req.setAttribute("tab",tab); req.setAttribute("tabTitle",tabTitle);
        req.getRequestDispatcher("/jsp/front/front_support_board_list.jsp").forward(req, resp);
    }

    // ── 숏클래스 목록 ──────────────────────────────────────────────
    private void doShortclassList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page = toInt(req.getParameter("page"),1); int offset=(page-1)*PS;
        String qWord  = nvl(req.getParameter("qWord"),"");
        String qField = nvl(req.getParameter("qField"),"");
        String qLL    = nvl(req.getParameter("qLL"),"0");
        String qCate  = nvl(req.getParameter("qCate"),"0");
        // v5.3: 원본 LAW_SHORTCLASS 컬럼(LS_*) 기준. JSP 호환 위해 기존 별칭(SC_*)으로 매핑.
        String w = "WHERE LS_SHOWYN='Y'";
        if (!qWord.isEmpty())  w += " AND LS_TITLE LIKE N'%"+qWord.replace("'","''")+"%'";
        if (!qField.isEmpty()) w += " AND LS_DIV=N'"+qField.replace("'","''")+"'";
        List<Map<String,Object>> list = sql("SELECT LS_IDX AS SC_IDX,LS_TITLE AS SC_TITLE,ISNULL(LS_CONTENTS,'') AS SC_DESC,ISNULL(LS_DIV,'') AS SC_TYPE FROM dpl_shortclass "+w+" ORDER BY LS_IDX DESC OFFSET "+offset+" ROWS FETCH NEXT "+PS+" ROWS ONLY");
        int total = countSql("SELECT COUNT(*) FROM dpl_shortclass "+w);
        req.setAttribute("list",list); req.setAttribute("total",total); req.setAttribute("page",page);
        req.setAttribute("pageCnt",total>0?(int)Math.ceil((double)total/PS):1);
        req.setAttribute("qWord",qWord); req.setAttribute("qField",qField);
        req.setAttribute("qLL",qLL); req.setAttribute("qCate",qCate);
        req.setAttribute("featured",sql("SELECT TOP 4 LS_IDX AS SC_IDX,LS_TITLE AS SC_TITLE,ISNULL(LS_CONTENTS,'') AS SC_DESC,ISNULL(LS_DIV,'') AS SC_TYPE FROM dpl_shortclass WHERE LS_SHOWYN='Y' ORDER BY LS_IDX DESC"));
        req.getRequestDispatcher("/jsp/front/front_shortclass_list.jsp").forward(req, resp);
    }

    // ── 숏클래스 상세 ──────────────────────────────────────────────
    private void doShortclassView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idx = toInt(req.getParameter("sc_idx"),0);
        List<Map<String,Object>> rows = idx>0?sql("SELECT LS_IDX AS SC_IDX,LS_TITLE AS SC_TITLE,ISNULL(LS_CONTENTS,'') AS SC_DESC,ISNULL(LS_DIV,'') AS SC_TYPE,CONVERT(NVARCHAR(10),LS_REG_DATE,120) AS SC_REG_DATE FROM dpl_shortclass WHERE LS_IDX="+idx):new ArrayList<>();
        req.setAttribute("info",rows.isEmpty()?new LinkedHashMap<>():rows.get(0));
        req.getRequestDispatcher("/jsp/front/front_shortclass_view.jsp").forward(req, resp);
    }

    // ── 통합검색 ──────────────────────────────────────────────────
    private void doSearch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String qWord = nvl(req.getParameter("qWord"), nvl(req.getParameter("qSearchWord"),""));
        String sw = qWord.replace("'","''");

        List<Map<String,Object>> legalResult = new ArrayList<>();
        List<Map<String,Object>> riskdbResult = new ArrayList<>();
        List<Map<String,Object>> standardResult = new ArrayList<>();
        List<Map<String,Object>> shortclassResult = new ArrayList<>();
        int cntLegal=0, cntRiskdb=0, cntStandard=0, cntShortclass=0;

        if (!qWord.isEmpty()) {
            // 법규정보 = 품명(dpl_items_detail) 단위 — 원본 통합검색 정합 (doLegalList 조인 재사용)
            String legalBase = "FROM dpl_items_detail d "
                + "LEFT JOIN dpl_regulation r ON r.LR_IDX=d.LR_IDX "
                + "LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=d.LL_IDX "
                + "LEFT JOIN dpl_items ii ON ii.LI_IDX=d.LI_IDX "
                + "LEFT JOIN dpl_notify n ON n.LN_IDX=d.LN_IDX "
                + "WHERE d.LD_IS_USE='Y' AND (d.LD_ITEM_NAME LIKE N'%"+sw+"%' OR ISNULL(ii.LI_LEGAL_NAME,'') LIKE N'%"+sw+"%' OR ISNULL(l.LL_TITLE,'') LIKE N'%"+sw+"%')";
            legalResult = sql("SELECT TOP 5 d.LD_IDX,d.LR_IDX,d.LD_ITEM_NAME,"
                + "ISNULL(l.LL_TITLE,'') AS LL_TITLE,ISNULL(r.LR_TITLE,'') AS LR_TITLE,"
                + "ISNULL(ii.LI_LEGAL_NAME,'') AS LI_LEGAL_NAME,ISNULL(n.LN_TITLE,'') AS LN_TITLE,"
                + "CONVERT(NVARCHAR(10),d.LD_REG_DATE,120) AS LD_REG_DATE "
                + legalBase+" ORDER BY d.LD_IDX DESC");
            cntLegal = countSql("SELECT COUNT(*) "+legalBase);

            // 위해정보 = dpl_riskdb (v5.3: 원본 LR_* → JSP 호환 별칭 RD_*)
            String riskBase = "FROM dpl_riskdb WHERE (LR_TITLE LIKE N'%"+sw+"%' OR ISNULL(LR_FACTOR,'') LIKE N'%"+sw+"%')";
            riskdbResult = sql("SELECT TOP 5 LR_IDX AS RD_IDX,LR_TITLE AS RD_TITLE,ISNULL(LR_TYPE,'') AS RD_TYPE,ISNULL(LR_FACTOR,'') AS RD_FACTOR,"
                + "ISNULL(LR_SOURCE,'') AS RD_SOURCE,ISNULL(LR_URL,'') AS RD_LINK,"
                + "CONVERT(NVARCHAR(10),LR_REG_DATE,120) AS RD_REG_DATE "
                + riskBase+" ORDER BY LR_IDX DESC");
            cntRiskdb = countSql("SELECT COUNT(*) "+riskBase);

            // 롯데 스탠다드 = dpl_standard (v5.3: 원본 LS_* → JSP 호환 별칭 ST_*)
            String stdBase = "FROM dpl_standard WHERE (LS_TITLE LIKE N'%"+sw+"%' OR ISNULL(LS_ITEMS,'') LIKE N'%"+sw+"%' OR ISNULL(LS_CODE,'') LIKE N'%"+sw+"%')";
            standardResult = sql("SELECT TOP 5 LS_IDX AS ST_IDX,ISNULL(LS_DIV,'') AS ST_DIV,ISNULL(LS_CODE,'') AS ST_CODE,LS_TITLE AS ST_TITLE,"
                + "ISNULL(LS_ITEMS,'') AS ST_ITEMS,ISNULL(LS_REV_DATE,'') AS ST_VER_DATE "
                + stdBase+" ORDER BY LS_IDX DESC");
            cntStandard = countSql("SELECT COUNT(*) "+stdBase);

            // 숏클래스 = dpl_shortclass (v5.3: 원본 LS_* → JSP 호환 별칭 SC_*.
            //   규제법률 연결은 원본 체인 LS.LN_IDX → dpl_notify.LR_IDX → dpl_regulation.LL_IDX)
            String scBase = "FROM dpl_shortclass s "
                + "LEFT JOIN dpl_notify n ON n.LN_IDX=s.LN_IDX "
                + "LEFT JOIN dpl_regulation r ON r.LR_IDX=n.LR_IDX "
                + "LEFT JOIN dpl_regulation_legal l ON l.LL_IDX=r.LL_IDX "
                + "WHERE s.LS_SHOWYN='Y' AND (s.LS_TITLE LIKE N'%"+sw+"%' OR ISNULL(s.LS_CONTENTS,'') LIKE N'%"+sw+"%')";
            shortclassResult = sql("SELECT TOP 5 s.LS_IDX AS SC_IDX,s.LS_TITLE AS SC_TITLE,ISNULL(s.LS_CONTENTS,'') AS SC_DESC,ISNULL(s.LS_DIV,'') AS SC_TYPE,"
                + "ISNULL(s.LS_THUMB_PATH,'') AS SC_THUMB_URL,ISNULL(l.LL_TITLE,'') AS LL_TITLE,"
                + "CONVERT(NVARCHAR(10),s.LS_REG_DATE,120) AS SC_REG_DATE "
                + scBase+" ORDER BY s.LS_IDX DESC");
            cntShortclass = countSql("SELECT COUNT(*) "+scBase);
        }

        req.setAttribute("qWord",qWord);
        req.setAttribute("legalResult",legalResult); req.setAttribute("cntLegal",cntLegal);
        req.setAttribute("riskdbResult",riskdbResult); req.setAttribute("cntRiskdb",cntRiskdb);
        req.setAttribute("standardResult",standardResult); req.setAttribute("cntStandard",cntStandard);
        req.setAttribute("shortclassResult",shortclassResult); req.setAttribute("cntShortclass",cntShortclass);
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
