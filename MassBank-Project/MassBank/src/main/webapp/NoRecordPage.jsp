<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!-- Copyright (C) 2021 MassBank consortium -->

<!-- This file is part of MassBank. -->

<!-- MassBank is free software; you can redistribute it and/or -->
<!-- modify it under the terms of the GNU General Public License -->
<!-- as published by the Free Software Foundation; either version 2 -->
<!-- of the License, or (at your option) any later version. -->

<!-- This program is distributed in the hope that it will be useful, -->
<!-- but WITHOUT ANY WARRANTY; without even the implied warranty of -->
<!-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the -->
<!-- GNU General Public License for more details. -->

<!-- You should have received a copy of the GNU General Public License -->
<!-- along with this program; if not, write to the Free Software -->
<!-- Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="en">

<head>
	<title>No Mass Spectrum</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0">
	<meta name="description" content="MassBank Record of ${accession} not found">
	<meta name="keywords" content="No MassBank record">
	<meta name="author" content="MassBank">
	<meta name="copyright" content="MassBank Consortium"/>
	<link rel="icon" href="favicon.ico" type="image/x-icon">
	<link rel="stylesheet" type="text/css" href="css/w3.css">
	<link rel="stylesheet" type="text/css" href="css/w3-theme-grey.css">
	<link rel="stylesheet" type="text/css" href="css/massbank.css">
	<link rel="preconnect" href="https://fonts.gstatic.com">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Mulish:ital,wght@0,400;0,600;1,400;1,600">
	<script src="js/jquery-3.5.1.min.js"></script>
	<script src="js/svg4everybody-2.1.9.min.js"></script>
	<script>svg4everybody();</script>
	
	<!-- Matomo -->
	<link rel="stylesheet" type="text/css" href="css/cookieconsent-3.1.1.min.css">
	<script src="https://www.ufz.de/stats/piwik.js" type="text/javascript"></script>
	<script type="text/javascript">
	try  {
        var piwikTracker = Piwik.getTracker("https://www.ufz.de/stats/piwik.php", 24);
            if(localStorage.getItem('cookie-banner') == '0' || localStorage.getItem('cookie-banner') === null) {
                piwikTracker.disableCookies();
            }
            piwikTracker.trackPageView();
            piwikTracker.enableLinkTracking();
        } catch( err ) {}
	</script>
	
	<noscript>
		<img src="https://www.ufz.de/stats/piwik.php?idsite=24" style="border:0" alt="" />
	</noscript>
	
	<script type="text/javascript">
            $(document).ready(function() {
                    if(localStorage.getItem('cookie-banner') === null) {
                        $('#cookie-banner').modal();
                        
                        $('#cookie-banner button.btn-secondary').off('click').on('click', function() {
                            localStorage.setItem('cookie-banner', '0');
                                        try  {
                var piwikTracker = Piwik.getTracker("https://www.ufz.de/stats/piwik.php", 24);
                if(localStorage.getItem('cookie-banner') == '0' || localStorage.getItem('cookie-banner') === null) {
                    piwikTracker.disableCookies();
                }
                piwikTracker.trackPageView();
                piwikTracker.enableLinkTracking();
            } catch( err ) {}
                        });
                        $('#cookie-banner button.btn-success').off('click').on('click', function() {
                            localStorage.setItem('cookie-banner', '1');
                                        try  {
                var piwikTracker = Piwik.getTracker("https://www.ufz.de/stats/piwik.php", 24);
                if(localStorage.getItem('cookie-banner') == '0' || localStorage.getItem('cookie-banner') === null) {
                    piwikTracker.disableCookies();
                }
                piwikTracker.trackPageView();
                piwikTracker.enableLinkTracking();
            } catch( err ) {}
                        });
                    }
				});
	</script>
</head>

<body class="w3-theme-gradient">

	<noscript>
		<div class="w3-panel w3-yellow">
			<p>Your JavaScript is disabled. To properly show MassBank please
				enable JavaScript and reload.</p>
		</div>
	</noscript>

	<div>
		<jsp:include page="menu.html" />
	</div>

	<header class="w3-container w3-text-dark-grey w3-grey">
		<div class="w3-bar">
			<div class="w3-left">
				<h1>
					<b>No Mass Spectrum</b>
				</h1>
			</div>
		</div>
	</header>

	<div class="w3-container">
  	<c:if test="${not empty accession}">
		<p>
			No MassBank record exists for accession '${accession}'.
		</p>
		<p>
			If you want to make a 'Keyword' search please visit our
			<a href="Search" title="Search">Search</a> page.
		</p>
		</c:if>
		<c:if test="${not empty error}">
			<b>Error message: ${error}</b>
		</c:if>	
	</div>	
	
	<br>
	<jsp:include page="copyrightline.html"/>
	
	<script src="js/cookieconsent-3.1.1.min.js" type="text/javascript"></script>
	<script src="js/cookieconsent-banner.js" type="text/javascript"></script>
	
</body>
</html>

