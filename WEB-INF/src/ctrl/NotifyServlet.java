package ctrl;

import mdl.NotifyDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * MODULE: notify (고시/부속서)
 * 원본: _admin/regulations/notify_*.asp
 * URL: /notify/?mode=list|form|proc
 */
public class NotifyServlet extends HttpServlet {

    private static final int LIST_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String mode = nvl(req.getParameter("mode"), "list");
        NotifyDAO dao = new NotifyDAO();

        try {
            switch (mode) {
                case "list": doList(req, resp, dao); break;
                case "form": doForm(req, resp, dao); break;
                default:     doList(req, resp, dao);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        if ("proc".equals(nvl(req.getParameter("mode"), ""))) {
            try { doProc(req, resp, new NotifyDAO()); }
            catch (Exception e) { throw new ServletException(e); }
        } else {
            doGet(req, resp);
        }
    }

    // ── LIST ────────────────────────────────────────────────────────
    private void doList(HttpServletRequest req, HttpServletResponse resp,
                        NotifyDAO dao) throws Exception {

        int page     = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"),  "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int qLL      = toInt(req.getParameter("qLL"), 0);
        int qLR      = toInt(req.getParameter("qLR"), 0);

        Map<String, Object> data = dao.getListPaging(page, LIST_SIZE, qKey, qWord, qLL, qLR, qSort);
        int total   = (int) data.get("total");
        int pageCnt = (total > 0) ? (int) Math.ceil((double) total / LIST_SIZE) : 1;

        req.setAttribute("list",     data.get("rows"));
        req.setAttribute("total",    total);
        req.setAttribute("page",     page);
        req.setAttribute("pageCnt",  pageCnt);
        req.setAttribute("listSize", LIST_SIZE);
        req.setAttribute("qKey",     qKey);
        req.setAttribute("qWord",    qWord);
        req.setAttribute("qSort",    qSort);
        req.setAttribute("qLL",      qLL);
        req.setAttribute("qLR",      qLR);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        // 2단계 드롭다운: qLL 선택돼 있으면 규제사항 목록도 로드
        if (qLL > 0) {
            req.setAttribute("regulationList", dao.getRegulationList("Y", qLL));
        }

        req.getRequestDispatcher("/jsp/notify/notify_list.jsp").forward(req, resp);
    }

    // ── FORM (등록/수정) ─────────────────────────────────────────────
    private void doForm(HttpServletRequest req, HttpServletResponse resp,
                        NotifyDAO dao) throws Exception {

        int lnIdx  = toInt(req.getParameter("ln_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();

        if (lnIdx > 0) {
            action = "MOD";
            info = dao.getInfo(lnIdx);
        }

        int llIdx = toInt(
            info.get("ll_idx") != null ? info.get("ll_idx").toString() : req.getParameter("ll_idx"),
            0);

        req.setAttribute("action",    action);
        req.setAttribute("info",      info);
        req.setAttribute("lnIdx",     lnIdx);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        // 수정 시 2단계 드롭다운 — 기존 ll_idx 기준으로 규제사항 목록 로드
        if (llIdx > 0) {
            req.setAttribute("regulationList", dao.getRegulationList("Y", llIdx));
        }
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));
        req.setAttribute("qLR",   nvl(req.getParameter("qLR"),   "0"));
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/notify/notify_form.jsp").forward(req, resp);
    }

    // ── PROC (저장처리) ──────────────────────────────────────────────
    private void doProc(HttpServletRequest req, HttpServletResponse resp,
                        NotifyDAO dao) throws Exception {

        String action   = nvl(req.getParameter("action"),    "ADD");
        int    lnIdx    = toInt(req.getParameter("ln_idx"),   0);
        String notify   = nvl(req.getParameter("ln_notify"),  "");
        int    lrIdx    = toInt(req.getParameter("lr_idx"),   0);
        String history  = nvl(req.getParameter("ln_history"), "");
        String isUse    = nvl(req.getParameter("ln_is_use"),  "N");
        String attaFiles = "";   // 첨부파일: 데모에서는 패스스루 처리
        String regUser  = nvl((String) req.getSession().getAttribute("loginId"), "");
        String regIp    = nvl(req.getRemoteAddr(), "");

        Map<String, Object> rtn = dao.putInfo(
                action, lnIdx, notify, lrIdx,
                history, isUse, attaFiles, regUser, regIp);

        String rtnMsg = (String) rtn.get("rtn_msg");
        int    rtnIdx = (int)    rtn.get("rtn_idx");

        String linkParams = buildLinkParams(req);

        if (rtnMsg == null || rtnMsg.isEmpty()) {
            String redirectUrl;
            if ("ADD".equals(action) || "DEL".equals(action)) {
                redirectUrl = req.getContextPath() + "/notify/?mode=list" + linkParams;
            } else {
                redirectUrl = req.getContextPath() + "/notify/?mode=form&ln_idx=" + rtnIdx + linkParams;
            }
            resp.sendRedirect(redirectUrl);
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>");
            out.println("alert('" + rtnMsg.replace("'", "\\'") + "');");
            out.println("history.back();");
            out.println("</script>");
        }
    }

    // ── 유틸 ─────────────────────────────────────────────────────────
    private String buildLinkParams(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        for (String k : new String[]{"qKey","qWord","qSort","qLL","qLR","page"}) {
            String v = req.getParameter(k);
            if (v != null && !v.isEmpty()) sb.append("&").append(k).append("=").append(v);
        }
        return sb.toString();
    }

    private String nvl(String v, String def) { return (v == null || v.isEmpty()) ? def : v; }
    private int toInt(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return def; }
    }
}
