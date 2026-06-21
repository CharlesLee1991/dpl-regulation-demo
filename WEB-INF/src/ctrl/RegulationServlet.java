package ctrl;

import mdl.RegulationDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * MODULE: regulation (규제사항)
 * 원본: _system/ctrl/c.regulation.asp + _admin/regulations/*.asp
 * URL: /regulation/?mode=list|form|view|proc
 */
public class RegulationServlet extends HttpServlet {

    private static final int LIST_SIZE_ADMIN = 20;
    private static final int LIST_SIZE_FRONT = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String mode = nvl(req.getParameter("mode"), "list");
        RegulationDAO dao = new RegulationDAO();

        try {
            switch (mode) {
                case "list":  doList(req, resp, dao);  break;
                case "form":  doForm(req, resp, dao);  break;
                case "view":  doView(req, resp, dao);  break;
                default:      doList(req, resp, dao);
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

        String mode = nvl(req.getParameter("mode"), "");
        if ("proc".equals(mode)) {
            try { doProc(req, resp, new RegulationDAO()); }
            catch (Exception e) { throw new ServletException(e); }
        } else {
            doGet(req, resp);
        }
    }

    // ── LIST ────────────────────────────────────────────────────────
    private void doList(HttpServletRequest req, HttpServletResponse resp,
                        RegulationDAO dao) throws Exception {

        int page     = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"),  "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int qLL      = toInt(req.getParameter("qLL"), 0);
        String site  = nvl(req.getParameter("site"), "admin");

        int listSize = "front".equals(site) ? LIST_SIZE_FRONT : LIST_SIZE_ADMIN;

        Map<String, Object> data = dao.getListPaging(page, listSize, qKey, qWord, qLL, qSort);
        int total = (int) data.get("total");
        int pageCnt = (total > 0) ? (int) Math.ceil((double) total / listSize) : 1;

        req.setAttribute("list",     data.get("rows"));
        req.setAttribute("total",    total);
        req.setAttribute("page",     page);
        req.setAttribute("pageCnt",  pageCnt);
        req.setAttribute("listSize", listSize);
        req.setAttribute("qKey",     qKey);
        req.setAttribute("qWord",    qWord);
        req.setAttribute("qSort",    qSort);
        req.setAttribute("qLL",      qLL);
        req.setAttribute("legalList", dao.getLegalList("Y"));

        req.getRequestDispatcher("/jsp/regulation/regulation_list.jsp")
           .forward(req, resp);
    }

    // ── FORM (등록/수정) ─────────────────────────────────────────────
    private void doForm(HttpServletRequest req, HttpServletResponse resp,
                        RegulationDAO dao) throws Exception {

        int lrIdx = toInt(req.getParameter("lr_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();

        if (lrIdx > 0) {
            action = "MOD";
            info = dao.getInfo(lrIdx);
        }

        // 규제법률 드롭다운
        List<Map<String, Object>> legalList = dao.getLegalList("Y");

        req.setAttribute("action",    action);
        req.setAttribute("info",      info);
        req.setAttribute("lrIdx",     lrIdx);
        req.setAttribute("legalList", legalList);
        // 검색 파라미터 보존
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/regulation/regulation_form.jsp")
           .forward(req, resp);
    }

    // ── VIEW (상세) ──────────────────────────────────────────────────
    private void doView(HttpServletRequest req, HttpServletResponse resp,
                        RegulationDAO dao) throws Exception {

        int lrIdx = toInt(req.getParameter("lr_idx"), 0);
        Map<String, Object> info = lrIdx > 0 ? dao.getInfo(lrIdx) : new LinkedHashMap<>();

        req.setAttribute("info",  info);
        req.setAttribute("lrIdx", lrIdx);
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));

        req.getRequestDispatcher("/jsp/regulation/regulation_view.jsp")
           .forward(req, resp);
    }

    // ── PROC (저장처리) ──────────────────────────────────────────────
    private void doProc(HttpServletRequest req, HttpServletResponse resp,
                        RegulationDAO dao) throws Exception {

        String action       = nvl(req.getParameter("action"),        "ADD");
        int lrIdx           = toInt(req.getParameter("lr_idx"),        0);
        String lrTitle      = nvl(req.getParameter("lr_title"),       "");
        int llIdx           = toInt(req.getParameter("ll_idx"),        0);
        String condition    = nvl(req.getParameter("lr_condition"),   "");
        String certify      = nvl(req.getParameter("lr_certify_guide"),"");
        String penalty      = nvl(req.getParameter("lr_penalty"),     "");
        String isUse        = nvl(req.getParameter("lr_is_use"),      "N");
        String regUser      = nvl((String) req.getSession().getAttribute("loginId"), "");
        String regIp        = nvl(req.getRemoteAddr(),                "");

        Map<String, Object> rtn = dao.putInfo(
                action, lrIdx, lrTitle, llIdx,
                condition, certify, penalty,
                isUse, regUser, regIp);

        String rtnMsg = (String) rtn.get("rtn_msg");
        int    rtnIdx = (int)    rtn.get("rtn_idx");

        String linkParams = buildLinkParams(req);

        if (rtnMsg == null || rtnMsg.isEmpty()) {
            // 성공
            String redirectUrl;
            if ("ADD".equals(action) || "DEL".equals(action)) {
                redirectUrl = req.getContextPath() + "/regulation/?mode=list" + linkParams;
            } else {
                redirectUrl = req.getContextPath() + "/regulation/?mode=form&lr_idx=" + rtnIdx + linkParams;
            }
            resp.sendRedirect(redirectUrl);
        } else {
            // 오류 → Back
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
        for (String k : new String[]{"qKey","qWord","qSort","qLL","page"}) {
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
