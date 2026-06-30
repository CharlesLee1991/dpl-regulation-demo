<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>위해정보DB — 디지털 제품안전 라이브러리</title>
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
<li class="active"><a class="gnb_1dep" href="/front/safety/">안전정보</a><div class="gnb_2dep"><ul><li><a href="/front/safety/">위해정보DB</a></li><li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a><div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/support/">셀프러닝</a><div class="gnb_2dep"><ul><li><a href="/front/support/">숏클래스</a></li><li><a href="/front/support/?tab=info">유용한 정보</a></li><li><a href="/front/support/?tab=video">동영상 정보</a></li><li><a href="/front/support/?tab=safety">안전센터 정보</a></li></ul></div></li>
</ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>
<div id="sub_visual"><div class="visual_title">안전정보</div><p>제품 위해정보를 확인하실 수 있습니다</p></div>
<div id="container">
<aside id="aside"><ul>
  <li><a class="on" href="/front/safety/">위해정보DB</a></li>
  <li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li>
</ul></aside>
  <div id="contents">
    <div id="title_content">
      <div id="title"><span>위해정보DB</span></div>
      <p class="bread"><span>Home</span><span>안전정보</span><span>위해정보DB</span></p>
    </div>
    <form id="frmSearch" method="get" action="/front/safety/">
      <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="149"><col width="*"><col width="100"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>키워드</th>
            <td colspan="3">
              <select name="qKey" id="qKey">
                <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>상품명</option>
                <option value="KEYWORD" <c:if test="${qKey=='KEYWORD'}">selected</c:if>>키워드</option>
                <option value="FACTOR" <c:if test="${qKey=='FACTOR'}">selected</c:if>>위해요인</option>
                <option value="CONTENTS" <c:if test="${qKey=='CONTENTS'}">selected</c:if>>위해내용</option>
              </select>
              <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}">
              <button type="submit" class="btn btn_style_01">검색</button>
              <button type="button" class="btn btn_style_02" onclick="$('#qKey').val('TITLE');$('#qWord').val('');$('#qCate').val('0');$('#qLT').val('0');$('#qLL').val('0');$('#frmSearch').submit()">초기화</button>
            </td>
          </tr>
          <tr>
            <th>카테고리</th>
            <td colspan="3">
              <select name="qCate" id="qCate">
                <option value="0" <c:if test="${empty qCate || qCate=='0'}">selected</c:if>>전체</option>
                <option value="1" <c:if test="${qCate=='1'}">selected</c:if>>패션잡화</option>
                <option value="2" <c:if test="${qCate=='2'}">selected</c:if>>취미·스포츠</option>
                <option value="3" <c:if test="${qCate=='3'}">selected</c:if>>주방용품</option>
                <option value="4" <c:if test="${qCate=='4'}">selected</c:if>>청소·욕실</option>
                <option value="5" <c:if test="${qCate=='5'}">selected</c:if>>출산·유아동</option>
                <option value="6" <c:if test="${qCate=='6'}">selected</c:if>>뷰티·퍼스널케어</option>
                <option value="7" <c:if test="${qCate=='7'}">selected</c:if>>문구·OA</option>
              </select>
              <select name="qCate2" id="qCate2"><option value="0">카테고리 선택</option></select>
              <select name="qCate3" id="qCate3"><option value="0">카테고리 선택</option></select>
            </td>
          </tr>
          <tr>
            <th>위해유형</th>
            <td>
              <select name="qLT" id="qLT">
                <option value="0" <c:if test="${empty qLT || qLT=='0'}">selected</c:if>>선택해 주세요</option>
                <option value="화학적결함" <c:if test="${qLT=='화학적결함'}">selected</c:if>>화학적결함</option>
                <option value="물리적결함" <c:if test="${qLT=='물리적결함'}">selected</c:if>>물리적결함</option>
                <option value="생물학적결함" <c:if test="${qLT=='생물학적결함'}">selected</c:if>>생물학적결함</option>
                <option value="환경적결함" <c:if test="${qLT=='환경적결함'}">selected</c:if>>환경적결함</option>
                <option value="전기적결함" <c:if test="${qLT=='전기적결함'}">selected</c:if>>전기적결함</option>
              </select>
            </td>
            <th>위해등급</th>
            <td>
              <select name="qLL" id="qLL">
                <option value="0" <c:if test="${empty qLL || qLL=='0'}">selected</c:if>>선택해 주세요</option>
                <option value="1" <c:if test="${qLL=='1'}">selected</c:if>>관심</option>
                <option value="2" <c:if test="${qLL=='2'}">selected</c:if>>주의</option>
                <option value="3" <c:if test="${qLL=='3'}">selected</c:if>>경계</option>
                <option value="4" <c:if test="${qLL=='4'}">selected</c:if>>심각</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table></div>
    </form>
    <div class="btn_r" style="text-align:right;margin-bottom:8px">
      <button type="button" class="btn btn_style_03" onclick="alert('엑셀저장은 데모에서 준비 중입니다.')">엑셀저장</button>
      <span style="color:#555;font-size:13px;margin-left:10px">총 <em style="color:#1565c0;font-weight:bold">${total}</em>건</span>
    </div>
    <div class="table_list_style_01">
      <table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="60"><col><col width="110"><col width="130"><col width="110"><col width="130"></colgroup>
        <thead><tr>
          <th>번호</th>
          <th>상품명</th>
          <th>위해유형</th>
          <th>위해요인</th>
          <th>등록일</th>
          <th>정보출처</th>
        </tr></thead>
        <tbody>
          <c:choose>
            <c:when test="${empty list}">
              <tr><td colspan="6" style="text-align:center;padding:30px;color:#999">검색된 결과가 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="row" items="${list}" varStatus="st">
                <tr>
                  <td style="text-align:center">${total-(page-1)*10-st.index}</td>
                  <td class="td_left"><a href="/front/safety/view?rd_idx=${row.rd_idx}">${fn:escapeXml(row.rd_title)}</a></td>
                  <td style="text-align:center">${fn:escapeXml(row.rd_type != null ? row.rd_type : '')}</td>
                  <td style="text-align:center">${fn:escapeXml(row.rd_factor != null ? row.rd_factor : '')}</td>
                  <td style="text-align:center">${row.rd_reg_date}</td>
                  <td style="text-align:center">${fn:escapeXml(row.rd_source != null ? row.rd_source : '')}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
    <div class="paginate_ui">
    <c:if test="${page>1}">
      <a class="first" href="?page=1&qKey=${{qKey}}&qWord=${{fn:escapeXml(qWord)}}&qSort=${{fn:escapeXml(qSort)}}">처음</a>
      <a class="prev" href="?page=${page-1}&qKey=${{qKey}}&qWord=${{fn:escapeXml(qWord)}}&qSort=${{fn:escapeXml(qSort)}}">이전</a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p>=page-4 && p<=page+4}">
        <c:choose>
          <c:when test="${p==page}"><strong>${p}</strong></c:when>
          <c:otherwise><a href="?page=${p}&qKey=${{qKey}}&qWord=${{fn:escapeXml(qWord)}}&qSort=${{fn:escapeXml(qSort)}}">${p}</a></c:otherwise>
        </c:choose>
      </c:if>
    </c:forEach>
    <c:if test="${page<pageCnt}">
      <a class="next" href="?page=${page+1}&qKey=${{qKey}}&qWord=${{fn:escapeXml(qWord)}}&qSort=${{fn:escapeXml(qSort)}}">다음</a>
      <a class="last" href="?page=${pageCnt}&qKey=${{qKey}}&qWord=${{fn:escapeXml(qWord)}}&qSort=${{fn:escapeXml(qSort)}}">마지막</a>
    </c:if>
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