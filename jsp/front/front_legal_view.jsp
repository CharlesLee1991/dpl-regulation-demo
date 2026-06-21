<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko">
<head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>법규정보 상세 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/swiper-bundle.min.js"></script>
<script src="/static/js/jquery.easing.min.js"></script>
<script src="/static/js/jquery.rollingCounter.min.js"></script>
<script src="/static/js/front_common.js"></script>
</head>
<body>
<div id="wrap" class="sub">
<div id="header">
<header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul><li class="active"><a class="gnb_1dep" href="/front/legal/">법규정보</a><div class="gnb_2dep"><ul><li><a href="/front/legal/">법규정보DB</a></li><li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/safety/">안전정보</a><div class="gnb_2dep"><ul><li><a href="/front/safety/">위해정보DB</a></li><li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a><div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/support/">셀프러닝</a><div class="gnb_2dep"><ul><li><a href="/front/support/">숏클래스</a></li><li><a href="/front/support/?tab=info">유용한 정보</a></li><li><a href="/front/support/?tab=video">동영상 정보</a></li><li><a href="/front/support/?tab=safety">안전센터 정보</a></li></ul></div></li>
</ul></nav>
  <div class="right_link">
    <p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p>
  </div>
</header>
<div class="bg_nav"></div>
</div>
<div id="sub_visual">
  <div class="visual_title">법규정보</div>
  <p>최신 법규정보를 확인하실 수 있습니다</p>
</div>
<div id="container">
  <aside id="aside">
    <ul>
      <li><a class="on" href="/front/legal/">법규정보DB</a></li>
      <li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li>
    </ul>
  </aside>
  <div id="contents">
    <div id="title_content">
      <div id="title"><span>법규정보DB</span></div>
      <p class="bread"><span>Home</span><span>법규정보</span><span>법규정보DB</span></p>
    </div>

    <c:if test="${not empty info}">
      <div class="view_header">
        <h3 class="view_title">${fn:escapeXml(info.lr_title)}</h3>
        <div class="view_info">
          <span class="cate">${fn:escapeXml(info.ll_title != null ? info.ll_title : '')}</span>
          <span class="date">${info.lr_reg_date}</span>
        </div>
      </div>
      <div class="view_content">
        <table class="view_table" border="0" cellspacing="0" cellpadding="0">
          <colgroup><col width="160"><col width="*"></colgroup>
          <tbody>
            <c:if test="${not empty info.lr_condition}">
              <tr>
                <th>사전이행요건</th>
                <td>${fn:escapeXml(info.lr_condition)}</td>
              </tr>
            </c:if>
            <c:if test="${not empty info.lr_certify_guide}">
              <tr>
                <th>인증표시방법</th>
                <td>${fn:escapeXml(info.lr_certify_guide)}</td>
              </tr>
            </c:if>
            <c:if test="${not empty info.lr_penalty}">
              <tr>
                <th>벌칙</th>
                <td>${fn:escapeXml(info.lr_penalty)}</td>
              </tr>
            </c:if>
            <c:if test="${not empty relatedNotify}">
              <tr>
                <th>관련 고시(부속서)</th>
                <td>
                  <ul class="related_list">
                    <c:forEach var="n" items="${relatedNotify}">
                      <li>${fn:escapeXml(n.ln_title)}</li>
                    </c:forEach>
                  </ul>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
      <div class="btn_area">
        <a href="/front/legal/?qLL=${info.ll_idx}" class="btn btn_style_02">목록으로</a>
      </div>
    </c:if>
    <c:if test="${empty info}">
      <div class="no_result"><p>해당 정보를 찾을 수 없습니다.</p></div>
    </c:if>
  </div>
</div>

</div><%-- /wrap --%>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>