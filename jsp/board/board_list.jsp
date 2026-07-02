<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% String uri = request.getRequestURI(); %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>${boardTitle} — DPL 법규정보 관리시스템</title>
<link rel="stylesheet" href="/static/css/admin_common.css">
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<script src="/static/js/jquery-1.11.1.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/admin_common.js"></script>
<script>
function jfCreate(idx){location.href="?mode=form&code=${code}&bd_idx="+idx+"&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&page=${page}";}
function jfSearch(){$("#frmSearch").attr("action","?mode=list&code=${code}");return true;}
function jfSearchReset(){$("#frmSearch input[type=text],#frmSearch select").val("");$("#frmSearch").attr("action","?mode=list&code=${code}").submit();}
$(function(){$("th[data-act='ls-sort']").click(function(){var s=$(this).data("sort");var c=$("#qSort").val();var d=(c===("A|"+s))?"D":"A";$("#qSort").val(d+"|"+s);$("#frmSearch").attr("action","?mode=list&code=${code}").submit();});});
</script>
</head><body>
<div id="header">
  <div class="logo"><a href="/"><img src="/static/img_admin/common/logo.png" alt="롯데중앙연구소 안전센터"></a></div>
  <div class="side_menu"><div class="side_link my_info">
    <a href="#none"><strong>관리자</strong>DPL</a>
    <div class="my_info_view"><p><strong>관리자</strong><span>DPL 관리자</span></p>
      <div class="btn_c"><a href="#">개인정보 수정</a><a href="#">로그아웃</a></div></div>
  </div></div>
  <div class="gnb">
    <strong class="menu_1depth_01 menu_1depth on"><a href="/legal/?mode=list">법규정보DB 관리</a></strong>
    <div class="menu_2depth_01 menu_2depth"><ul>
      <li><p class="<%= uri.contains("/legal/") ? "on":"" %>"><a href="/legal/?mode=list">규제법률</a></p></li>
      <li><p class="<%= uri.contains("/regulation/") ? "on":"" %>"><a href="/regulation/?mode=list">규제사항</a></p></li>
      <li><p class="<%= uri.contains("/notify/") ? "on":"" %>"><a href="/notify/?mode=list">고시(부속서)</a></p></li>
      <li><p class="<%= uri.contains("/safety/") ? "on":"" %>"><a href="/safety/?mode=list">안전요건</a></p></li>
      <li><p><a href="#">표시사항</a></p></li>
      <li><p class="<%= uri.contains("items_def") ? "on":"" %>"><a href="/items_def/?mode=list">품목정보</a></p></li>
      <li><p class="<%= uri.contains("items_detail") ? "on":"" %>"><a href="/items_detail/?mode=list">세부품목정보</a></p></li>
      <li><p><a href="#">온라인표시사항</a></p></li>
      <li><p><a href="#">법규 제·개정 사항</a></p></li>
    </ul></div>
    <strong class="menu_1depth_02 menu_1depth"><a href="#">안전정보DB 관리</a></strong>
    <div class="menu_2depth_02 menu_2depth"><ul>
      <li><p><a href="#">제품안전 뉴스</a></p></li>
      <li><p><a href="#">위해정보DB</a></p></li>
      <li><p><a href="#">롯데스탠다드(품질기준서)</a></p></li>
    </ul></div>
    <strong class="menu_1depth_03 menu_1depth"><a href="#">셀프러닝 관리</a></strong>
    <div class="menu_2depth_03 menu_2depth"><ul>
      <li><p><a href="#">숏클래스</a></p></li>
      <li><p class="<%= uri.contains("/board/") && "6".equals(request.getParameter("code")==null?"6":request.getParameter("code")) ? "on":"" %>"><a href="/board/?mode=list&code=6">유용한 정보</a></p></li>
      <li><p class="<%= "8".equals(request.getParameter("code")) ? "on":"" %>"><a href="/board/?mode=list&code=8">동영상 정보</a></p></li>
      <li><p class="<%= "10".equals(request.getParameter("code")) ? "on":"" %>"><a href="/board/?mode=list&code=10">안전센터정보</a></p></li>
    </ul></div>
    <strong class="menu_1depth_04 menu_1depth"><a href="#">카테고리 관리</a></strong>
    <div class="menu_2depth_04 menu_2depth"><ul>
      <li><p><a href="#">중분류 관리</a></p></li>
      <li><p><a href="#">소분류 관리</a></p></li>
    </ul></div>
    <strong class="menu_1depth_05 menu_1depth"><a href="#">배너 관리</a></strong>
    <div class="menu_2depth_05 menu_2depth"><ul>
      <li><p><a href="#">메인상단띠배너</a></p></li>
    </ul></div>
  </div>
