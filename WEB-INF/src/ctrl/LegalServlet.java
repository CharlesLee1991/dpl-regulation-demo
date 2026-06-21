package ctrl;

import mdl.LegalDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * MODULE: regulation_legal (규제법률)
 * URL: /legal/?mode=list|form|proc
 * 원본: _admin/regulations/legal_*.asp
 */
public class LegalServlet extends HttpServlet {

    private static final int LIST_SIZE = 20;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String mode = nvl(req.getParameter("mode"), "list");
        LegalDAO dao = new LegalDAO();

        try {
            switch (mode) {
                case "list": doList(req, resp, dao); break;
                case "form": doForm(req, resp, dao); break;
                default:
                    if ("proc".equals(nvl(req.getParameter("mode"), "")))
                        doProc(req, resp, dao);
                    else doList(req, resp, dao);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException { service(req, resp); }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException { service(req, resp); }

    // ── LIST ─────────────────────────────────────────────────────────
    private void doList(HttpServletRequest req, HttpServletResponse resp,
                        LegalDAO dao) throws Exception {

        int page     = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"),  "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");

        Map<String, Object> data = dao.getListPaging(page, LIST_SIZE, qKey, qWord, qSort);
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

        req.getRequestDispatcher("/jsp/legal/legal_list.jsp").forward(req, resp);
    }

    // ── FORM (등록/수정) ──────────────────────────────────────────────
    private void doForm(HttpServletRequest req, HttpServletResponse resp,
                        LegalDAO dao) throws Exception {

        int llIdx   = toInt(req.getParameter("ll_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();

        if (llIdx > 0) {
            action = "MOD";
            info   = dao.getInfo(llIdx);
        }

        req.setAttribute("action", action);
        req.setAttribute("info",   info);
        req.setAttribute("llIdx",  llIdx);
        req.setAttribute("qKey",   nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord",  nvl(req.getParameter("qWord"), ""));
        req.setAttribute("page",   nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/legal/legal_form.jsp").forward(req, resp);
    }

    // ── PROC (저장/삭제) ──────────────────────────────────────────────
    private void doProc(HttpServletRequest req, HttpServletResponse resp,
                        LegalDAO dao) throws Exception {

        String action  = nvl(req.getParameter("action"), "ADD");
        int    llIdx   = toInt(req.getParameter("ll_idx"), 0);
        String llTitle = nvl(req.getParameter("ll_title"), "");
        String llDept  = nvl(req.getParameter("ll_dept"),  "");
        String llIsUse = nvl(req.getParameter("ll_is_use"), "Y");
        String page    = nvl(req.getParameter("page"), "1");
        String qKey    = nvl(req.getParameter("qKey"),  "");
        String qWord   = nvl(req.getParameter("qWord"), "");

        String[] result = dao.putInfo(action, llIdx, llTitle, llDept, llIsUse, "admin", req.getRemoteAddr());
        String rtnMsg   = result[0];

        String linkParams = "page=" + page
                + (qKey.isEmpty()  ? "" : "&qKey="  + qKey)
                + (qWord.isEmpty() ? "" : "&qWord=" + qWord);

        String redirectUrl;
        if (rtnMsg.isEmpty()) {
            if ("ADD".equals(action) || "DEL".equals(action)) {
                redirectUrl = req.getContextPath() + "/legal/?mode=list&" + linkParams;
            } else {
                redirectUrl = req.getContextPath() + "/legal/?mode=form&ll_idx=" + llIdx + "&" + linkParams;
            }
        } else {
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write(
                "<script>alert('" + rtnMsg.replace("'", "\\'") + "'); history.back();</script>");
            return;
        }
        resp.sendRedirect(redirectUrl);
    }

    // ── 유틸 ─────────────────────────────────────────────────────────
    private static String nvl(String v, String def) { return (v == null || v.isEmpty()) ? def : v.trim(); }
    private static int    toInt(String v, int def)  { try { return Integer.parseInt(nvl(v, "")); } catch (Exception e) { return def; } }
}
