<!-- Matomo -->
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
<!-- End Matomo Code -->

