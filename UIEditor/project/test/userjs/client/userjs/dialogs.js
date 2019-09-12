function dialogs_start(uiInfo, initdata) {
    ControlHelp.setEvent(uiInfo, "dialogButton", "onGetUserData", function() {
        jumpUIForName("dialogs");
    });

    ControlHelp.setEvent(uiInfo, "deleteself1", "onClick", function() {
        GlobalDialog.close();
    });

}

var info = getGlobalScriptInfo("dialogs")
dialogs_start(info.uiInfo, info.initdata);