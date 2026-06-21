package ctrl;

import mdl.RegulationDAO;
import mdl.LegalDAO;
import mdl.NotifyDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * 프론트엔드 사용자 화면
 * /front/ 메인, /front/legal/ 법규목록/상세, /front/safety/ 위해정보
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
            if (uri.equals("/front/") || uri.equals("/front") || uri.equals("/front/index")) {
                doMain(req, resp);
            } else if (uri.startsWith("/front/legal/view")) {
                doLegalView(req, resp);
            } else if (uri.startsWith("/front/legal/")) {
                doLegalList(req, resp);
            } else if (uri.startsWith("/front/safety/")) {
                doSafety(req, resp);
            } else if (uri.startsWith("/front/standard/")) {
                doStandard(req, resp);
            } else if (uri.startsWith("/front/support/")) {
                doSupport(req, resp);
            } else {
                doMain(req, resp);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    // ── 메인 ────────────────────────────────────────────────────
    private void doMain(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        RegulationDAO dao = new RegulationDAO();
        LegalDAO legalDAO = new LegalDAO();

        // 건수
        int cntLegal = countTable("dpl_regulation");
        int cntSafety = countTable("dpl_safety");
        int cntStandard = countTable("dpl_items");

        // 최신 법규사항 (목록 5건)
        Map<String, Object> data = dao.getListPaging(1, 5, "", "", 0, "");
        Map<String, Object> legalData = legalDAO.getListPaging(1, 5, "", "");

        req.setAttribute("cntLegal",    cntLegal);
        req.setAttribute("cntSafety",   cntSafety);
        req.setAttribute("cntStandard", cntStandard);
        req.setAttribute("recentLegal", data.get("rows"));
        req.setAttribute("legalNews",   legalData.get("rows"));
        req.getRequestDispatcher("/jsp/front/front_main.jsp").forward(req, resp);
    }

    // ── 법규정보 목록 ────────────────────────────────────────────
    private void doLegalList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int page     = toInt(req.getParameter("page"), 1);
        int qLL      = toInt(req.getParameter("qLL"), 0);
        int qLR      = toInt(req.getParameter("qLR"), 0);
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");

        RegulationDAO dao = new RegulationDAO();
        Map<String, Object> data = dao.getListPaging(page, LIST_SIZE, "TITLE", qWord, qLL, qSort);
        int total   = (int) data.get("total");
        int pageCnt = (total > 0) ? (int) Math.ceil((double) total / LIST_SIZE) : 1;

        req.setAttribute("list",    data.get("rows"));
        req.setAttribute("total",   total);
        req.setAttribute("page",    page);
        req.setAttribute("pageCnt", pageCnt);
        req.setAttribute("qLL",     qLL);
        req.setAttribute("qLR",     qLR);
        req.setAttribute("qWord",   qWord);
        req.setAttribute("qSort",   qSort);
        req.setAttribute("legalList", new LegalDAO().getListAll("Y"));
        if (qLL > 0) req.setAttribute("regulationList", dao.getListAll("Y", qLL, 0));
        req.getRequestDispatcher("/jsp/front/front_legal_list.jsp").forward(req, resp);
    }

    // ── 법규정보 상세 ────────────────────────────────────────────
    private void doLegalView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int lrIdx = toInt(req.getParameter("lr_idx"), 0);
        RegulationDAO dao = new RegulationDAO();
        NotifyDAO notifyDAO = new NotifyDAO();

        Map<String, Object> info = lrIdx > 0 ? dao.getInfo(lrIdx) : new LinkedHashMap<>();
        List<Map<String, Object>> relatedNotify = new ArrayList<>();
        if (!info.isEmpty() && info.get("lr_idx") != null) {
            relatedNotify = notifyDAO.getListAll("Y", lrIdx, 0);
        }

        req.setAttribute("info",          info);
        req.setAttribute("relatedNotify", relatedNotify);
        req.getRequestDispatcher("/jsp/front/front_legal_view.jsp").forward(req, resp);
    }

    // ── 위해정보 / 스탠다드 / 셀프러닝 (placeholder) ──────────────
    private void doSafety(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.getRequestDispatcher("/jsp/front/front_safety_list.jsp").forward(req, resp);
    }
    private void doStandard(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.getRequestDispatcher("/jsp/front/front_safety_list.jsp").forward(req, resp);
    }
    private void doSupport(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        req.getRequestDispatcher("/jsp/front/front_safety_list.jsp").forward(req, resp);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────
    private int countTable(String tableName) {
        try (java.sql.Connection conn = db.DBPool.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
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
