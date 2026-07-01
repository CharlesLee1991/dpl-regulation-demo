<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>숏클래스 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/swiper-bundle.min.js"></script>
<script src="/static/js/front_common.js"></script>
<style>
.btn_tab_group{display:inline-flex;gap:6px}
.btn_tab{min-width:80px;padding:7px 16px;border:1px solid #2f74c0;background:#fff;color:#2f74c0;border-radius:4px;cursor:pointer;font-size:14px}
.btn_tab.on{background:#2f74c0;color:#fff;font-weight:bold}
</style>
</head><body>
<div id="wrap" class="sub">
<div id="header"><header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul><li><a class="gnb_1dep" href="/front/legal/">법규정보</a><div class="gnb_2dep"><ul><li><a href="/front/legal/">법규정보DB</a></li><li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/safety/">안전정보</a><div class="gnb_2dep"><ul><li><a href="/front/safety/">위해정보DB</a></li><li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li></ul></div></li>
<li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a><div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
<li class="active"><a class="gnb_1dep" href="/front/support/">셀프러닝</a><div class="gnb_2dep"><ul><li><a href="/front/support/">숏클래스</a></li><li><a href="/front/support/?tab=info">유용한 정보</a></li><li><a href="/front/support/?tab=video">동영상 정보</a></li><li><a href="/front/support/?tab=safety">안전센터 정보</a></li></ul></div></li>
</ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>
<div id="sub_visual"><div class="visual_title">셀프러닝</div><p>제품 안전정보 관련 숏클래스를 이용하세요</p></div>
<div id="container">
<aside id="aside"><ul>
  <li><a class="on" href="/front/support/">숏클래스</a></li>
  <li><a href="/front/support/?tab=info">유용한 정보</a></li>
  <li><a href="/front/support/?tab=video">동영상 정보</a></li>
  <li><a href="/front/support/?tab=safety">안전센터 정보</a></li>
</ul></aside>
  <div id="contents">
    <div id="title_content">
      <div id="title"><span>숏클래스</span></div>
      <p class="bread"><span>Home</span><span>셀프러닝</span><span>숏클래스</span></p>
    </div>
    <c:if test="${not empty featured}">
      <div class="recomm_wrap">
        <h3 class="sub_title">추천클래스</h3>
        <ul class="image_list_style_01" style="display:grid;grid-template-columns:repeat(4,1fr);gap:20px;list-style:none;padding:0;margin-bottom:30px">
          <c:forEach var="row" items="${featured}" varStatus="st">
            <c:if test="${st.index < 4}">
              <li>
                <div class="slidebox">
                  <a href="/front/support/view?sc_idx=${row.sc_idx}">
                    <div class="video" style="background:#1a237e;height:120px;display:flex;align-items:center;justify-content:center;border-radius:6px">
                      <span style="color:#fff;font-size:28px">▶</span>
                    </div>
                    <div class="text" style="padding:10px 0">
                      <em class="tit" style="font-weight:bold;display:block;margin-bottom:6px">${fn:escapeXml(row.sc_title)}</em>
                      <p class="txt" style="color:#777;font-size:12px;overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">
                        ${fn:escapeXml(row.sc_desc != null ? row.sc_desc : '')}
                      </p>
                    </div>
                  </a>
                </div>
              </li>
            </c:if>
          </c:forEach>
        </ul>
      </div>
    </c:if>
    <form id="frmSearch" method="get" action="/front/support/">
      <input type="hidden" name="qField" id="qField" value="${fn:escapeXml(qField)}">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="149"><col width="*"><col width="100"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>키워드</th>
            <td colspan="3">
              <input type="text" name="qWord" value="${fn:escapeXml(qWord)}">
              <button type="submit" class="btn btn_style_01">검색</button>
              <button type="button" class="btn btn_style_02" onclick="$('#qField').val('');$('#qWord').val('');$('#qLL').val('0');$('#qCate').val('0');$('#frmSearch').submit()">초기화</button>
            </td>
          </tr>
          <tr>
            <th>교육분야</th>
            <td colspan="3">
              <div class="btn_tab_group">
                <button type="button" class="btn_tab ${empty qField?'on':''}" onclick="$('#qField').val('');$('#frmSearch').submit()">전체</button>
                <button type="button" class="btn_tab ${qField=='법규'?'on':''}" onclick="$('#qField').val('법규');$('#frmSearch').submit()">법규</button>
                <button type="button" class="btn_tab ${qField=='시스템'?'on':''}" onclick="$('#qField').val('시스템');$('#frmSearch').submit()">시스템</button>
                <button type="button" class="btn_tab ${qField=='공통'?'on':''}" onclick="$('#qField').val('공통');$('#frmSearch').submit()">공통</button>
              </div>
            </td>
          </tr>
          <tr>
            <th>법령</th>
            <td colspan="3">
              <select name="qLL" id="qLL">
                <option value="0" <c:if test="${empty qLL || qLL=='0'}">selected</c:if>>전체</option>
                <option value="1" <c:if test="${qLL=='1'}">selected</c:if>>대외무역법</option>
                <option value="2" <c:if test="${qLL=='2'}">selected</c:if>>생활화학제품 및 살생물제의 안전관리에 관한 법률</option>
                <option value="3" <c:if test="${qLL=='3'}">selected</c:if>>식품위생법</option>
                <option value="4" <c:if test="${qLL=='4'}">selected</c:if>>약사법</option>
                <option value="5" <c:if test="${qLL=='5'}">selected</c:if>>어린이제품안전특별법</option>
                <option value="6" <c:if test="${qLL=='6'}">selected</c:if>>위생용품관리법</option>
                <option value="7" <c:if test="${qLL=='7'}">selected</c:if>>전기용품 및 생활용품안전관리법</option>
              </select>
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
        </tbody>
      </table></div>
    </form>
    <ul class="image_list_style_01" style="display:grid;grid-template-columns:repeat(4,1fr);gap:20px;list-style:none;padding:0;margin-top:20px">
      <c:choose>
        <c:when test="${empty list}">
          <li style="grid-column:1/-1;text-align:center;padding:40px;color:#999">검색된 결과가 없습니다.</li>
        </c:when>
        <c:otherwise>
          <c:forEach var="row" items="${list}">
            <li>
              <div class="slidebox">
                <a href="/front/support/view?sc_idx=${row.sc_idx}">
                  <div class="video" style="background:#0d47a1;height:120px;display:flex;align-items:center;justify-content:center;border-radius:6px;margin-bottom:8px">
                    <span style="color:#fff;font-size:24px">▶</span>
                  </div>
                  <div class="text">
                    <em class="tit" style="font-weight:bold;display:block;margin-bottom:4px;font-size:14px">${fn:escapeXml(row.sc_title)}</em>
                    <p class="txt" style="color:#777;font-size:12px;overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">
                      ${fn:escapeXml(row.sc_desc != null ? row.sc_desc : '')}
                    </p>
                  </div>
                </a>
              </div>
            </li>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </ul>
    <div class="paginate_ui">
    <c:if test="${page>1}">
      <a class="first" href="?page=1&qWord=${fn:escapeXml(qWord)}&qField=${fn:escapeXml(qField)}&qLL=${fn:escapeXml(qLL)}&qCate=${fn:escapeXml(qCate)}">처음</a>
      <a class="prev" href="?page=${page-1}&qWord=${fn:escapeXml(qWord)}&qField=${fn:escapeXml(qField)}&qLL=${fn:escapeXml(qLL)}&qCate=${fn:escapeXml(qCate)}">이전</a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p>=page-4 && p<=page+4}">
        <c:choose>
          <c:when test="${p==page}"><strong>${p}</strong></c:when>
          <c:otherwise><a href="?page=${p}&qWord=${fn:escapeXml(qWord)}&qField=${fn:escapeXml(qField)}&qLL=${fn:escapeXml(qLL)}&qCate=${fn:escapeXml(qCate)}">${p}</a></c:otherwise>
        </c:choose>
      </c:if>
    </c:forEach>
    <c:if test="${page<pageCnt}">
      <a class="next" href="?page=${page+1}&qWord=${fn:escapeXml(qWord)}&qField=${fn:escapeXml(qField)}&qLL=${fn:escapeXml(qLL)}&qCate=${fn:escapeXml(qCate)}">다음</a>
      <a class="last" href="?page=${pageCnt}&qWord=${fn:escapeXml(qWord)}&qField=${fn:escapeXml(qField)}&qLL=${fn:escapeXml(qLL)}&qCate=${fn:escapeXml(qCate)}">마지막</a>
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