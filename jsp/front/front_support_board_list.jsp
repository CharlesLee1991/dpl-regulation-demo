<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${tabTitle} — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/front_common.js"></script>
</head>
<body>
<div id="wrap" class="sub">
<div id="header"><header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul>
    <li><a class="gnb_1dep" href="/front/legal/">법규정보</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/legal/">법규정보DB</a></li>
        <li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li>
      </ul></div></li>
    <li><a class="gnb_1dep" href="/front/safety/">안전정보</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/safety/">위해정보DB</a></li>
        <li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li>
      </ul></div></li>
    <li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a>
      <div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
    <li class="active"><a class="gnb_1dep" href="/front/support/">셀프러닝</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/support/">숏클래스</a></li>
        <li><a href="/front/support/?tab=info">유용한 정보</a></li>
        <li><a href="/front/support/?tab=video">동영상 정보</a></li>
        <li><a href="/front/support/?tab=safety">안전센터 정보</a></li>
      </ul></div></li>
  </ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>

<div id="sub_visual">
  <div class="visual_title">셀프러닝</div>
  <p>학습자의 니즈에 맞춘 정보 검색과 학습이 가능합니다</p>
</div>

<div id="container">
  <aside id="aside"><ul>
    <li><a href="/front/support/">숏클래스</a></li>
    <li><a class="${tab=='info'?'on':''}" href="/front/support/?tab=info">유용한 정보</a></li>
    <li><a class="${tab=='video'?'on':''}" href="/front/support/?tab=video">동영상 정보</a></li>
    <li><a class="${tab=='safety'?'on':''}" href="/front/support/?tab=safety">안전센터 정보</a></li>
  </ul></aside>

  <div id="contents">
    <div id="title_content">
      <div id="title"><span>${tabTitle}</span></div>
      <p class="bread"><span>Home</span><span>셀프러닝</span><span>${tabTitle}</span></p>
    </div>

    <form id="frmSearch" method="get" action="/front/support/">
      <input type="hidden" name="tab" value="${tab}">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="149"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>키워드</th>
            <td>
              <select name="qKey" id="qKey">
                <option value="" <c:if test="${empty qKey}">selected</c:if>>제목</option>
                <option value="CONT" <c:if test="${qKey=='CONT'}">selected</c:if>>내용</option>
                <option value="COL1" <c:if test="${qKey=='COL1'}">selected</c:if>>정보유형</option>
                <option value="COL2" <c:if test="${qKey=='COL2'}">selected</c:if>>상품유형</option>
              </select>
              <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}">
              <button type="submit" class="btn btn_style_01">검색</button>
              <button type="button" class="btn btn_style_02" onclick="$('#qKey').val('');$('#qWord').val('');$('#frmSearch').submit()">초기화</button>
            </td>
          </tr>
        </tbody>
      </table></div>
    </form>
    <div class="btn_r" style="text-align:right;margin-bottom:8px">
      <span style="color:#555;font-size:13px">총 <em style="color:#1565c0;font-weight:bold">${total}</em>건</span>
    </div>

    <div class="table_list_style_01">
      <table border="0" cellspacing="0" cellpadding="0" style="table-layout:fixed">
        <caption>${tabTitle}</caption>
        <colgroup><col width="70"><col><col width="130"><col width="130"><col width="100"><col width="110"></colgroup>
        <thead><tr>
          <th>번호</th><th>제목</th><th>정보유형</th><th>상품유형</th><th>작성자</th><th>작성일</th>
        </tr></thead>
        <tbody>
          <c:choose>
            <c:when test="${empty list}">
              <tr><td colspan="6" style="text-align:center;padding:40px;color:#999">검색된 내용이 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="row" items="${list}" varStatus="st">
                <tr>
                  <td style="text-align:center">${total-(page-1)*10-st.index}</td>
                  <td class="td_left"><span>${fn:escapeXml(row.bd_title)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.bd_etc_cols_1)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.bd_etc_cols_2)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.bd_writer)}</span></td>
                  <td style="text-align:center"><span>${row.bd_reg_date}</span></td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <div class="paginate_ui">
      <c:if test="${page>1}"><a href="?tab=${tab}&page=${page-1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}">&lt;</a></c:if>
      <c:forEach begin="1" end="${pageCnt}" var="p">
        <c:if test="${p>=page-4 && p<=page+4}">
          <c:choose>
            <c:when test="${p==page}"><strong>${p}</strong></c:when>
            <c:otherwise><a href="?tab=${tab}&page=${p}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}">${p}</a></c:otherwise>
          </c:choose>
        </c:if>
      </c:forEach>
      <c:if test="${page<pageCnt}"><a href="?tab=${tab}&page=${page+1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}">&gt;</a></c:if>
    </div>

  </div>
</div>

</div>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>
