//此函数为每个页面的入口函数，当页面所有控件初始化完毕，会自动调用此函数，开发人员可以在这个函数中初始页面信息，以及挂载事件。
function initPage(uiInfo, tabName, paramData) {
    ControlHelp.setOnInitUI(initPage);
    // $.ajaxSetup({
    //     cache: true
    // });
    execGlobalScript(uiInfo, tabName, paramData);
}