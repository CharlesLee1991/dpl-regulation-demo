<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html><html lang="ko">
<head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>디지털 제품안전 라이브러리 — 디지털 제품안전 라이브러리</title>
<link rel="stylesheet" href="/static/css/front_common.css">
<link rel="stylesheet" href="/static/css/swiper-bundle.min.css">
<script src="/static/js/jquery-1.8.3.min.js"></script>
<script src="/static/js/swiper-bundle.min.js"></script>
<script src="/static/js/jquery.easing.min.js"></script>
<script src="/static/js/front_common.js"></script>
</head>
<body>
<div id="wrap" class="main">
<div id="header">
<header>
  <div class="logo"><a href="/front/"><img src="/static/img_front/images/common/logo.png" alt="디지털 제품안전 라이브러리"></a></div>
  <nav><ul><li><a class="gnb_1dep" href="/front/legal/">법규정보</a><div class="gnb_2dep"><ul><li><a href="/front/legal/">법규정보DB</a></li><li><a href="/front/legal/?tab=revise">법규 제·개정 정보</a></li></ul></div></li>
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

<section class="visual_area">
  <div class="search_content">
    <h2 class="title">제품 안전정보 통합 검색</h2>
    <p class="text">이제 찾고 싶은 제품 안전정보 관련 소식을<br>간편하게 키워드 입력 또는 테마 검색으로 찾아보세요.</p>
    <form action="/front/search/" id="frmSearch" method="get">
      <div class="search_form">
        <input type="text" id="qWord" name="qWord" placeholder="키워드를 입력해 주세요.">
        <button type="submit">검색</button>
      </div>
    </form>
  </div>
</section>

<section class="info_status">
  <div class="inner">
    <h2 class="main_title">조회 가능<br>정보 현황</h2>
    <ul>
      <a href="/front/legal/"><li class="counter_ui">
        <em>법규정보</em>
        <strong class="counter"><fmt:formatNumber value="${cntLegal}" type="number"/></strong> 개
      </li></a>
      <a href="/front/safety/"><li class="counter_ui">
        <em>위해정보</em>
        <strong class="counter"><fmt:formatNumber value="${cntSafety}" type="number"/></strong> 개
      </li></a>
      <a href="/front/standard/"><li class="counter_ui">
        <em>롯데 스탠다드</em>
        <strong class="counter"><fmt:formatNumber value="${cntStandard}" type="number"/></strong> 개
      </li></a>
    </ul>
  </div>
</section>

<section class="search_themes">
  <div class="inner">
    <h2 class="main_title">테마별 검색</h2>
    <div class="tab_theme">
      <button class="on" type="button" onclick="location.href='/front/legal/'">법규정보</button>
      <button type="button" onclick="location.href='/front/safety/'">위해정보</button>
      <button type="button" onclick="location.href='/front/standard/'">롯데 스탠다드</button>
    </div>
    <div class="category">
      <div class="swiper paradeSlide">
        <ul class="swiper-wrapper" id="theme-swiper">
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=패션잡화">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_01.png" alt=""></span>
    <span class="tit">패션잡화</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=취미·스포츠">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_02.png" alt=""></span>
    <span class="tit">취미·스포츠</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=주방용품">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_03.png" alt=""></span>
    <span class="tit">주방용품</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=청소·욕실">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_04.png" alt=""></span>
    <span class="tit">청소·욕실</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=출산·유아동">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_05.png" alt=""></span>
    <span class="tit">출산·유아동</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=뷰티·퍼스널케어">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_06.png" alt=""></span>
    <span class="tit">뷰티·퍼스널케어</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=문구·OA">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_07.png" alt=""></span>
    <span class="tit">문구·OA</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=리빙·인테리어">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_08.png" alt=""></span>
    <span class="tit">리빙·인테리어</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=디지털·가전">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_09.png" alt=""></span>
    <span class="tit">디지털·가전</span>
  </a>
</li>
<li class="swiper-slide" style="margin-right:30px">
  <a href="/front/legal/?cate=건강·위생">
    <span class="img"><img src="/static/img_front/images/main/ico_theme_10.png" alt=""></span>
    <span class="tit">건강·위생</span>
  </a>
</li>
        </ul>
        <div class="swiper-button-prev"></div>
        <div class="swiper-button-next"></div>
      </div>
    </div>
  </div>
</section>

<section class="news_area">
  <div class="inner">
    <h2 class="main_title">NEWS</h2>
    <div class="box_wrap">
      <div class="box news_content">
        <div class="text_title">법규 제·개정 정보<a class="btn_more" href="/front/legal/?tab=revise">more</a></div>
        <ul>
          <c:forEach var="item" items="${legalNews}">
            <li>
              <a href="/front/legal/view?lr_idx=${item.lr_idx}">
                <span class="label">개정</span>
                <p>${fn:escapeXml(item.lr_title)}</p>
              </a>
              <span class="txt_date">${item.lr_reg_date}</span>
            </li>
          </c:forEach>
          <c:if test="${empty legalNews}">
            <li><a href="/front/legal/"><span class="label">안내</span><p>법규정보를 확인하세요</p></a></li>
          </c:if>
        </ul>
      </div>
      <div class="box security_content">
        <div class="text_title">법규정보 바로가기<a class="btn_more" href="/front/legal/">more</a></div>
        <ul>
          <c:forEach var="item" items="${recentLegal}">
            <li>
              <a href="/front/legal/view?lr_idx=${item.lr_idx}">
                <span class="level level1">법규</span>
                <p>${fn:escapeXml(item.lr_title)}</p>
              </a>
              <span class="txt_source">${fn:escapeXml(item.ll_title != null ? item.ll_title : '')}</span>
            </li>
          </c:forEach>
        </ul>
      </div>
    </div>
  </div>
</section>

<script>
$(function(){
  // 조회현황 숫자는 서버에서 콤마포맷 정적 렌더 (롤링 플러그인 미안착 버그로 제거)
  // 테마 swiper
  if(typeof Swiper!=='undefined'){
    new Swiper('.paradeSlide',{slidesPerView:6,spaceBetween:30,loop:false,
      navigation:{nextEl:'.swiper-button-next',prevEl:'.swiper-button-prev'}});
  }
});
</script>

</div><%-- /wrap --%>
<div id="footer"><footer>
  <div class="footer_logo"><img src="/static/img_front/images/common/footer_logo.png" alt="롯데중앙연구소 안전센터"></div>
  <address>서울특별시 강서구 마곡중앙로 201 (마곡동) <i></i> TEL 02.6309.3244(식품) 02.6309.3581(비식품) <i></i> FAX 02.6309.3099</address>
  <p class="copy">copyright © 2022 LOTTE R&amp;D CENTER SAFETY CENTER Co., Ltd. All rights reserved.</p>
</footer></div>
</body></html>