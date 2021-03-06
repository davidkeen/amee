<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> / 
   <a href='/admin/valueDefinitions'>Value Definitions</a> /
   <a href='/admin/valueDefinitions/${valueDefinition.uid}'>${valueDefinition.name}</a></p>

<h2>Value Definition Details</h2>

<p>Name: ${valueDefinition.name}<br/>
   Value Type: ${valueDefinition.valueType}<br/>
   Created: ${valueDefinition.created?datetime}<br/>
   Modified: ${valueDefinition.modified?datetime}<br/>
</p>

<#if canModify()>
    <h2>Update Value Definition</h2>
    <p>
        <form action='/admin/valueDefinitions/${valueDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' value='${valueDefinition.name}' type='text' size='30'/><br/>
            Description: <input name='description' value='${valueDefinition.description}' type='text' size='50'/><br/>
            Value Type: <select name='valueType'>
            <#list valueTypes?keys as key>
                <option value='${key}'<#if valueDefinition.valueType == key> selected</#if>>${valueTypes[key]}</option>
            </#list>
            </select><br/><br/>
            <input type='submit' value='Update'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>