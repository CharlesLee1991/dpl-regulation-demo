<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>제품안전 뉴스 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/front_common.js"></script>
<style>
.label{display:inline-block;min-width:44px;padding:3px 8px;font-size:12px;line-height:1.3;text-align:center;border-radius:12px;color:#fff}
.lv1{background:#2f74c0}.lv2{background:#3aa856}.lv3{background:#e0902f}.lv4{background:#c0563f}
</style>
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
    <li class="active"><a class="gnb_1dep" href="/front/safety/">안전정보</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/safety/">위해정보DB</a></li>
        <li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li>
      </ul></div></li>
    <li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a>
      <div class="gnb_2dep"><ul><li><a href="/front/standard/">품질관리기준</a></li></ul></div></li>
    <li><a class="gnb_1dep" href="/front/support/">셀프러닝</a>
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
  <div class="visual_title">안전정보</div>
  <p>유용한 정보를 안내해 드립니다</p>
</div>

<div id="container">
  <aside id="aside"><ul>
    <li><a href="/front/safety/">위해정보DB</a></li>
    <li><a class="on" href="/front/safety/?tab=news">제품안전 뉴스</a></li>
  </ul></aside>

  <div id="contents">
    <div id="title_content">
      <div id="title"><span>제품안전 뉴스</span></div>
      <p class="bread"><span>Home</span><span>안전정보</span><span>제품안전 뉴스</span></p>
    </div>

    <form id="frmSearch" method="get" action="/front/safety/">
      <input type="hidden" name="tab" value="news">
      <div class="search_table"><table border="0" cellspacing="0" cellpadding="0">
        <colgroup><col width="149"><col width="*"><col width="120"><col width="*"></colgroup>
        <tbody>
          <tr>
            <th>키워드</th>
            <td colspan="3">
              <select name="qKey" id="qKey">
                <option value="" <c:if test="${empty qKey}">selected</c:if>>제목</option>
                <option value="CONT" <c:if test="${qKey=='CONT'}">selected</c:if>>내용</option>
                <option value="URL" <c:if test="${qKey=='URL'}">selected</c:if>>URL</option>
                <option value="SOURCE" <c:if test="${qKey=='SOURCE'}">selected</c:if>>정보출처</option>
                <option value="SUMMARY" <c:if test="${qKey=='SUMMARY'}">selected</c:if>>요약</option>
              </select>
              <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}">
              <button type="submit" class="btn btn_style_01">검색</button>
              <button type="button" class="btn btn_style_02" onclick="$('#qKey').val('');$('#qWord').val('');$('#qC3').val('0');$('#qC4').val('0');$('#frmSearch').submit()">초기화</button>
            </td>
          </tr>
          <tr>
            <th>정보구분</th>
            <td>
              <select name="qC3" id="qC3">
                <option value="0" <c:if test="${empty qC3 || qC3=='0'}">selected</c:if>>선택해 주세요</option>
                <option value="1" <c:if test="${qC3=='1'}">selected</c:if>>일반 안전정보</option>
                <option value="2" <c:if test="${qC3=='2'}">selected</c:if>>상품 위해정보</option>
              </select>
            </td>
            <th>중요도등급</th>
            <td>
              <select name="qC4" id="qC4">
                <option value="0" <c:if test="${empty qC4 || qC4=='0'}">selected</c:if>>선택해 주세요</option>
                <option value="1" <c:if test="${qC4=='1'}">selected</c:if>>관심</option>
                <option value="2" <c:if test="${qC4=='2'}">selected</c:if>>주의</option>
                <option value="3" <c:if test="${qC4=='3'}">selected</c:if>>경계</option>
                <option value="4" <c:if test="${qC4=='4'}">selected</c:if>>심각</option>
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
      <table border="0" cellspacing="0" cellpadding="0" style="table-layout:fixed">
        <caption>제품안전 뉴스</caption>
        <colgroup><col width="70"><col width="120"><col width="90"><col><col width="110"><col width="130"></colgroup>
        <thead><tr>
          <th>번호</th><th>정보분류</th><th>중요도</th><th>제목</th><th>등록일</th><th>정보출처</th>
        </tr></thead>
        <tbody>
          <c:choose>
            <c:when test="${empty list}">
              <tr><td colspan="6" style="text-align:center;padding:40px;color:#999">검색된 결과가 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="row" items="${list}" varStatus="st">
                <tr>
                  <td style="text-align:center">${total-(page-1)*10-st.index}</td>
                  <td style="text-align:center">
                    <c:choose>
                      <c:when test="${row.ls_cols_03==2}">상품위해정보</c:when>
                      <c:otherwise>일반안전정보</c:otherwise>
                    </c:choose>
                  </td>
                  <td style="text-align:center">
                    <c:choose>
                      <c:when test="${row.ls_cols_04==1}"><span class="label lv1">관심</span></c:when>
                      <c:when test="${row.ls_cols_04==2}"><span class="label lv2">주의</span></c:when>
                      <c:when test="${row.ls_cols_04==3}"><span class="label lv3">경계</span></c:when>
                      <c:when test="${row.ls_cols_04==4}"><span class="label lv4">심각</span></c:when>
                    </c:choose>
                  </td>
                  <td class="td_left"><span>${fn:escapeXml(row.ls_title)}</span></td>
                  <td style="text-align:center"><span>${row.ls_reg_date}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.ls_cols_02)}</span></td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <div class="paginate_ui">
      <c:if test="${page>1}"><a href="?tab=news&page=${page-1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC3=${qC3}&qC4=${qC4}">&lt;</a></c:if>
      <c:forEach begin="1" end="${pageCnt}" var="p">
        <c:if test="${p>=page-4 && p<=page+4}">
          <c:choose>
            <c:when test="${p==page}"><strong>${p}</strong></c:when>
            <c:otherwise><a href="?tab=news&page=${p}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC3=${qC3}&qC4=${qC4}">${p}</a></c:otherwise>
          </c:choose>
        </c:if>
      </c:forEach>
      <c:if test="${page<pageCnt}"><a href="?tab=news&page=${page+1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC3=${qC3}&qC4=${qC4}">&gt;</a></c:if>
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
