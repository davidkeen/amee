<#assign sectionName = 'admin' />

<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl' />

<script type="text/javascript">

    function success(response) {
        document.algorithmFrm.algorithmContextContent.value = response.responseJSON.algorithmContextResource.content;
    }

    function failure(response) {
        document.algorithmFrm.algorithmContextContent.value = "Error! Unable to get Algorithm context, try reloading the page."
    }

    function successTest(response) {
        var errElement = document.getElementById("errorTxt");
        var resElement = document.getElementById("resultTxt");

        if (response.responseJSON.algorithmTestWrapper.result !== undefined) {
            var e = new Element('b', {id : 'resultTxt'});
            e.insert(response.responseJSON.algorithmTestWrapper.result);
            resElement.replace(e)

            if (errElement != null) {
                errElement.value = "";
            }
        } else {
            var e = new Element('textarea', {id : 'errorTxt', rows : 15, cols : 60, readonly : ""}).setStyle("color:red;");
            e.value = response.responseJSON.algorithmTestWrapper.error;
            errElement.replace(e);

            if (resElement != null) {
                var re = new Element('b', {id : 'resultTxt'});
                re.insert("&nbsp;");
                resElement.replace(re);
            }
        }
        return false;
    }

    function failureTest(response) {
        var errElement = document.getElementById("errorTxt");
        errElement.value = "Error! Unable to test Algorithm, try reloading the page."
    }

    function updateContext(select) {
        var url = "/admin/algorithmContexts/";
        var uid = select.options[select.selectedIndex].value;
        if (uid != "") {
            // update url
            url += uid;

            // make server call
            var myAjax = new Ajax.Request(
                    url, {
                method: 'get',
                parameters: 'method=get',
                requestHeaders: ['Accept', 'application/json'],
                onSuccess: success.bind(this),
                onFailure: failure.bind(this)});
        } else {
            // reset
            document.algorithmFrm.algorithmContextContent.value = "";
        }
        return false;
    }

    function testAlgorithm(path) {

        // init form
        document.algorithmTestFrm.testValues.value = document.algorithmFrm.testValues.value;
        document.algorithmTestFrm.testAlgorithmContent.value = document.algorithmFrm.content.value;
        if (document.algorithmFrm.algorithmContextContent) {
            document.algorithmTestFrm.testAlgorithmContextContent.value = document.algorithmFrm.algorithmContextContent.value;
        }
        document.algorithmTestFrm.startDate.value = document.algorithmFrm.startDate.value;
        document.algorithmTestFrm.endDate.value = document.algorithmFrm.endDate.value;

        var myAjax = new Ajax.Request(
                path, {
            method: 'get',
            parameters: document.algorithmTestFrm.serialize(),
            requestHeaders: ['Accept', 'application/json'],
            onSuccess: successTest.bind(this),
            onFailure: failureTest.bind(this)});
        return false;
    }

</script>

<style type="text/css">
    .nameCol {
        width: 15%;
        float: left;
    }

    .valueCol {
        width: 85%;
        float: right;
    }

    fieldset {
        width: 600px
    }
</style>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> /
   <a href='/admin/itemDefinitions'>Item Definitions</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/algorithms'>Algorithms</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/algorithms/${algorithm.uid}'>${algorithm.name}</a>
</p>

<h2>Algorithm Details</h2>
<p>Name: ${algorithm.name}<br/>
    Created: ${algorithm.created?datetime}<br/>
    Modified: ${algorithm.modified?datetime}<br/>
</p>

<#if canModify()>

    <#if !testValues?? >
        <#assign testValues="" />
    </#if>

    <h2>Update Algorithm</h2>

    <p>

        <form id="algorithmFrm" name="algorithmFrm"
              action='/admin/itemDefinitions/${itemDefinition.uid}/algorithms/${algorithm.uid}?method=put'
              method='POST' enctype='application/x-www-form-urlencoded'>

            <fieldset>
                <legend>Algorithm</legend>
                <div class="nameCol">Name:</div>
                <div class="valueCol"><input name='name' value='${algorithm.name}' type='text' size='30'/></div>
                <div class="nameCol">Context:</div>
                <div class="valueCol">
                    <select name="algorithmContextUid" onchange="updateContext(this)">
                        <option value="">None</option>
                        <#list algorithmContexts as algorithmContext>
                            <#if algorithm.algorithmContext?? && algorithm.algorithmContext == algorithmContext>
                                <option value="${algorithmContext.uid}" selected="true">${algorithmContext.name}</option>
                            <#else>
                                <option value="${algorithmContext.uid}">${algorithmContext.name}</option>
                            </#if>
                        </#list>
                    </select>
                </div>
                <div class="nameCol">Content:</div>
                <div class="valueCol"><textarea name='content' rows='15' cols='60'>${algorithm.content}</textarea></div>
                <input type='submit' value='Update' class="valueCol"/>
            </fieldset>

            <fieldset>

                <legend>Algorithm Test</legend>

                <div class="nameCol">Values:</div>
                <div class="valueCol"><input name='testValues' value='${testValues}' type='text' size='60'/> <a href="#" onclick="return testAlgorithm('${path}');" style="margin-left:10%">Test</a><br/>
                    <span style="font-size:12; font-weight:bold; color:silver;">Comma delimited list of name=value pairs (e.g name=1,kg=2)</span>
                </div><br/><br/>

                <div class="nameCol">Date(s):<br/><span style="font-size:12; font-weight:bold; color:silver;">optional</span></div>
                <div class="valueCol">
                    <div class="nameCol">Start:</div> <input name='startDate' value='' type='text' size='20' />
                    <span style="font-size:12; font-weight:bold; color:silver;">format (${getDateFormat(activeUser.APIVersion.version)})</span><br/>
                    <div class="nameCol">End:</div> <input name='endDate' value='' type='text' size='20' />
                    <span style="font-size:12; font-weight:bold; color:silver;">format (${getDateFormat(activeUser.APIVersion.version)})</span>
                </div><br/><br/><br/><hr>

                <div name="testResult" id="testResult" style="visibility:visible;">
                    <div class="nameCol">Result:</div>
                    <div name="result" id="result" class="valueCol"><b id="resultTxt">&nbsp;</b></div>
                </div><br/><br/>

                <div name="testError" id="testError" style="visibility:visible;">
                    <div class="nameCol">Error:</div>
                    <div name="error" id="error" class="valueCol"><textarea id="errorTxt" rows="15" cols="60" readonly="" style="color:red;"></textarea></div>
                </div>

            </fieldset>

        </form>

        <form id="algorithmTestFrm" name="algorithmTestFrm" action='#' method='POST'
              enctype='application/x-www-form-urlencoded'>

            <input type="hidden" name="testAlgorithmContent" value=""/>
            <input type="hidden" name="testAlgorithmContextContent" value=""/>
            <input type="hidden" name="testValues" value=""/>
            <input type="hidden" name="startDate" value=""/>
            <input type="hidden" name="endDate" value=""/>

        </form>

    </p>

</#if>

<#include '/includes/after_content.ftl'>