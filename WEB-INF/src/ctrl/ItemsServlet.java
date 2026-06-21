package ctrl;

import mdl.ItemsDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * MODULE: items — 품목정보(마스터) + 세부품목(디테일)
 * 원본: item_def_*.asp + item_detail_*.asp
 * URL: /items_def/?mode=list|form|proc
 *      /items_detail/?mode=list|form|proc
 */
public class ItemsServlet extends HttpServlet {

    private static final int LIST_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 경로로 마스터/디테일 구분
        String uri = req.getRequestURI();
        boolean isDetail = uri.contains("items_detail");
        String mode = nvl(req.getParameter("mode"), "list");
        ItemsDAO dao = new ItemsDAO();

        try {
            if (isDetail) {
                switch (mode) {
                    case "list": doDetailList(req, resp, dao); break;
                    case "form": doDetailForm(req, resp, dao); break;
                    default:     doDetailList(req, resp, dao);
                }
            } else {
                switch (mode) {
                    case "list": doMasterList(req, resp, dao); break;
                    case "form": doMasterForm(req, resp, dao); break;
                    default:     doMasterList(req, resp, dao);
                }
            }
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String uri = req.getRequestURI();
        boolean isDetail = uri.contains("items_detail");
        if ("proc".equals(nvl(req.getParameter("mode"), ""))) {
            try {
                if (isDetail) doDetailProc(req, resp, new ItemsDAO());
                else          doMasterProc(req, resp, new ItemsDAO());
            } catch (Exception e) { throw new ServletException(e); }
        } else { doGet(req, resp); }
    }