</div>
<div id="container">
<div id="contents">
  <div class="title title_navi">
    <h2>${boardTitle}</h2>
    <p class="navi"><span>셀프러닝 관리</span><span>${boardTitle}</span></p>
  </div>

  <form id="frmSearch" name="frmSearch" method="get" action="?mode=list&code=${code}">
    <input type="hidden" name="mode" value="list">
    <input type="hidden" name="code" value="${code}">
    <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
    <div class="search_box">
      <table><colgroup><col width="120"><col width="*"></colgroup><tbody>
        <tr>
          <th>검색어</th>
          <td>
            <select name="qKey" id="qKey">
              <option value="">전체</option>
              <option value="TITLE" ${qKey=='TITLE'?'selected':''}>제목</option>
              <option value="CONT" ${qKey=='CONT'?'selected':''}>내용</option>
            </select>
            <input type="text" name="qWord" id="qWord" value="${fn:escapeXml(qWord)}" class="inp">
            <button type="submit" class="btn btn_style_01" onclick="return jfSearch();">검색</button>
            <button type="button" class="btn btn_style_02" onclick="jfSearchReset();">초기화</button>
          </td>
        </tr>
      </tbody></table>
    </div>
  </form>

  <div class="list_info">
    <p>총 <strong>${total}</strong>건</p>
    <p class="btn_r"><button type="button" class="btn btn_style_01" onclick="jfCreate(0);">등록</button></p>
  </div>

  <div class="table_list">
    <table><colgroup><col width="70"><col width="*"><col width="120"><col width="110"><col width="80"><col width="70"></colgroup>
      <thead><tr>
        <th>No.</th>
        <th data-act="ls-sort" data-sort="TITLE" style="cursor:pointer;">제목</th>
        <th data-act="ls-sort" data-sort="WRITER" style="cursor:pointer;">작성자</th>
        <th data-act="ls-sort" data-sort="RDATE" style="cursor:pointer;">작성일</th>
        <th data-act="ls-sort" data-sort="HIT" style="cursor:pointer;">조회</th>
        <th>노출</th>
      </tr></thead>
      <tbody>
        <c:choose>
          <c:when test="${empty list}"><tr><td colspan="6">등록된 내용이 없습니다.</td></tr></c:when>
          <c:otherwise>
            <c:forEach var="row" items="${list}" varStatus="st">
              <tr>
                <td>${total - (page-1)*10 - st.index}</td>
                <td class="td_left"><a href="?mode=form&code=${code}&bd_idx=${row.bd_idx}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&page=${page}">${fn:escapeXml(row.bd_title)}</a></td>
                <td>${fn:escapeXml(row.bd_writer)}</td>
                <td>${row.bd_reg_date}</td>
                <td>${row.bd_hit}</td>
                <td>${row.bd_is_use}</td>
              </tr>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </tbody>
    </table>
  </div>

  <div class="paging">
    <c:forEach var="p" begin="1" end="${pageCnt}">
      <c:choose>
        <c:when test="${p==page}"><strong>${p}</strong></c:when>
        <c:otherwise><a href="?mode=list&code=${code}&page=${p}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qSort=${fn:escapeXml(qSort)}">${p}</a></c:otherwise>
      </c:choose>
    </c:forEach>
  </div>

</div>
</div>
</body></html>
