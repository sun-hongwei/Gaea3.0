var RequestInstance = {
	submit:function(serviceName, command, hint, data, onsucc, onfail) {
		if(Tools.isNull(serviceName) || Tools.isNull(command)){
            if (onfail != undefined)
                onfail({ret:false,msg:"servicePageName and command is not null!"});
            return;
		}
		
        var ajaxurl = "./Services/DispatchService.php";
        var json = Tools.createAjaxPackage(command, data);
        json["Command_Running_Hint"] = hint;
        json["SERVICE_NAME"] = serviceName;
        Tools.updateAjaxPackageSign(json);
        Tools.ajaxSubmit({url:ajaxurl, data:json, success:onsucc,
                                error : function(jqXHR, textStatus, errorThrown){
                                    if (onfail != undefined){
                                        if (Tools.isNull(errorThrown))
                                        {
                                            onfail(textStatus);
                                        }
                                        else
                                            onfail(errorThrown);
                                    }
                                } 
                        });
    }
}