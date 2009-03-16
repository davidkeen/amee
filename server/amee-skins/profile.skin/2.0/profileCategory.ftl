<#include 'profileCommon.ftl'>
<#include '/includes/furniture.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/profile_service.js" type="text/javascript"></script>

<script type="text/javascript">

    function profileCategoryLoaded() {
        $("tAmount").innerHTML = this.resource.tAmount;
    }

    function profileItemDeleted() {
        Effect.Fade(this.resourceElem);
        var profileCategoryResource = new ProfileCategoryResource('${profile.uid}', '${pathItem.fullPath}');
        profileCategoryResource.loadedCallback = profileCategoryLoaded;
        profileCategoryResource.load();
    }

    function deleteProfileItem(profileItemUid, profileItemPath) {
        if (profileItemPath && profileItemPath.indexOf("?") > -1) {
            resourceUrl = profileItemPath + '&method=delete';
        } else {
            resourceUrl = profileItemPath + '?method=delete';
        }
        resourceElem = $('Elem_' + profileItemUid);
        resourceType = 'Profile Item';
        var deleteResource = new DeleteResource();
        deleteResource.deleteResourceCallback = profileItemDeleted;
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }

    document.observe('dom:loaded', function() {

        <#if browser.profileItemActions.allowList>

            var profileCategoryApiService = new ProfileCategoryApiService({
                heading : "Profile Items",
                headingElementName : "apiHeading",
                contentElementName : "apiContent",
                tAmountElementName : 'apiTAmount',
                pagerTopElementName : 'apiTopPager',
                pagerBtmElementName : 'apiBottomPager',
                headingCategory : 'Profile Categories',
                dataHeadingCategory : 'Profile Category Details',
                dataHeadingCategoryElementName : 'apiDataCategoryHeading',
                dataContentElementName : "apiDataCategoryContent",
                apiVersion : '2.0'
            });
            profileCategoryApiService.apiRequest();

        </#if>

        <#if dataCategory.itemDefinition?? && browser.profileItemActions.allowCreate>
            new DrillDown(
                "/data${pathItem.fullPath}",
                "2.0",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                true
            ).loadDrillDown();
        </#if>
    });

</script>

<h1>Profile Category</h1>

<#include 'profileTrail.ftl'>

<h2 id="apiDataCategoryHeading"></h2>
<p id="apiDataCategoryContent"></p>
<h2 id="apiHeading"></h2>
<div id="apiTopPager"></div>
<table id="apiContent"></table>
<p id="apiTAmount"></p>
<div id="apiBottomPager"></div>

<#if dataCategory.itemDefinition?? && browser.profileItemActions.allowCreate>
    <h2 id="createProfileHeading"></h2>
    <form id="createProfileFrm" onSubmit="return false;">
        <div id="createProfileItemDiv">
        </div>
    </form>
</#if>

<#include '/includes/after_content.ftl'>