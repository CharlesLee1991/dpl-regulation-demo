package ctrl;

import mdl.SafetyDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * MODULE: safety (안전요건)
 * 원본: _admin/regulations/safety_*.asp
 * URL: /safety/?mode=list|form|proc
 */
public class SafetyServlet extends HttpServlet {

    private static final int LIST_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String mode = nvl(req.getParameter("mode"), "list");
        SafetyDAO dao = new SafetyDAO();
        try {
            switch (mode) {
                case "list": doList(req, resp, dao); break;
                case "form": doForm(req, resp, dao); break;
                default:     doList(req, resp, dao);
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if ("proc".equals(nvl(req.getParameter("mode"), ""))) {
            try { doProc(req, resp, new SafetyDAO()); }
            catch (Exception e) { throw new ServletException(e); }
        } else { doGet(req, resp); }
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp,
                        SafetyDAO dao) throws Exception {
        int page     = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int qLL = toInt(req.getParameter("qLL"), 0);
        int qLR = toInt(req.getParameter("qLR"), 0);
        int qLN = toInt(req.getParameter("qLN"), 0);

        Map<String, Object> data = dao.getListPaging(page, LIST_SIZE, qKey, qWord, qLL, qLR, qLN, qSort);
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
        req.setAttribute("qLL", qLL); req.setAttribute("qLR", qLR); req.setAttribute("qLN", qLN);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (qLL > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", qLL));
        if (qLR > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", qLR));

        req.getRequestDispatcher("/jsp/safety/safety_list.jsp").forward(req, resp);
    }

    private void doForm(HttpServletRequest req, HttpServletResponse resp,
                        SafetyDAO dao) throws Exception {
        int lsIdx = toInt(req.getParameter("ls_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();
        if (lsIdx > 0) { action = "MOD"; info = dao.getInfo(lsIdx); }

        int llIdx = toInt(info.get("ll_idx") != null ? info.get("ll_idx").toString() : "0", 0);
        int lrIdx = toInt(info.get("lr_idx") != null ? info.get("lr_idx").toString() :
                          nvl(req.getParameter("lr_idx"), "0"), 0);
        int lnIdx = toInt(info.get("ln_idx") != null ? info.get("ln_idx").toString() :
                          nvl(req.getParameter("ln_idx"), "0"), 0);

        req.setAttribute("action", action);
        req.setAttribute("info", info);
        req.setAttribute("lsIdx", lsIdx);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (llIdx > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", llIdx));
        if (lrIdx > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", lrIdx));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));
        req.setAttribute("qLR",   nvl(req.getParameter("qLR"),   "0"));
        req.setAttribute("qLN",   nvl(req.getParameter("qLN"),   "0"));
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/safety/safety_form.jsp").forward(req, resp);
    }

    private void doProc(HttpServletRequest req, HttpServletResponse resp,
                        SafetyDAO dao) throws Exception {
        String action  = nvl(req.getParameter("action"),    "ADD");
        int    lsIdx   = toInt(req.getParameter("ls_idx"),   0);
        int    lnIdx   = toInt(req.getParameter("ln_idx"),   0);
        String title   = nvl(req.getParameter("ls_title"),   "");
        String content = nvl(req.getParameter("ls_content"), "");
        String isUse   = nvl(req.getParameter("ls_is_use"), "N");
        String regUser = nvl((String) req.getSession().getAttribute("loginId"), "");
        String regIp   = nvl(req.getRemoteAddr(), "");

        Map<String, Object> rtn = dao.putInfo(action, lsIdx, lnIdx, title, content, isUse, regUser, regIp);
        String rtnMsg = (String) rtn.get("rtn_msg");
        int    rtnIdx = (int)    rtn.get("rtn_idx");
        String linkParams = buildLinkParams(req);

        if (rtnMsg == null || rtnMsg.isEmpty()) {
            String url = "ADD".equals(action) || "DEL".equals(action)
                ? req.getContextPath() + "/safety/?mode=list" + linkParams
                : req.getContextPath() + "/safety/?mode=form&ls_idx=" + rtnIdx + linkParams;
            resp.sendRedirect(url);
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>alert('" + rtnMsg.replace("'", "\\'") + "');history.back();</script>");
        }
    }

    private String buildLinkParams(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        for (String k : new String[]{"qKey","qWord","qSort","qLL","qLR","qLN","page"}) {
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
