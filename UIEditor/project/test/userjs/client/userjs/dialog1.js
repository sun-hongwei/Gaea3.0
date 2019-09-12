function dialog1_start(uiInfo, initdata) {
    ControlHelp.setEvent(uiInfo, "deleteself2", "onClick", function() {
        GlobalDialog.close();
    });

}

var info = getGlobalScriptInfo("dialog1")
dialog1_start(info.uiInfo, info.initdata);