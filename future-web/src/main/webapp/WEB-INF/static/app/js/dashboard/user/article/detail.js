$(function () {
    updateState("user/article");

    $(".markdown a").attr("target", "_blank");
    $(".markdown pre").addClass("prettyprint linenums");

    prettyPrint();

});