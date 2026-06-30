<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>법규 제·개정 정보 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/front_common.js"></script>
<style>
.label{display:inline-block;min-width:54px;padding:3px 6px;margin:1px 0;font-size:12px;line-height:1.3;text-align:center;border-radius:3px;color:#fff}
.label.t_admin{background:#3aa856}   /* 행정예고 */
.label.t_fix{background:#2f74c0}     /* 확정고시 */
.label.t_law{background:#7d5fc4}     /* 입법예고 */
.label.t_pub{background:#c0563f}     /* 법률공포 */
.label.t_item{background:#eef1f4;color:#555}  /* 품목 */
</style>
<script>
function jfReset(){
  $("#qWord").val(""); $("#qKey").val(""); $("#qC7").val(""); $("#frmSearch").submit();
}
</script>
</head>
<body>
<div id="wrap" class="sub">
<div id="header"><header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul>
    <li class="active"><a class="gnb_1dep" href="/front/legal/">법규정보</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/legal/">법규정보DB</a></li>
        <li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li>
      </ul></div>
    </li>
    <li><a class="gnb_1dep" href="/front/safety/">안전정보</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/safety/">위해정보DB</a></li>
        <li><a href="/front/safety/?tab=news">제품안전 뉴스</a></li>
      </ul></div>
    </li>
    <li><a class="gnb_1dep" href="/front/standard/">롯데 스탠다드</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/standard/">품질관리기준</a></li>
      </ul></div>
    </li>
    <li><a class="gnb_1dep" href="/front/support/">셀프러닝</a>
      <div class="gnb_2dep"><ul>
        <li><a href="/front/support/">숏클래스</a></li>
        <li><a href="/front/support/?tab=info">유용한 정보</a></li>
        <li><a href="/front/support/?tab=video">동영상 정보</a></li>
        <li><a href="/front/support/?tab=safety">안전센터 정보</a></li>
      </ul></div>
    </li>
  </ul></nav>
  <div class="right_link"><p class="btn_admin"><a href="/legal/?mode=list">관리자</a></p></div>
</header><div class="bg_nav"></div></div>

<div id="sub_visual">
  <div class="visual_title">법규정보</div>
  <p>최신 법규정보를 확인하실 수 있습니다</p>
</div>

<div id="container">
  <aside id="aside">
    <ul>
      <li><a href="/front/legal/">법규정보DB</a></li>
      <li><a class="on" href="/front/legal/?tab=revise">법규 제·개정 정보</a></li>
    </ul>
  </aside>

  <div id="contents">
    <div id="title_content">
      <div id="title"><span>법규 제·개정 정보</span></div>
      <p class="bread"><span>Home</span><span>법규정보</span><span>법규 제·개정 정보</span></p>
    </div>

    <%-- 검색폼 --%>
    <form id="frmSearch" method="get" action="/front/legal/">
      <input type="hidden" name="tab" value="revise">
      <div class="search_table">
        <table border="0" cellspacing="0" cellpadding="0">
          <colgroup><col width="149"><col width="*"></colgroup>
          <tbody>
            <tr>
              <th>키워드</th>
              <td>
                <select name="qKey" id="qKey">
                  <option value="" <c:if test="${empty qKey}">selected</c:if>>전체</option>
                  <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>제목</option>
                  <option value="CONT"  <c:if test="${qKey=='CONT'}">selected</c:if>>내용</option>
                  <option value="COL1"  <c:if test="${qKey=='COL1'}">selected</c:if>>고시번호</option>
                  <option value="COL2"  <c:if test="${qKey=='COL2'}">selected</c:if>>고시명</option>
                  <option value="COL5"  <c:if test="${qKey=='COL5'}">selected</c:if>>담당부처</option>
                  <option value="COL8"  <c:if test="${qKey=='COL8'}">selected</c:if>>품목</option>
                </select>
                <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}">
                <button type="submit" class="btn btn_style_01">검색</button>
                <button type="button" class="btn btn_style_02" onclick="jfReset()">초기화</button>
              </td>
            </tr>
            <tr>
              <th>고시유형</th>
              <td>
                <select name="qC7" id="qC7">
                  <option value="" <c:if test="${empty qC7}">selected</c:if>>선택해주세요</option>
                  <option value="행정예고" <c:if test="${qC7=='행정예고'}">selected</c:if>>행정예고</option>
                  <option value="확정고시" <c:if test="${qC7=='확정고시'}">selected</c:if>>확정고시</option>
                  <option value="입법예고" <c:if test="${qC7=='입법예고'}">selected</c:if>>입법예고</option>
                  <option value="법률공포" <c:if test="${qC7=='법률공포'}">selected</c:if>>법률공포</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </form>

    <%-- 목록 테이블 --%>
    <div class="table_list_style_01">
      <table border="0" cellspacing="0" cellpadding="0" style="table-layout:fixed">
        <caption>법규 제·개정 정보</caption>
        <colgroup>
          <col width="70"><col width="110"><col><col width="110"><col width="110"><col width="150">
        </colgroup>
        <thead>
          <tr>
            <th>번호</th>
            <th>유형</th>
            <th>고시제목</th>
            <th>고시일자</th>
            <th>시행일자</th>
            <th>담당부처</th>
          </tr>
        </thead>
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
                      <c:when test="${row.ln_type=='행정예고'}"><span class="label t_admin">행정예고</span></c:when>
                      <c:when test="${row.ln_type=='확정고시'}"><span class="label t_fix">확정고시</span></c:when>
                      <c:when test="${row.ln_type=='입법예고'}"><span class="label t_law">입법예고</span></c:when>
                      <c:when test="${row.ln_type=='법률공포'}"><span class="label t_pub">법률공포</span></c:when>
                      <c:otherwise><c:if test="${not empty row.ln_type}"><span class="label">${fn:escapeXml(row.ln_type)}</span></c:if></c:otherwise>
                    </c:choose>
                    <c:if test="${not empty row.ln_item}"><br><span class="label t_item">${fn:escapeXml(row.ln_item)}</span></c:if>
                  </td>
                  <td class="td_left"><span>${fn:escapeXml(row.ln_title)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.ln_noti_date)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.ln_exec_date)}</span></td>
                  <td style="text-align:center"><span>${fn:escapeXml(row.ln_dept)}</span></td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <%-- 페이지네이션 --%>
    <div class="paginate_ui">
      <c:if test="${page>1}">
        <a class="pre" href="?tab=revise&page=1&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC7=${fn:escapeXml(qC7)}">&lt;&lt;</a>
        <a class="pre" href="?tab=revise&page=${page-1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC7=${fn:escapeXml(qC7)}">&lt;</a>
      </c:if>
      <c:forEach begin="1" end="${pageCnt}" var="p">
        <c:if test="${p>=page-4 && p<=page+4}">
          <c:choose>
            <c:when test="${p==page}"><strong>${p}</strong></c:when>
            <c:otherwise>
              <a href="?tab=revise&page=${p}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC7=${fn:escapeXml(qC7)}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:if>
      </c:forEach>
      <c:if test="${page<pageCnt}">
        <a class="next" href="?tab=revise&page=${page+1}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC7=${fn:escapeXml(qC7)}">&gt;</a>
        <a class="next" href="?tab=revise&page=${pageCnt}&qKey=${qKey}&qWord=${fn:escapeXml(qWord)}&qC7=${fn:escapeXml(qC7)}">&gt;&gt;</a>
      </c:if>
    </div>

  </div><%-- /contents --%>
</div><%-- /container --%>

</div><%-- /wrap --%>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>
