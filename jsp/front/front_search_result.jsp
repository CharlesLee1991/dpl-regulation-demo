<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>통합검색 결과 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/swiper-bundle.min.js"></script>
<script src="/static/js/front_common.js"></script>
</head><body>
<div id="wrap" class="sub">
<div id="header"><header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul><li><a class="gnb_1dep" href="/front/legal/">법규정보</a><div class="gnb_2dep"><ul><li><a href="/front/legal/">법규정보DB</a></li><li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/safety/">안전정보</a><div class="gnb_2dep"><ul><li><a href="/front/safety/">위해정보DB</a></li><li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a><div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/support/">셀프러닝</a><div class="gnb_2dep"><ul><li><a href="/front/support/">숏클래스</a></li><li><a href="/front/support/?tab=info">유용한 정보</a></li><li><a href="/front/support/?tab=video">동영상 정보</a></li><li><a href="/front/support/?tab=safety">안전센터 정보</a></li></ul></div></li>
</ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>

<div id="sub_visual">
  <div class="visual_title" data-target="gnb-text">통합 검색</div>
  <p>제품 안전정보를 통합 검색하세요</p>
</div>
<div id="container">
  <div id="aside" style="display:none"></div>
  <div id="contents" style="width:100%;max-width:1000px;margin:0 auto">
    <div id="title_content">
      <div id="title"><span>통합 검색결과</span></div>
      <p class="bread"><span>Home</span><span>통합검색</span><span>${fn:escapeXml(qWord)}</span></p>
    </div>
    <form id="frmSearch" method="get" action="/front/search/">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="149"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>검색어</th>
            <td>
              <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}" style="width:400px">
              <button type="submit" class="btn btn_style_01">검색</button>
            </td>
          </tr>
        </tbody>
      </table></div>
    </form>

    <div class="search_list_wrap legal" style="margin-top:30px">
      <div class="title-wrap" style="display:flex;justify-content:space-between;align-items:center;border-bottom:2px solid #1565c0;padding-bottom:10px;margin-bottom:15px">
        <h2 style="font-size:18px;color:#1565c0">법규정보 (<span class="div_cnt">${cntLegal}</span>건)</h2>
        <a href="/front/legal/?qWord=${fn:escapeXml(qWord)}" style="font-size:13px;color:#666">더보기 →</a>
      </div>
      <c:choose>
        <c:when test="${empty legalResult}">
          <p style="color:#999;padding:10px 0">해당 검색어로 법규정보를 찾을 수 없습니다.</p>
        </c:when>
        <c:otherwise>
          <ul class="list legal_list" style="list-style:none;padding:0">
            <c:forEach var="row" items="${legalResult}">
              <li style="border-bottom:1px solid #eee;padding:12px 0">
                <a href="/front/legal/view?lr_idx=${row.lr_idx}" style="display:flex;gap:16px;text-decoration:none;color:inherit">
                  <div style="flex:1">
                    <div style="color:#888;font-size:12px;margin-bottom:4px">${fn:escapeXml(row.ll_title != null ? row.ll_title : '')}</div>
                    <div style="font-weight:bold;margin-bottom:4px">${fn:escapeXml(row.lr_title)}</div>
                    <div style="font-size:12px;color:#999">${row.lr_reg_date}</div>
                  </div>
                </a>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="search_list_wrap riskdb" style="margin-top:40px">
      <div class="title-wrap" style="display:flex;justify-content:space-between;align-items:center;border-bottom:2px solid #e53935;padding-bottom:10px;margin-bottom:15px">
        <h2 style="font-size:18px;color:#e53935">위해정보 (<span class="div_cnt">${cntRiskdb}</span>건)</h2>
        <a href="/front/safety/?qWord=${fn:escapeXml(qWord)}" style="font-size:13px;color:#666">더보기 →</a>
      </div>
      <c:choose>
        <c:when test="${empty riskdbResult}">
          <p style="color:#999;padding:10px 0">해당 검색어로 위해정보를 찾을 수 없습니다.</p>
        </c:when>
        <c:otherwise>
          <ul class="list riskdb_list" style="list-style:none;padding:0">
            <c:forEach var="row" items="${riskdbResult}">
              <li style="border-bottom:1px solid #eee;padding:12px 0">
                <a href="/front/safety/view?rd_idx=${row.rd_idx}" style="text-decoration:none;color:inherit">
                  <span style="background:#e53935;color:#fff;font-size:11px;padding:2px 6px;border-radius:3px;margin-right:8px">${fn:escapeXml(row.rd_type != null ? row.rd_type : '')}</span>
                  <strong>${fn:escapeXml(row.rd_title)}</strong>
                  <span style="font-size:12px;color:#999;margin-left:8px">${row.rd_reg_date}</span>
                </a>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="search_list_wrap standard" style="margin-top:40px">
      <div class="title-wrap" style="display:flex;justify-content:space-between;align-items:center;border-bottom:2px solid #2e7d32;padding-bottom:10px;margin-bottom:15px">
        <h2 style="font-size:18px;color:#2e7d32">롯데 스탠다드 (<span class="div_cnt">${cntStandard}</span>건)</h2>
        <a href="/front/standard/?qWord=${fn:escapeXml(qWord)}" style="font-size:13px;color:#666">더보기 →</a>
      </div>
      <c:choose>
        <c:when test="${empty standardResult}">
          <p style="color:#999;padding:10px 0">해당 검색어로 스탠다드를 찾을 수 없습니다.</p>
        </c:when>
        <c:otherwise>
          <ul style="list-style:none;padding:0">
            <c:forEach var="row" items="${standardResult}">
              <li style="border-bottom:1px solid #eee;padding:12px 0">
                <a href="/front/standard/view?st_idx=${row.st_idx}" style="text-decoration:none;color:inherit">
                  <span style="background:#2e7d32;color:#fff;font-size:11px;padding:2px 6px;border-radius:3px;margin-right:8px">${fn:escapeXml(row.st_div != null ? row.st_div : '')}</span>
                  <strong>${fn:escapeXml(row.st_title)}</strong>
                  <span style="font-size:12px;color:#999;margin-left:8px">${fn:escapeXml(row.st_code != null ? row.st_code : '')}</span>
                </a>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>

  </div>
</div>
</div></div>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>