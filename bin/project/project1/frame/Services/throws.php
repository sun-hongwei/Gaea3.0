<?php

function throwException($msgObject){
    if ($msgObject instanceof Exception){
        throw $msgObject;
    }else
        throw new Exception($msgObject);
}
?>