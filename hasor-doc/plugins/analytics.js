//const analyticsPlugin = require('./apush');

module.exports = function (context, options) {
    return {
        name: 'docusaurus-plugin',
        getClientModules() {
            return [];
            //return [analyticsPlugin];
        },
        injectHtmlTags({content}) {
            return {
                postBodyTags: [`
<!-- 百度统计 -->
<script>
    var _hmt = _hmt || [];
    (function () {
        var hm = document.createElement("script");
        hm.src = "https://hm.baidu.com/hm.js?8d5f7a839f4f8fee4eefbeac046d05cc";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(hm, s);
    })();
</script>
<!-- 备案审核 -->
<script>
var tmpTitle = window.location.pathname;

function trackView(){
    if (_hmt == null) {
        return;
    }
    if (tmpTitle != window.location.pathname) {
        try {
            _hmt.push(['_trackPageview', window.location.pathname]); 
        } catch (e) {
        } finally {
            tmpTitle = window.location.pathname;
        }
    }
}

function setTitle(){
    trackView();
    //document.title = 'dbVisitor Project';
    window.setTimeout(setTitle,100);
}
window.setTimeout(setTitle,100);
</script>
`],
            };
        },
    };
};