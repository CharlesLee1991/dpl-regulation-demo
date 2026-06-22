<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>법규정보DB — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/front_common.js"></script>
<script>
function jfReset(){
  $("#qWord").val(""); $("#qLL").val(""); $("#qLR").val("0"); $("#frmSearch").submit();
}
$(function(){
  $("th[data-act='ls-sort']").click(function(){
    var s=$(this).data("sort");
    var c=$("#qSort").val()||"";
    var d=(c==="A|"+s)?"D":"A";
    $("#qSort").val(d+"|"+s); $("#frmSearch").submit();
  });
  $("#qLL").change(function(){
    var idx=$(this).val(); $("#qLR option:gt(0)").remove();
    if(idx) fetch("/ajax/?type=regulation&qLL="+idx).then(r=>r.json()).then(d=>{
      (d.rows||[]).forEach(row=>{$("#qLR").append('<option value="'+row.lr_idx+'">'+row.lr_title+'</option>');});
    });
  });
});
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
      <li><a class="on" href="/front/legal/">법규정보DB</a></li>
      <li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li>
    </ul>
  </aside>

  <div id="contents">
    <div id="title_content">
      <div id="title"><span>법규정보DB</span></div>
      <p class="bread"><span>Home</span><span>법규정보</span><span>법규정보DB</span></p>
    </div>

    <%-- 검색폼 --%>
    <form id="frmSearch" method="get" action="/front/legal/">
      <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
      <div class="search_table">
        <table border="0" cellspacing="0" cellpadding="0">
          <colgroup><col width="149"><col width="*"></colgroup>
          <tbody>
            <tr>
              <th>키워드</th>
              <td>
                <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}">
                <button type="submit" class="btn btn_style_01">검색</button>
                <button type="button" class="btn btn_style_02" onclick="jfReset()">초기화</button>
              </td>
            </tr>
            <tr>
              <th>관리법률</th>
              <td>
                <select name="qLL" id="qLL">
                  <option value="">전체</option>
                  <c:forEach var="ll" items="${legalList}">
                    <option value="${ll.ll_idx}" <c:if test="${ll.ll_idx==qLL}">selected</c:if>>${fn:escapeXml(ll.ll_title)}</option>
                  </c:forEach>
                </select>
                <select name="qLR" id="qLR">
                  <option value="0">관리제도 선택</option>
                  <c:forEach var="lr" items="${regulationList}">
                    <option value="${lr.lr_idx}" <c:if test="${lr.lr_idx==qLR}">selected</c:if>>${fn:escapeXml(lr.lr_title)}</option>
                  </c:forEach>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </form>

    <%-- 건수 --%>
    <div class="btn_r" style="margin-bottom:8px">
      <span style="font-size:13px;color:#555">
        <strong style="color:#1565c0">${total}</strong>건의 검색결과가 있습니다.
      </span>
    </div>

    <%-- 목록 테이블 — table_list_style_01 (개발서버 동일) --%>
    <div class="table_list_style_01">
      <table border="0" cellspacing="0" cellpadding="0" style="table-layout:fixed">
        <caption>검색결과</caption>
        <colgroup>
          <col width="70"><col><col width="200"><col width="80"><col width="110">
        </colgroup>
        <thead>
          <tr>
            <th>번호</th>
            <th data-act="ls-sort" data-sort="TITLE" style="cursor:pointer">
              관리제도 <c:if test="${fn:startsWith(qSort,'A|TITLE')}">▲</c:if><c:if test="${fn:startsWith(qSort,'D|TITLE')}">▼</c:if>
            </th>
            <th data-act="ls-sort" data-sort="LEGAL" style="cursor:pointer">규제법률</th>
            <th data-act="ls-sort" data-sort="DISP" style="cursor:pointer">노출</th>
            <th data-act="ls-sort" data-sort="RDATE" style="cursor:pointer">등록일</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty list}">
              <tr><td colspan="5" style="text-align:center;padding:40px;color:#999">검색된 결과가 없습니다.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="row" items="${list}" varStatus="st">
                <tr>
                  <td style="text-align:center">${total-(page-1)*10-st.index}</td>
                  <td class="td_left">
                    <a href="/front/legal/view?lr_idx=${row.lr_idx}">
                      <span>${fn:escapeXml(row.lr_title)}</span>
                    </a>
                  </td>
                  <td style="text-align:center">
                    <span>${fn:escapeXml(row.ll_title != null ? row.ll_title : '')}</span>
                  </td>
                  <td style="text-align:center">${row.lr_is_use eq 'Y' ? '사용' : '미사용'}</td>
                  <td style="text-align:center">${row.lr_reg_date}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>

    <%-- 페이지네이션 — 개발서버 동일 구조 (숫자 링크) --%>
    <div class="paginate_ui">
      <c:if test="${page>1}">
        <a class="pre" href="?page=1&qLL=${qLL}&qLR=${qLR}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">
          <img src="/static/img_front/images/common/btn_paginate_ui_prev_02.gif" alt="처음" onerror="this.style.display='none';this.parentNode.innerHTML='&lt;&lt;'">
        </a>
        <a class="pre" href="?page=${page-1}&qLL=${qLL}&qLR=${qLR}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">
          <img src="/static/img_front/images/common/btn_paginate_ui_prev_01.gif" alt="이전" onerror="this.style.display='none';this.parentNode.innerHTML='&lt;'">
        </a>
      </c:if>
      <c:forEach begin="1" end="${pageCnt}" var="p">
        <c:if test="${p>=page-4 && p<=page+4}">
          <c:choose>
            <c:when test="${p==page}"><strong>${p}</strong></c:when>
            <c:otherwise>
              <a href="?page=${p}&qLL=${qLL}&qLR=${qLR}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">${p}</a>
            </c:otherwise>
          </c:choose>
        </c:if>
      </c:forEach>
      <c:if test="${page<pageCnt}">
        <a class="next" href="?page=${page+1}&qLL=${qLL}&qLR=${qLR}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">
          <img src="/static/img_front/images/common/btn_paginate_ui_next_01.gif" alt="다음" onerror="this.style.display='none';this.parentNode.innerHTML='&gt;'">
        </a>
        <a class="next" href="?page=${pageCnt}&qLL=${qLL}&qLR=${qLR}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">
          <img src="/static/img_front/images/common/btn_paginate_ui_next_02.gif" alt="마지막" onerror="this.style.display='none';this.parentNode.innerHTML='&gt;&gt;'">
        </a>
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
