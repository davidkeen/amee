<#if node??>
  <h2>Details</h2>
  <table>
    <#if node.displayName??>
      <tr>
        <td>Name:</td><td>${node.displayName}</td>
      </tr>
    </#if>
    <#if node.displayPath??>
      <tr>
        <td>Path:</td><td>${node.displayPath}</td>
      </tr>
    </#if>
    <tr>
      <td>UID:</td><td>${node.uid}</td>
    </tr>
    <#if node.amountPerMonth??>
      <tr>
        <td>kgCO2 pm:</td><td>${node.amountPerMonth}</td>
      </tr>
    </#if>
    <tr>
      <td>Created:</td><td>${node.created?string.short}</td>
    </tr>
    <tr>
      <td>Modified:</td><td>${node.modified?string.short}</td>
    </tr>
  </table>
</#if>

<script type='text/javascript'>

function showApiResult(message) {
    var modal = new Control.Modal(false, {
        contents: '<div class="columnBlock paddingTopBottom clearfix" style="overflow: auto; text-align: left;">' + message + '</div>',
        width: 600,
        height: 300
    });
    modal.open();
}

function showJSON() {
    new Ajax.Request(window.location.href,
        {method: 'get', requestHeaders: ['Accept', 'application/json'], onSuccess: showJSONResponse});
}

function showJSONResponse(t) {
    showApiResult(t.responseText.escapeHTML());
}

function showXML() {
    new Ajax.Request(window.location.href,
        {method: 'get', requestHeaders: ['Accept', 'application/xml'], onSuccess: showXMLResponse});
}

function showXMLResponse(t) {
    showApiResult(t.responseText.escapeHTML());
}

</script>
        
<h2>API</h2>
<p>
<form onSubmit="return false;">
<button name='showAPIJSON' type='button' onClick='showJSON(); return false;'>Show JSON</button>
<br/><br/>
<button name='showAPIXML' type='button' onClick='showXML(); return false;'>Show XML</button>
</form>
</p>