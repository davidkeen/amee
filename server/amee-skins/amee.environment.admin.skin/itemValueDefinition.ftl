<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a> /
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}'>${itemValueDefinition.name}</a></p>
<h2>Item Value Definition Details</h2>
<p>Name: ${itemValueDefinition.name}<br/>
   Value Definition: <#if browser.valueDefinitionActions.allowView><a href='/environments/${environment.uid}/valueDefinitions/${itemValueDefinition.valueDefinition.uid}'>${itemValueDefinition.valueDefinition.name}</a><#else>${itemValueDefinition.valueDefinition.name}</#if><br/>
   Value Type: ${itemValueDefinition.valueDefinition.valueType}<br/>
   Created: ${itemValueDefinition.created?datetime}<br/>
   Modified: ${itemValueDefinition.modified?datetime}<br/>
</p>
<#if browser.itemValueDefinitionActions.allowModify>
  <h2>Update Item Value Definition</h2>
  <p>  <form action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${itemValueDefinition.name}' type='text' size='30'/><br/>
  Path: <input name='path' value='${itemValueDefinition.path}' type='text' size='30'/><br/>
  Default value: <input name='value' <#if itemValueDefinition.value??>value='${itemValueDefinition.value}'</#if> type='text' size='30'/><br/>
  Choices: <input name='choices' <#if itemValueDefinition.choices??>value='${itemValueDefinition.choices}'</#if> type='text' size='30'/> (comma delimited name=value pairs)<br/>
  Get value from admin? <select name='fromData'><option value='false'<#if !itemValueDefinition.fromData> selected</#if>>No</option><option value='true'<#if itemValueDefinition.fromData> selected</#if>>Yes</option></select><br/>
  Get value from user? <select name='fromProfile'><option value='false'<#if !itemValueDefinition.fromProfile> selected</#if>>No</option><option value='true'<#if itemValueDefinition.fromProfile> selected</#if>>Yes</option></select><br/>
  Allowed roles: <input name='allowedRoles' <#if itemValueDefinition.allowedRoles??>value='${itemValueDefinition.allowedRoles}'</#if> type='text' size='30'/><br/>
  Unit: <input name='unit' value='<#if itemValueDefinition.hasUnits()>${itemValueDefinition.unit}</#if>' type='text' size="10"/><br/>
  PerUnit: <input name='perUnit' value='<#if itemValueDefinition.hasPerUnits()>${itemValueDefinition.perUnit}</#if>' type='text' size="10"/><br/>
  API Version:
  <#list apiVersions as v>
    ${v.version}<input type="checkbox" name="apiversion-${v.version}" value="true" <#if itemValueDefinition.isValidInAPIVersion(v)>checked</#if>>
  </#list><br/>                                             
  Alias To: <select name='aliasedTo'>


    <#if !itemValueDefinition.aliasedTo??>
        <option value='' selected>None</option>
    </#if>
    <#list itemDefinition.itemValueDefinitions as ivd>
        <#if itemValueDefinition.uid != ivd.uid>
            <option value='${ivd.uid}' <#if itemValueDefinition.aliasedTo?? && itemValueDefinition.aliasedTo.uid == ivd.uid>selected</#if>>${ivd.name}</option>
        </#if>
    </#list>
  </select>
  <br/><br/>
  <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>