    // ── 마스터 LIST ──────────────────────────────────────────────────
    private void doMasterList(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
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

        req.setAttribute("list", data.get("rows")); req.setAttribute("total", total);
        req.setAttribute("page", page); req.setAttribute("pageCnt", pageCnt);
        req.setAttribute("listSize", LIST_SIZE);
        req.setAttribute("qKey", qKey); req.setAttribute("qWord", qWord); req.setAttribute("qSort", qSort);
        req.setAttribute("qLL", qLL); req.setAttribute("qLR", qLR); req.setAttribute("qLN", qLN);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (qLL > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", qLL));
        if (qLR > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", qLR));

        req.getRequestDispatcher("/jsp/items_def/items_def_list.jsp").forward(req, resp);
    }

    // ── 마스터 FORM ──────────────────────────────────────────────────
    private void doMasterForm(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
        int liIdx  = toInt(req.getParameter("li_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();
        if (liIdx > 0) { action = "MOD"; info = dao.getInfo(liIdx); }

        int llIdx = toInt(info.get("ll_idx") != null ? info.get("ll_idx").toString() : "0", 0);
        int lrIdx = toInt(info.get("lr_idx") != null ? info.get("lr_idx").toString() : "0", 0);
        int lnIdx = toInt(info.get("ln_idx") != null ? info.get("ln_idx").toString() : "0", 0);

        req.setAttribute("action", action); req.setAttribute("info", info); req.setAttribute("liIdx", liIdx);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (llIdx > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", llIdx));
        if (lrIdx > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", lrIdx));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));
        req.setAttribute("qLR",   nvl(req.getParameter("qLR"),   "0"));
        req.setAttribute("qLN",   nvl(req.getParameter("qLN"),   "0"));
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/items_def/items_def_form.jsp").forward(req, resp);
    }

    // ── 마스터 PROC ──────────────────────────────────────────────────
    private void doMasterProc(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
        String action    = nvl(req.getParameter("action"),       "ADD");
        int    liIdx     = toInt(req.getParameter("li_idx"),      0);
        String legalName = nvl(req.getParameter("li_legal_name"), "");
        int    lnIdx     = toInt(req.getParameter("ln_idx"),      0);
        String scope     = nvl(req.getParameter("li_scope"),      "");
        String exception = nvl(req.getParameter("li_exception"),  "");
        int    loIdx     = toInt(req.getParameter("lo_idx"),      0);
        int    lgIdx     = toInt(req.getParameter("lg_idx"),      0);
        String isUse     = nvl(req.getParameter("li_is_use"),     "N");
        String lsIdxs   = nvl(req.getParameter("ls_idxs"),       "");
        String regUser   = nvl((String) req.getSession().getAttribute("loginId"), "");
        String regIp     = nvl(req.getRemoteAddr(), "");

        Map<String, Object> rtn = dao.putInfo(action, liIdx, legalName, lnIdx,
                scope, exception, loIdx, lgIdx, isUse, lsIdxs, regUser, regIp);
        redirect(req, resp, rtn, "items_def", "li_idx", buildLinkParams(req, "qLL","qLR","qLN","qKey","qWord","qSort","page"));
    }

    // ── 디테일 LIST ──────────────────────────────────────────────────
    private void doDetailList(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
        int page     = toInt(req.getParameter("page"), 1);
        String qKey  = nvl(req.getParameter("qKey"), "");
        String qWord = nvl(req.getParameter("qWord"), "");
        String qSort = nvl(req.getParameter("qSort"), "");
        int qLL = toInt(req.getParameter("qLL"), 0);
        int qLR = toInt(req.getParameter("qLR"), 0);
        int qLN = toInt(req.getParameter("qLN"), 0);

        Map<String, Object> data = dao.getDetailListPaging(page, LIST_SIZE, qKey, qWord, qSort, qLL, qLR, qLN);
        int total   = (int) data.get("total");
        int pageCnt = (total > 0) ? (int) Math.ceil((double) total / LIST_SIZE) : 1;

        req.setAttribute("list", data.get("rows")); req.setAttribute("total", total);
        req.setAttribute("page", page); req.setAttribute("pageCnt", pageCnt);
        req.setAttribute("listSize", LIST_SIZE);
        req.setAttribute("qKey", qKey); req.setAttribute("qWord", qWord); req.setAttribute("qSort", qSort);
        req.setAttribute("qLL", qLL); req.setAttribute("qLR", qLR); req.setAttribute("qLN", qLN);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (qLL > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", qLL));
        if (qLR > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", qLR));

        req.getRequestDispatcher("/jsp/items_detail/items_detail_list.jsp").forward(req, resp);
    }

    // ── 디테일 FORM ──────────────────────────────────────────────────
    private void doDetailForm(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
        int ldIdx  = toInt(req.getParameter("ld_idx"), 0);
        String action = "ADD";
        Map<String, Object> info = new LinkedHashMap<>();
        if (ldIdx > 0) { action = "MOD"; info = dao.getDetailInfo(ldIdx); }

        int llIdx = toInt(info.get("ll_idx") != null ? info.get("ll_idx").toString() : "0", 0);
        int lrIdx = toInt(info.get("lr_idx") != null ? info.get("lr_idx").toString() : "0", 0);
        int lnIdx = toInt(info.get("ln_idx") != null ? info.get("ln_idx").toString() : "0", 0);

        req.setAttribute("action", action); req.setAttribute("info", info); req.setAttribute("ldIdx", ldIdx);
        req.setAttribute("legalList", dao.getLegalList("Y"));
        if (llIdx > 0) req.setAttribute("regulationList", dao.getRegulationList("Y", llIdx));
        if (lrIdx > 0) req.setAttribute("notifyList", dao.getNotifyList("Y", lrIdx));
        if (lnIdx > 0) req.setAttribute("itemsList", dao.getItemsList("Y", lnIdx, 0));
        req.setAttribute("qKey",  nvl(req.getParameter("qKey"),  ""));
        req.setAttribute("qWord", nvl(req.getParameter("qWord"), ""));
        req.setAttribute("qLL",   nvl(req.getParameter("qLL"),   "0"));
        req.setAttribute("qLR",   nvl(req.getParameter("qLR"),   "0"));
        req.setAttribute("qLN",   nvl(req.getParameter("qLN"),   "0"));
        req.setAttribute("page",  nvl(req.getParameter("page"),  "1"));

        req.getRequestDispatcher("/jsp/items_detail/items_detail_form.jsp").forward(req, resp);
    }

    // ── 디테일 PROC ──────────────────────────────────────────────────
    private void doDetailProc(HttpServletRequest req, HttpServletResponse resp,
                               ItemsDAO dao) throws Exception {
        String action   = nvl(req.getParameter("action"),       "ADD");
        int    ldIdx    = toInt(req.getParameter("ld_idx"),      0);
        String itemName = nvl(req.getParameter("ld_item_name"),  "");
        int    liIdx    = toInt(req.getParameter("li_idx"),      0);
        String useAge   = nvl(req.getParameter("ld_use_age"),    "");
        String material = nvl(req.getParameter("ld_material"),   "");
        String isUse    = nvl(req.getParameter("ld_is_use"),     "N");
        String regUser  = nvl((String) req.getSession().getAttribute("loginId"), "");
        String regIp    = nvl(req.getRemoteAddr(), "");

        Map<String, Object> rtn = dao.putDetailInfo(action, ldIdx, itemName, liIdx,
                useAge, material, isUse, regUser, regIp);
        redirect(req, resp, rtn, "items_detail", "ld_idx", buildLinkParams(req, "qLL","qLR","qLN","qKey","qWord","qSort","page"));
    }

    // ── 공통 리다이렉트 ───────────────────────────────────────────────
    private void redirect(HttpServletRequest req, HttpServletResponse resp,
                          Map<String, Object> rtn, String module, String idxParam,
                          String linkParams) throws Exception {
        String rtnMsg = (String) rtn.get("rtn_msg");
        int    rtnIdx = (int)    rtn.get("rtn_idx");
        String action = nvl(req.getParameter("action"), "ADD");

        if (rtnMsg == null || rtnMsg.isEmpty()) {
            String url = "ADD".equals(action) || "DEL".equals(action)
                ? req.getContextPath() + "/" + module + "/?mode=list" + linkParams
                : req.getContextPath() + "/" + module + "/?mode=form&" + idxParam + "=" + rtnIdx + linkParams;
            resp.sendRedirect(url);
        } else {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().println("<script>alert('" + rtnMsg.replace("'","\\'") + "');history.back();</script>");
        }
    }

    private String buildLinkParams(HttpServletRequest req, String... keys) {
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
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
